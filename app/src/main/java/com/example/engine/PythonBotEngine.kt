package com.example.engine

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

sealed class BotAction {
    data class SendMessage(
        val chatId: Long,
        val text: String,
        val replyToMessageId: Long? = null,
        val replyMarkupJson: String? = null,
        val parseMode: String? = null
    ) : BotAction()

    data class AnswerCallback(
        val callbackId: String,
        val text: String? = null
    ) : BotAction()

    data class DeleteMessage(
        val chatId: Long,
        val messageId: Long
    ) : BotAction()

    data class LogInfo(val message: String) : BotAction()
}

/**
 * Pure-Kotlin Python Telegram Bot (telebot / pyTelegramBotAPI style) runtime engine.
 * Parses and executes bot.py scripts safely inside the APK without DCL.
 */
class PythonBotEngine(private val scriptSource: String) {

    private data class HandlerDef(
        val kind: HandlerKind,
        val commands: List<String>,
        val paramName: String,
        val bodyLines: List<IndentedLine>
    )

    private enum class HandlerKind { MESSAGE, CALLBACK_QUERY }

    private data class IndentedLine(val indent: Int, val text: String)

    private class InlineMarkupBuilder {
        val rows = mutableListOf<List<Map<String, String>>>()
    }

    private class ReplyMarkupBuilder(val resizeKeyboard: Boolean = true) {
        val rows = mutableListOf<List<Map<String, String>>>()
    }

    private class ReturnSignal : RuntimeException()

    private val globalVars = mutableMapOf<String, Any?>()
    private val handlers = mutableListOf<HandlerDef>()

    init {
        parseScript(scriptSource)
    }

    private fun parseScript(source: String) {
        val rawLines = source.replace("\r\n", "\n").split("\n")
        val logicalLines = joinMultilineStatements(rawLines)

        var i = 0
        while (i < logicalLines.size) {
            val line = logicalLines[i]
            if (line.indent == 0 && line.text.startsWith("@")) {
                val decorator = line.text
                if (i + 1 < logicalLines.size && logicalLines[i + 1].text.startsWith("def ")) {
                    val defLine = logicalLines[i + 1].text
                    val paramName = Regex("""def\s+\w+\s*\(\s*(\w+)""")
                        .find(defLine)?.groupValues?.getOrNull(1) ?: "message"
                    val bodyStart = i + 2
                    var bodyEnd = bodyStart
                    while (bodyEnd < logicalLines.size && logicalLines[bodyEnd].indent > 0) {
                        bodyEnd++
                    }
                    val body = logicalLines.subList(bodyStart, bodyEnd)

                    when {
                        decorator.contains("callback_query_handler") -> {
                            handlers.add(HandlerDef(HandlerKind.CALLBACK_QUERY, emptyList(), paramName, body))
                        }
                        decorator.contains("message_handler") -> {
                            val cmds = extractCommandsFromDecorator(decorator)
                            handlers.add(HandlerDef(HandlerKind.MESSAGE, cmds, paramName, body))
                        }
                    }
                    i = bodyEnd
                    continue
                }
            } else if (line.indent == 0 && !line.text.startsWith("#") &&
                !line.text.startsWith("import ") && !line.text.startsWith("from ") &&
                !line.text.startsWith("bot.") && line.text.contains("=")
            ) {
                executeAssignment(line.text, globalVars)
            }
            i++
        }
    }

    private fun joinMultilineStatements(rawLines: List<String>): List<IndentedLine> {
        val result = mutableListOf<IndentedLine>()
        var buffer = StringBuilder()
        var baseIndent = 0
        var parenBalance = 0

        for (raw in rawLines) {
            val withoutComment = stripInlineComment(raw)
            val trimmed = withoutComment.trim()
            if (trimmed.isEmpty()) continue

            val indent = raw.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
            if (parenBalance == 0) {
                baseIndent = indent
                buffer = StringBuilder(trimmed)
            } else {
                buffer.append(" ").append(trimmed)
            }
            parenBalance += countDeltaParens(trimmed)
            if (parenBalance <= 0) {
                parenBalance = 0
                result.add(IndentedLine(baseIndent, buffer.toString()))
            }
        }
        if (buffer.isNotEmpty() && parenBalance > 0) {
            result.add(IndentedLine(baseIndent, buffer.toString()))
        }
        return result
    }

