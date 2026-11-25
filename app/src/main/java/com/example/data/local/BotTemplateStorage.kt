package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import com.example.R
import com.example.domain.model.BotTemplate
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Manages user-uploaded custom bot packages containing:
 * - bot.py
 * - malumot.json
 * - icon.png
 *
 * Scans both internal app storage (filesDir/custom_bots) and app external storage
 * (getExternalFilesDir("bots")) so bots placed or imported appear automatically
 * in the BOT OCHISH catalog.
 */
class BotTemplateStorage(private val context: Context) {

    val internalBotsDir: File
        get() = File(context.filesDir, "custom_bots").apply { mkdirs() }

    val externalBotsDir: File?
        get() = context.getExternalFilesDir("bots")?.apply { mkdirs() }

    fun loadAllCustomTemplates(): List<BotTemplate> {
        val result = mutableListOf<BotTemplate>()
        val seenIds = mutableSetOf<String>()

        listOfNotNull(internalBotsDir, externalBotsDir).forEach { root ->
            val dirs = root.listFiles()?.filter { it.isDirectory } ?: emptyList()
            dirs.sortedByDescending { it.lastModified() }.forEach { folder ->
                val template = parseBotFolder(folder)
                if (template != null && seenIds.add(template.id)) {
                    result.add(template)
                }
            }
        }
        return result
    }

    private fun parseBotFolder(folder: File): BotTemplate? {
        val botPyFile = folder.listFiles()?.firstOrNull {
            it.isFile && it.name.equals("bot.py", ignoreCase = true)
        } ?: return null

        val infoJsonFile = folder.listFiles()?.firstOrNull {
            it.isFile && (it.name.equals("malumot.json", ignoreCase = true) ||
                it.name.equals("info.json", ignoreCase = true))
        }

        val iconFile = folder.listFiles()?.firstOrNull {
            it.isFile && (it.name.equals("icon.png", ignoreCase = true) ||
                it.name.equals("icon.jpg", ignoreCase = true) ||
                it.name.equals("icon.jpeg", ignoreCase = true) ||
                it.name.equals("icon.webp", ignoreCase = true))
        }

        val pythonCode = runCatching { botPyFile.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        val rawJson = runCatching { infoJsonFile?.readText(Charsets.UTF_8) }.getOrNull().orEmpty()

        return buildTemplateFromContent(
            id = "custom_${folder.name.lowercase().replace(Regex("[^a-z0-9_]"), "_")}",
            folderName = folder.name,
            pythonCode = pythonCode,
            rawJson = rawJson,
            iconFilePath = iconFile?.absolutePath,
            sourceFolderPath = folder.absolutePath
        )
    }

    fun saveCustomBotFromContent(
        folderSlug: String,
        pythonCode: String,
        infoJson: String,
        iconBytes: ByteArray?
    ): BotTemplate {
        val safeSlug = folderSlug.lowercase()
            .replace(Regex("[^a-z0-9_]+"), "_")
            .trim('_')
            .ifEmpty { "bot_${System.currentTimeMillis()}" }
        val targetDir = File(internalBotsDir, safeSlug).apply { mkdirs() }

        File(targetDir, "bot.py").writeText(pythonCode, Charsets.UTF_8)
        File(targetDir, "malumot.json").writeText(infoJson, Charsets.UTF_8)
        if (iconBytes != null && iconBytes.isNotEmpty()) {
            File(targetDir, "icon.png").writeBytes(iconBytes)
        }

        return parseBotFolder(targetDir)
            ?: buildTemplateFromContent(
                id = "custom_$safeSlug",
                folderName = safeSlug,
                pythonCode = pythonCode,
                rawJson = infoJson,
                iconFilePath = File(targetDir, "icon.png").takeIf { it.exists() }?.absolutePath,
                sourceFolderPath = targetDir.absolutePath
            )
    }

    fun saveCustomBotFromUris(
        botPyUri: Uri,
        malumotJsonUri: Uri?,
        iconUri: Uri?,
        fallbackName: String = "Yangi Bot"
    ): Result<BotTemplate> = runCatching {
        val pyBytes = readBytesFromUri(botPyUri)
            ?: error("bot.py faylini o'qib bo'lmadi")
        val pyText = pyBytes.toString(Charsets.UTF_8)
        require(pyText.isNotBlank()) { "bot.py fayli bo'sh bo'lmasligi kerak" }

        val jsonText = malumotJsonUri?.let { readBytesFromUri(it)?.toString(Charsets.UTF_8) }
            ?: defaultMalumotJson(fallbackName)

        val iconBytes = iconUri?.let { readBytesFromUri(it) }
        val parsedName = extractNameFromJson(jsonText) ?: fallbackName
        val slug = "${parsedName}_${System.currentTimeMillis() % 10000}"

        saveCustomBotFromContent(slug, pyText, jsonText, iconBytes)
    }

    fun importFromFolderTree(treeUri: Uri): Result<BotTemplate> = runCatching {
        val resolver = context.contentResolver
        val docId = android.provider.DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)

        var botPyUri: Uri? = null
        var malumotUri: Uri? = null
        var iconUri: Uri? = null

        resolver.query(
            childrenUri,
            arrayOf(
                android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndex(android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIdx = cursor.getColumnIndex(android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val childId = cursor.getString(idIdx)
                val name = cursor.getString(nameIdx)?.lowercase() ?: continue
                val fileUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, childId)
                when (name) {
                    "bot.py" -> botPyUri = fileUri
                    "malumot.json", "info.json" -> malumotUri = fileUri
                    "icon.png", "icon.jpg", "icon.jpeg", "icon.webp" -> iconUri = fileUri
                }
            }
        }

        val foundPy = botPyUri ?: error("Tanlangan papkada bot.py fayli topilmadi")
        saveCustomBotFromUris(foundPy, malumotUri, iconUri, "Yuklangan Bot").getOrThrow()
    }

    fun exportTestSampleToExternalBotsDir(): Result<File> = runCatching {
        val base = externalBotsDir ?: internalBotsDir
        val sampleDir = File(base, "test_bot_namuna").apply { mkdirs() }
        File(sampleDir, "bot.py").writeText(BuiltInTemplates.testBotPythonCode, Charsets.UTF_8)
        File(sampleDir, "malumot.json").writeText(BuiltInTemplates.testBotInfoJson, Charsets.UTF_8)

        val iconFile = File(sampleDir, "icon.png")
        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_bot_test)
        FileOutputStream(iconFile).use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        sampleDir
    }

