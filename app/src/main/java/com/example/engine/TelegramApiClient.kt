package com.example.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TelegramBotInfo(
    val id: Long,
    val firstName: String,
    val username: String
)

data class TelegramIncomingMessage(
    val messageId: Long,
    val chatId: Long,
    val text: String,
    val userId: Long,
    val firstName: String,
    val username: String
)

data class TelegramCallbackQuery(
    val id: String,
    val data: String,
    val chatId: Long,
    val messageId: Long,
    val userId: Long,
    val firstName: String,
    val username: String
)

data class TelegramUpdate(
    val updateId: Long,
    val message: TelegramIncomingMessage? = null,
    val callbackQuery: TelegramCallbackQuery? = null
)

class TelegramApiClient {

    private val shortClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val pollingClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun verifyToken(token: String): Result<TelegramBotInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val clean = token.trim()
            require(clean.contains(":") && clean.length > 20) {
                "Token formati noto'g'ri. Misol: 123456789:ABCdef..."
            }
            val request = Request.Builder()
                .url("https://api.telegram.org/bot$clean/getMe")
                .get()
                .build()

            shortClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                if (!json.optBoolean("ok", false)) {
                    val desc = json.optString("description", "Telegram token rad etildi")
                    error(desc)
                }
                val res = json.getJSONObject("result")
                TelegramBotInfo(
                    id = res.optLong("id"),
                    firstName = res.optString("first_name", "Telegram Bot"),
                    username = res.optString("username", "unknown_bot")
                )
            }
        }
    }

    suspend fun getUpdates(
        token: String,
        offset: Long,
        timeoutSeconds: Int = 25
    ): Result<List<TelegramUpdate>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = buildString {
                append("https://api.telegram.org/bot")
                append(token.trim())
                append("/getUpdates?timeout=")
                append(timeoutSeconds)
                if (offset > 0) {
                    append("&offset=").append(offset)
                }
                append("&allowed_updates=%5B%22message%22%2C%22callback_query%22%5D")
            }

            val request = Request.Builder().url(url).get().build()
            pollingClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                if (!json.optBoolean("ok", false)) {
                    val desc = json.optString("description", "HTTP ${response.code}")
                    error(desc)
                }
                val arr = json.optJSONArray("result") ?: JSONArray()
                buildList {
                    for (i in 0 until arr.length()) {
                        val upd = arr.getJSONObject(i)
                        val updateId = upd.optLong("update_id")
                        val msgObj = upd.optJSONObject("message")
                        val cbObj = upd.optJSONObject("callback_query")

                        val parsedMsg = msgObj?.let { m ->
                            val chat = m.optJSONObject("chat")
                            val from = m.optJSONObject("from")
                            val text = m.optString("text", m.optString("caption", ""))
                            if (chat != null) {
                                TelegramIncomingMessage(
                                    messageId = m.optLong("message_id"),
                                    chatId = chat.optLong("id"),
                                    text = text,
                                    userId = from?.optLong("id") ?: chat.optLong("id"),
                                    firstName = from?.optString("first_name", "Foydalanuvchi") ?: "Foydalanuvchi",
                                    username = from?.optString("username", "") ?: ""
                                )
                            } else null
                        }

                        val parsedCb = cbObj?.let { c ->
                            val from = c.optJSONObject("from")
                            val msg = c.optJSONObject("message")
                            val chat = msg?.optJSONObject("chat")
                            if (chat != null) {
                                TelegramCallbackQuery(
                                    id = c.optString("id"),
                                    data = c.optString("data"),
                                    chatId = chat.optLong("id"),
                                    messageId = msg.optLong("message_id"),
                                    userId = from?.optLong("id") ?: chat.optLong("id"),
                                    firstName = from?.optString("first_name", "Foydalanuvchi") ?: "Foydalanuvchi",
                                    username = from?.optString("username", "") ?: ""
                                )
                            } else null
                        }

                        add(TelegramUpdate(updateId = updateId, message = parsedMsg, callbackQuery = parsedCb))
                    }
                }
            }
        }
    }

    suspend fun sendMessage(
        token: String,
        chatId: Long,
        text: String,
        replyToMessageId: Long? = null,
        replyMarkup: JSONObject? = null,
        parseMode: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
                if (replyToMessageId != null && replyToMessageId > 0) {
                    put("reply_to_message_id", replyToMessageId)
                }
                if (replyMarkup != null) {
                    put("reply_markup", replyMarkup)
                }
                if (!parseMode.isNullOrBlank()) {
                    put("parse_mode", parseMode)
                }
            }
            val request = Request.Builder()
                .url("https://api.telegram.org/bot${token.trim()}/sendMessage")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            shortClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                if (!json.optBoolean("ok", false)) {
                    error(json.optString("description", "Xabar yuborilmadi"))
                }
            }
        }
    }

    suspend fun answerCallbackQuery(
        token: String,
        callbackQueryId: String,
        text: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject().apply {
                put("callback_query_id", callbackQueryId)
                if (!text.isNullOrBlank()) {
                    put("text", text)
                }
            }
            val request = Request.Builder()
                .url("https://api.telegram.org/bot${token.trim()}/answerCallbackQuery")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            shortClient.newCall(request).execute().use { response ->
                response.body?.close()
            }
            Unit
        }
    }

    suspend fun deleteMessage(
        token: String,
        chatId: Long,
        messageId: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject().apply {
                put("chat_id", chatId)
                put("message_id", messageId)
            }
            val request = Request.Builder()
                .url("https://api.telegram.org/bot${token.trim()}/deleteMessage")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            shortClient.newCall(request).execute().use { response ->
                response.body?.close()
            }
            Unit
        }
    }
}