    private fun stripInlineComment(line: String): String {
        var inSingle = false
        var inDouble = false
        var escaped = false
        for (idx in line.indices) {
            val c = line[idx]
            if (escaped) {
                escaped = false
                continue
            }
            if (c == '\\') {
                escaped = true
                continue
            }
            if (c == '\'' && !inDouble) inSingle = !inSingle
            else if (c == '"' && !inSingle) inDouble = !inDouble
            else if (c == '#' && !inSingle && !inDouble) {
                return line.substring(0, idx)
            }
        }
        return line
    }

    private fun countDeltaParens(text: String): Int {
        var delta = 0
        var inSingle = false
        var inDouble = false
        var escaped = false
        for (c in text) {
            if (escaped) {
                escaped = false
                continue
            }
            if (c == '\\') {
                escaped = true
                continue
            }
            if (c == '\'' && !inDouble) inSingle = !inSingle
            else if (c == '"' && !inSingle) inDouble = !inDouble
            else if (!inSingle && !inDouble) {
                when (c) {
                    '(', '[', '{' -> delta++
                    ')', ']', '}' -> delta--
                }
            }
        }
        return delta
    }

    private fun extractCommandsFromDecorator(decorator: String): List<String> {
        val match = Regex("""commands\s*=\s*\[([^\]]*)]""").find(decorator) ?: return emptyList()
        return Regex("""['"]([^'"]+)['"]""")
            .findAll(match.groupValues[1])
            .map { it.groupValues[1].trimStart('/').lowercase() }
            .toList()
    }

    fun processMessage(msg: TelegramIncomingMessage): List<BotAction> {
        val actions = mutableListOf<BotAction>()
        val trimmedText = msg.text.trim()
        val commandName = if (trimmedText.startsWith("/")) {
            trimmedText.substring(1).split(Regex("\\s+|@")).firstOrNull()?.lowercase()
        } else null

        val targetHandler = if (commandName != null) {
            handlers.firstOrNull { it.kind == HandlerKind.MESSAGE && commandName in it.commands }
                ?: handlers.firstOrNull { it.kind == HandlerKind.MESSAGE && it.commands.isEmpty() }
        } else {
            handlers.firstOrNull { it.kind == HandlerKind.MESSAGE && it.commands.isEmpty() }
        }

        if (targetHandler == null) {
            return actions
        }

        val scope = globalVars.toMutableMap()
        scope[targetHandler.paramName] = mapOf(
            "text" to msg.text,
            "message_id" to msg.messageId,
            "chat" to mapOf("id" to msg.chatId),
            "from_user" to mapOf(
                "id" to msg.userId,
                "first_name" to msg.firstName,
                "username" to msg.username
            )
        )

        try {
            executeBlock(targetHandler.bodyLines, scope, actions)
        } catch (_: ReturnSignal) {
        } catch (e: Exception) {
            actions.add(BotAction.LogInfo("Python ijro ogohlantirishi: ${e.message}"))
        }
        return actions
    }

    fun processCallback(cb: TelegramCallbackQuery): List<BotAction> {
        val actions = mutableListOf<BotAction>()
        val targetHandler = handlers.firstOrNull { it.kind == HandlerKind.CALLBACK_QUERY } ?: return actions

        val scope = globalVars.toMutableMap()
        scope[targetHandler.paramName] = mapOf(
            "id" to cb.id,
            "data" to cb.data,
            "message" to mapOf(
                "message_id" to cb.messageId,
                "chat" to mapOf("id" to cb.chatId)
            ),
            "from_user" to mapOf(
                "id" to cb.userId,
                "first_name" to cb.firstName,
                "username" to cb.username
            )
        )

        try {
            executeBlock(targetHandler.bodyLines, scope, actions)
        } catch (_: ReturnSignal) {
        } catch (e: Exception) {
            actions.add(BotAction.LogInfo("Callback ijro ogohlantirishi: ${e.message}"))
        }
        return actions
    }