    fun deleteCustomBot(template: BotTemplate) {
        val path = template.sourceFolderPath ?: return
        val dir = File(path)
        if (dir.exists() && dir.isDirectory) {
            dir.deleteRecursively()
        }
    }

    fun getDisplayFileName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return c.getString(idx) ?: "fayl"
            }
        }
        return uri.lastPathSegment ?: "fayl"
    }

    private fun readBytesFromUri(uri: Uri): ByteArray? {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull()
    }

    private fun extractNameFromJson(rawJson: String): String? {
        return runCatching {
            val obj = JSONObject(rawJson)
            obj.optString("name").ifBlank { obj.optString("nomi") }.ifBlank { null }
        }.getOrNull()
    }

    private fun defaultMalumotJson(name: String): String = """
        {
          "name": "$name",
          "tagline": "Foydalanuvchi yuklagan Python Telegram bot",
          "description": "Fayllar bo'limi orqali yuklangan maxsus Python Telegram boti.",
          "price": 0,
          "category": "Maxsus Bot",
          "version": "1.0",
          "author": "Foydalanuvchi",
          "commands": ["/start - Botni ishga tushirish"],
          "features": ["24/7 fon rejimida ishlaydi", "Maxsus bot.py kodi"]
        }
    """.trimIndent()

    private fun buildTemplateFromContent(
        id: String,
        folderName: String,
        pythonCode: String,
        rawJson: String,
        iconFilePath: String?,
        sourceFolderPath: String
    ): BotTemplate {
        val jsonObj = runCatching { JSONObject(rawJson) }.getOrNull() ?: JSONObject()

        val name = jsonObj.optString("name")
            .ifBlank { jsonObj.optString("nomi") }
            .ifBlank { folderName.replace("_", " ").replaceFirstChar { it.uppercase() } }

        val tagline = jsonObj.optString("tagline")
            .ifBlank { jsonObj.optString("qisqa_tavsif") }
            .ifBlank { "Maxsus Python Telegram Bot" }

        val description = jsonObj.optString("description")
            .ifBlank { jsonObj.optString("malumot") }
            .ifBlank { jsonObj.optString("tavsif") }
            .ifBlank { "Foydalanuvchi tomonidan bot.py va malumot.json orqali yuklangan maxsus Telegram bot." }

        val price = when {
            jsonObj.has("price") -> jsonObj.optLong("price", 0L)
            jsonObj.has("narxi") -> jsonObj.optLong("narxi", 0L)
            else -> 0L
        }.coerceAtLeast(0L)

        val monthlyFee = jsonObj.optLong("monthly_fee", 0L).coerceAtLeast(0L)

        val category = jsonObj.optString("category")
            .ifBlank { jsonObj.optString("kategoriya") }
            .ifBlank { "Yuklangan Bot" }

        val version = jsonObj.optString("version")
            .ifBlank { jsonObj.optString("versiya") }
            .ifBlank { "1.0" }

        val author = jsonObj.optString("author")
            .ifBlank { jsonObj.optString("muallif") }
            .ifBlank { "Foydalanuvchi" }

        val commands = parseStringList(jsonObj.optJSONArray("commands") ?: jsonObj.optJSONArray("buyruqlar"))
            .ifEmpty { listOf("/start - Botni ishga tushirish") }

        val features = parseStringList(jsonObj.optJSONArray("features") ?: jsonObj.optJSONArray("imkoniyatlar"))
            .ifEmpty { listOf("24/7 fon rejimida ishlaydi", "Maxsus Python (bot.py) logikasi") }

        return BotTemplate(
            id = id,
            name = name,
            shortTagline = tagline,
            description = description,
            priceSom = price,
            monthlyServerFeeSom = monthlyFee,
            category = category,
            version = version,
            author = author,
            commands = commands,
            features = features,
            pythonCode = pythonCode,
            infoJson = rawJson.ifBlank { defaultMalumotJson(name) },
            drawableResName = if (iconFilePath == null) "img_bot_test" else null,
            iconFilePath = iconFilePath,
            isCustomImported = true,
            sourceFolderPath = sourceFolderPath,
            uptimeSla = "99.9%",
            speedScore = 95,
            reliabilityScore = 95,
            popularityScore = 88
        )
    }

    private fun parseStringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optString(i)
                if (item.isNotBlank()) add(item)
            }
        }
    }
}