    private fun executeBlock(
        lines: List<IndentedLine>,
        scope: MutableMap<String, Any?>,
        actions: MutableList<BotAction>
    ) {
        var idx = 0
        while (idx < lines.size) {
            val current = lines[idx]
            val text = current.text
            when {
                text == "return" || text.startsWith("return ") -> {
                    throw ReturnSignal()
                }
                text == "pass" -> {
                    idx++
                }
                text.startsWith("if ") && text.endsWith(":") -> {
                    // Collect if / elif / else chain at current.indent
                    val baseIndent = current.indent
                    var matchedBranch = false
                    var cursor = idx
                    while (cursor < lines.size) {
                        val header = lines[cursor]
                        if (header.indent != baseIndent) break
                        val hText = header.text
                        val isIf = cursor == idx && hText.startsWith("if ") && hText.endsWith(":")
                        val isElif = cursor > idx && hText.startsWith("elif ") && hText.endsWith(":")
                        val isElse = cursor > idx && hText == "else:"
                        if (!isIf && !isElif && !isElse) break

                        val blockStart = cursor + 1
                        var blockEnd = blockStart
                        while (blockEnd < lines.size && lines[blockEnd].indent > baseIndent) {
                            blockEnd++
                        }

                        if (!matchedBranch) {
                            val condResult = when {
                                isElse -> true
                                isIf -> evalCondition(hText.removePrefix("if ").removeSuffix(":").trim(), scope)
                                isElif -> evalCondition(hText.removePrefix("elif ").removeSuffix(":").trim(), scope)
                                else -> false
                            }
                            if (condResult) {
                                matchedBranch = true
                                executeBlock(lines.subList(blockStart, blockEnd), scope, actions)
                            }
                        }
                        cursor = blockEnd
                    }
                    idx = cursor
                }
                text.startsWith("for ") && text.endsWith(":") -> {
                    val match = Regex("""for\s+(\w+)\s+in\s+(.+):$""").find(text)
                    val baseIndent = current.indent
                    val blockStart = idx + 1
                    var blockEnd = blockStart
                    while (blockEnd < lines.size && lines[blockEnd].indent > baseIndent) {
                        blockEnd++
                    }
                    if (match != null) {
                        val varName = match.groupValues[1]
                        val iterableVal = evalExpr(match.groupValues[2].trim(), scope)
                        val items = when (iterableVal) {
                            is Iterable<*> -> iterableVal.toList()
                            is String -> iterableVal.map { it.toString() }
                            else -> emptyList()
                        }
                        for (item in items) {
                            scope[varName] = item
                            executeBlock(lines.subList(blockStart, blockEnd), scope, actions)
                        }
                    }
                    idx = blockEnd
                }
                else -> {
                    executeStatement(text, scope, actions)
                    idx++
                }
            }
        }
    }

    private fun executeStatement(
        stmt: String,
        scope: MutableMap<String, Any?>,
        actions: MutableList<BotAction>
    ) {
        when {
            stmt.startsWith("bot.send_message(") -> {
                val args = splitCallArgs(stmt.removePrefix("bot.send_message(").removeSuffix(")"))
                val chatId = (evalPositionalOrNamed(args, 0, "chat_id", scope) as? Number)?.toLong() ?: return
                val text = evalPositionalOrNamed(args, 1, "text", scope)?.toString().orEmpty()
                val markupObj = evalPositionalOrNamed(args, 2, "reply_markup", scope)
                val parseMode = evalNamedArg(args, "parse_mode", scope)?.toString()
                actions.add(
                    BotAction.SendMessage(
                        chatId = chatId,
                        text = text,
                        replyMarkupJson = toTelegramMarkup(markupObj),
                        parseMode = parseMode
                    )
                )
            }
            stmt.startsWith("bot.reply_to(") -> {
                val args = splitCallArgs(stmt.removePrefix("bot.reply_to(").removeSuffix(")"))
                val msgMap = evalPositionalOrNamed(args, 0, "message", scope) as? Map<*, *>
                val chatMap = msgMap?.get("chat") as? Map<*, *>
                val chatId = (chatMap?.get("id") as? Number)?.toLong() ?: return
                val msgId = (msgMap["message_id"] as? Number)?.toLong()
                val text = evalPositionalOrNamed(args, 1, "text", scope)?.toString().orEmpty()
                val markupObj = evalPositionalOrNamed(args, 2, "reply_markup", scope)
                val parseMode = evalNamedArg(args, "parse_mode", scope)?.toString()
                actions.add(
                    BotAction.SendMessage(
                        chatId = chatId,
                        text = text,
                        replyToMessageId = msgId,
                        replyMarkupJson = toTelegramMarkup(markupObj),
                        parseMode = parseMode
                    )
                )
            }
            stmt.startsWith("bot.answer_callback_query(") -> {
                val args = splitCallArgs(stmt.removePrefix("bot.answer_callback_query(").removeSuffix(")"))
                val cbId = evalPositionalOrNamed(args, 0, "callback_query_id", scope)?.toString() ?: return
                val text = evalPositionalOrNamed(args, 1, "text", scope)?.toString()
                actions.add(BotAction.AnswerCallback(cbId, text))
            }
            stmt.startsWith("bot.delete_message(") -> {
                val args = splitCallArgs(stmt.removePrefix("bot.delete_message(").removeSuffix(")"))
                val chatId = (evalPositionalOrNamed(args, 0, "chat_id", scope) as? Number)?.toLong() ?: return
                val msgId = (evalPositionalOrNamed(args, 1, "message_id", scope) as? Number)?.toLong() ?: return
                actions.add(BotAction.DeleteMessage(chatId, msgId))
            }
            Regex("""^(\w+)\.add\((.*)\)$""").matches(stmt) -> {
                val m = Regex("""^(\w+)\.add\((.*)\)$""").find(stmt)!!
                val target = scope[m.groupValues[1]]
                val args = splitCallArgs(m.groupValues[2])
                val evaluated = args.mapNotNull { evalExpr(it, scope) }
                when (target) {
                    is InlineMarkupBuilder -> {
                        @Suppress("UNCHECKED_CAST")
                        val buttons = evaluated.filterIsInstance<Map<String, String>>()
                        if (buttons.isNotEmpty()) target.rows.add(buttons)
                    }
                    is ReplyMarkupBuilder -> {
                        val buttons = evaluated.map { item ->
                            @Suppress("UNCHECKED_CAST")
                            when (item) {
                                is Map<*, *> -> item as Map<String, String>
                                else -> mapOf("text" to item.toString())
                            }
                        }
                        if (buttons.isNotEmpty()) target.rows.add(buttons)
                    }
                }
            }
            stmt.contains("=") && !stmt.startsWith("==") -> {
                executeAssignment(stmt, scope)
            }
        }
    }

    private fun executeAssignment(stmt: String, scope: MutableMap<String, Any?>) {
        val eqIdx = findAssignmentEquals(stmt) ?: return
        val lhs = stmt.substring(0, eqIdx).trim()
        val rhs = stmt.substring(eqIdx + 1).trim()
        if (lhs.matches(Regex("""\w+"""))) {
            scope[lhs] = evalExpr(rhs, scope)
        }
    }

    private fun findAssignmentEquals(stmt: String): Int? {
        var inSingle = false
        var inDouble = false
        var depth = 0
        for (i in stmt.indices) {
            val c = stmt[i]
            if (c == '\'' && !inDouble) inSingle = !inSingle
            else if (c == '"' && !inSingle) inDouble = !inDouble
            else if (!inSingle && !inDouble) {
                when (c) {
                    '(', '[', '{' -> depth++
                    ')', ']', '}' -> depth--
                    '=' -> if (depth == 0) {
                        val prev = stmt.getOrNull(i - 1)
                        val next = stmt.getOrNull(i + 1)
                        if (prev !in listOf('=', '!', '<', '>') && next != '=') {
                            return i
                        }
                    }
                }
            }
        }
        return null
    }

    private fun evalCondition(expr: String, scope: MutableMap<String, Any?>): Boolean {
        val clean = expr.trim()
        // Handle 'or' / 'and' at top level
        val orParts = splitTopLevelKeyword(clean, "or")
        if (orParts.size > 1) return orParts.any { evalCondition(it, scope) }

        val andParts = splitTopLevelKeyword(clean, "and")
        if (andParts.size > 1) return andParts.all { evalCondition(it, scope) }

        if (clean.startsWith("not ")) {
            return !evalCondition(clean.removePrefix("not ").trim(), scope)
        }

        // ' in '
        val inParts = splitTopLevelKeyword(clean, "in")
        if (inParts.size == 2) {
            val left = evalExpr(inParts[0], scope)
            val right = evalExpr(inParts[1], scope)
            return when (right) {
                is String -> right.contains(left?.toString().orEmpty())
                is Collection<*> -> right.any { it.toString() == left?.toString() }
                else -> false
            }
        }

        // Comparisons: ==, !=, >=, <=, >, <
        for (op in listOf("==", "!=", ">=", "<=", ">", "<")) {
            val idx = findTopLevelOperator(clean, op)
            if (idx >= 0) {
                val left = evalExpr(clean.substring(0, idx), scope)
                val right = evalExpr(clean.substring(idx + op.length), scope)
                return compareValues(left, right, op)
            }
        }

        val evaluated = evalExpr(clean, scope)
        return when (evaluated) {
            is Boolean -> evaluated
            is Number -> evaluated.toDouble() != 0.0
            is String -> evaluated.isNotEmpty()
            is Collection<*> -> evaluated.isNotEmpty()
            else -> evaluated != null
        }
    }

    private fun compareValues(left: Any?, right: Any?, op: String): Boolean {
        if (left is Number && right is Number) {
            val a = left.toDouble()
            val b = right.toDouble()
            return when (op) {
                "==" -> a == b
                "!=" -> a != b
                ">" -> a > b
                "<" -> a < b
                ">=" -> a >= b
                "<=" -> a <= b
                else -> false
            }
        }
        val a = left?.toString()
        val b = right?.toString()
        return when (op) {
            "==" -> a == b
            "!=" -> a != b
            else -> false
        }
    }

    private fun evalExpr(raw: String, scope: MutableMap<String, Any?>): Any? {
        val expr = raw.trim()
        if (expr.isEmpty()) return ""

        // String concatenation with '+' outside quotes/brackets
        val plusParts = splitTopLevelChar(expr, '+')
        if (plusParts.size > 1) {
            val vals = plusParts.map { evalExpr(it, scope) }
            return if (vals.all { it is Number }) {
                vals.sumOf { (it as Number).toLong() }
            } else {
                vals.joinToString("") { it?.toString().orEmpty() }
            }
        }

        // f-string: f"..." or f'...'
        if ((expr.startsWith("f\"") && expr.endsWith("\"")) ||
            (expr.startsWith("f'") && expr.endsWith("'"))
        ) {
            val inner = expr.substring(2, expr.length - 1)
            val resolved = Regex("""\{([^}]+)}""").replace(inner) { m ->
                evalExpr(m.groupValues[1], scope)?.toString().orEmpty()
            }
            return unescapePythonString(resolved)
        }

        // Normal quoted string
        if ((expr.startsWith("\"") && expr.endsWith("\"")) ||
            (expr.startsWith("'") && expr.endsWith("'"))
        ) {
            return unescapePythonString(expr.substring(1, expr.length - 1))
        }

        // Booleans / None
        when (expr) {
            "True" -> return true
            "False" -> return false
            "None" -> return null
        }

        // Numbers
        expr.toLongOrNull()?.let { return it }
        expr.toDoubleOrNull()?.let { return it }

        // List literal [...]
        if (expr.startsWith("[") && expr.endsWith("]")) {
            val inner = expr.substring(1, expr.length - 1).trim()
            if (inner.isEmpty()) return emptyList<Any?>()
            return splitCallArgs(inner).map { evalExpr(it, scope) }
        }

        // Special Python Telegram / stdlib constructors & calls
        when {
            expr.startsWith("types.InlineKeyboardMarkup(") -> return InlineMarkupBuilder()
            expr.startsWith("types.ReplyKeyboardMarkup(") -> return ReplyMarkupBuilder()
            expr.startsWith("types.InlineKeyboardButton(") -> {
                val args = splitCallArgs(expr.removePrefix("types.InlineKeyboardButton(").removeSuffix(")"))
                val text = evalPositionalOrNamed(args, 0, "text", scope)?.toString().orEmpty()
                val cbData = evalNamedArg(args, "callback_data", scope)?.toString()
                val url = evalNamedArg(args, "url", scope)?.toString()
                return buildMap {
                    put("text", text)
                    if (!cbData.isNullOrBlank()) put("callback_data", cbData)
                    if (!url.isNullOrBlank()) put("url", url)
                }
            }
            expr.startsWith("types.KeyboardButton(") -> {
                val args = splitCallArgs(expr.removePrefix("types.KeyboardButton(").removeSuffix(")"))
                val text = evalPositionalOrNamed(args, 0, "text", scope)?.toString().orEmpty()
                return mapOf("text" to text)
            }
            expr.startsWith("datetime.datetime.now().strftime(") || expr.startsWith("datetime.now().strftime(") -> {
                val fmt = expr.substringAfter("strftime(").removeSuffix(")").trim().trim('"', '\'')
                val javaFmt = fmt
                    .replace("%Y", "yyyy")
                    .replace("%m", "MM")
                    .replace("%d", "dd")
                    .replace("%H", "HH")
                    .replace("%M", "mm")
                    .replace("%S", "ss")
                return SimpleDateFormat(javaFmt, Locale.getDefault()).format(Date())
            }
            expr.startsWith("random.choice(") -> {
                val arg = evalExpr(expr.removePrefix("random.choice(").removeSuffix(")"), scope)
                val list = (arg as? Collection<*>)?.toList().orEmpty()
                return if (list.isNotEmpty()) list[Random.nextInt(list.size)] else ""
            }
            expr.startsWith("random.randint(") -> {
                val args = splitCallArgs(expr.removePrefix("random.randint(").removeSuffix(")"))
                val min = (evalExpr(args.getOrElse(0) { "0" }, scope) as? Number)?.toInt() ?: 0
                val max = (evalExpr(args.getOrElse(1) { "100" }, scope) as? Number)?.toInt() ?: 100
                return Random.nextInt(min, max + 1)
            }
            expr.startsWith("len(") && expr.endsWith(")") -> {
                val inner = evalExpr(expr.removePrefix("len(").removeSuffix(")"), scope)
                return when (inner) {
                    is Collection<*> -> inner.size
                    is String -> inner.length
                    else -> 0
                }
            }
            expr.startsWith("str(") && expr.endsWith(")") -> {
                return evalExpr(expr.removePrefix("str(").removeSuffix(")"), scope)?.toString().orEmpty()
            }
        }

        // Method chain on string/object e.g. text.lower() or message.from_user.first_name
        if (expr.endsWith(".lower()")) {
            return evalExpr(expr.removeSuffix(".lower()"), scope)?.toString()?.lowercase().orEmpty()
        }
        if (expr.endsWith(".upper()")) {
            return evalExpr(expr.removeSuffix(".upper()"), scope)?.toString()?.uppercase().orEmpty()
        }
        if (expr.endsWith(".strip()")) {
            return evalExpr(expr.removeSuffix(".strip()"), scope)?.toString()?.trim().orEmpty()
        }
        if (expr.contains(".startswith(") && expr.endsWith(")")) {
            val target = evalExpr(expr.substringBefore(".startswith("), scope)?.toString().orEmpty()
            val arg = evalExpr(expr.substringAfter(".startswith(").removeSuffix(")"), scope)?.toString().orEmpty()
            return target.startsWith(arg)
        }
        if (expr.contains(".endswith(") && expr.endsWith(")")) {
            val target = evalExpr(expr.substringBefore(".endswith("), scope)?.toString().orEmpty()
            val arg = evalExpr(expr.substringAfter(".endswith(").removeSuffix(")"), scope)?.toString().orEmpty()
            return target.endsWith(arg)
        }

        // Dotted attribute access e.g. message.from_user.first_name
        if (expr.contains(".") && !expr.contains("(")) {
            val parts = expr.split(".")
            var current: Any? = scope[parts[0]]
            for (p in parts.drop(1)) {
                current = (current as? Map<*, *>)?.get(p)
            }
            return current
        }

        // Direct variable lookup
        if (scope.containsKey(expr)) {
            return scope[expr]
        }

        return expr
    }

    private fun unescapePythonString(s: String): String {
        return s.replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\'", "'")
    }

    private fun splitCallArgs(raw: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var depth = 0
        var inSingle = false
        var inDouble = false
        var escaped = false

        for (c in raw) {
            if (escaped) {
                current.append(c)
                escaped = false
                continue
            }
            if (c == '\\') {
                current.append(c)
                escaped = true
                continue
            }
            if (c == '\'' && !inDouble) inSingle = !inSingle
            else if (c == '"' && !inSingle) inDouble = !inDouble
            else if (!inSingle && !inDouble) {
                when (c) {
                    '(', '[', '{' -> depth++
                    ')', ']', '}' -> depth--
                    ',' -> if (depth == 0) {
                        val part = current.toString().trim()
                        if (part.isNotEmpty()) result.add(part)
                        current = StringBuilder()
                        continue
                    }
                }
            }
            current.append(c)
        }
        val tail = current.toString().trim()
        if (tail.isNotEmpty()) result.add(tail)
        return result
    }

    private fun evalPositionalOrNamed(
        args: List<String>,
        index: Int,
        name: String,
        scope: MutableMap<String, Any?>
    ): Any? {
        val named = evalNamedArg(args, name, scope)
        if (named != null) return named
        val pos = args.getOrNull(index) ?: return null
        if (findAssignmentEquals(pos) != null) return null
        return evalExpr(pos, scope)
    }

    private fun evalNamedArg(
        args: List<String>,
        name: String,
        scope: MutableMap<String, Any?>
    ): Any? {
        for (arg in args) {
            val eqIdx = findAssignmentEquals(arg) ?: continue
            val key = arg.substring(0, eqIdx).trim()
            if (key == name) {
                return evalExpr(arg.substring(eqIdx + 1), scope)
            }
        }
        return null
    }

    private fun toTelegramMarkup(obj: Any?): String? {
        return when (obj) {
            is InlineMarkupBuilder -> {
                if (obj.rows.isEmpty()) return null
                val rowsJson = obj.rows.joinToString(",") { row ->
                    "[" + row.joinToString(",") { mapToJson(it) } + "]"
                }
                """{"inline_keyboard":[$rowsJson]}"""
            }
            is ReplyMarkupBuilder -> {
                if (obj.rows.isEmpty()) return null
                val rowsJson = obj.rows.joinToString(",") { row ->
                    "[" + row.joinToString(",") { mapToJson(it) } + "]"
                }
                """{"keyboard":[$rowsJson],"resize_keyboard":${obj.resizeKeyboard}}"""
            }
            else -> null
        }
    }

    private fun mapToJson(map: Map<String, String>): String {
        val entries = map.entries.joinToString(",") { (k, v) ->
            """"${escapeJson(k)}":"${escapeJson(v)}""""
        }
        return "{$entries}"
    }

    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun splitTopLevelKeyword(expr: String, keyword: String): List<String> {
        val parts = mutableListOf<String>()
        var last = 0
        var depth = 0
        var inSingle = false
        var inDouble = false
        var i = 0
        val token = " $keyword "
        while (i <= expr.length - token.length) {
            val c = expr[i]
            if (c == '\'' && !inDouble) inSingle = !inSingle
            else if (c == '"' && !inSingle) inDouble = !inDouble
            else if (!inSingle && !inDouble) {
                when (c) {
                    '(', '[', '{' -> depth++
                    ')', ']', '}' -> depth--
                }
                if (depth == 0 && expr.regionMatches(i, token, 0, token.length)) {
                    parts.add(expr.substring(last, i).trim())
                    i += token.length
                    last = i
                    continue
                }
            }
            i++
        }
        parts.add(expr.substring(last).trim())
        return parts
    }

    private fun splitTopLevelChar(expr: String, sep: Char): List<String> {
        val parts = mutableListOf<String>()
        var last = 0
        var depth = 0
        var inSingle = false
        var inDouble = false
        var escaped = false
        for (i in expr.indices) {
            val c = expr[i]
            if (escaped) {
                escaped = false
                continue
            }
            if (c == '\\') {
                escaped = true
                continue
            }
            if (c == '\'' && !inDouble) inSingle = !inSingle
            else if (c == '"' && !inSingle) inDouble = !inDouble
            else if (!inSingle && !inDouble) {
                when (c) {
                    '(', '[', '{' -> depth++
                    ')', ']', '}' -> depth--
                    sep -> if (depth == 0) {
                        parts.add(expr.substring(last, i).trim())
                        last = i + 1
                    }
                }
            }
        }
        parts.add(expr.substring(last).trim())
        return parts
    }

    private fun findTopLevelOperator(expr: String, op: String): Int {
        var depth = 0
        var inSingle = false
        var inDouble = false
        for (i in 0..(expr.length - op.length)) {
            val c = expr[i]
            if (c == '\'' && !inDouble) inSingle = !inSingle
            else if (c == '"' && !inSingle) inDouble = !inDouble
            else if (!inSingle && !inDouble) {
                when (c) {
                    '(', '[', '{' -> depth++
                    ')', ']', '}' -> depth--
                }
                if (depth == 0 && expr.regionMatches(i, op, 0, op.length)) {
                    return i
                }
            }
        }
        return -1
    }
}
