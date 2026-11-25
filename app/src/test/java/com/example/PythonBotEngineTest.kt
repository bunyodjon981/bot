package com.example

import com.example.data.local.BuiltInTemplates
import com.example.engine.BotAction
import com.example.engine.PythonBotEngine
import com.example.engine.TelegramCallbackQuery
import com.example.engine.TelegramIncomingMessage
import org.junit.Assert.assertTrue
import org.junit.Test

class PythonBotEngineTest {

    @Test
    fun testBuiltInTestBotStartAndEcho() {
        val engine = PythonBotEngine(BuiltInTemplates.testBotPythonCode)

        val startActions = engine.processMessage(
            TelegramIncomingMessage(
                messageId = 1L,
                chatId = 555L,
                text = "/start",
                userId = 10L,
                firstName = "Ali",
                username = "ali_uz"
            )
        )
        val startMsg = startActions.filterIsInstance<BotAction.SendMessage>().firstOrNull()
        assertTrue(startMsg != null)
        assertTrue(startMsg!!.text.contains("Assalomu alaykum, Ali"))
        assertTrue("Expected replyMarkupJson, got actions=$startActions", startMsg.replyMarkupJson?.contains("inline_keyboard") == true)

        val salomActions = engine.processMessage(
            TelegramIncomingMessage(
                messageId = 2L,
                chatId = 555L,
                text = "Salom bot",
                userId = 10L,
                firstName = "Ali",
                username = "ali_uz"
            )
        )
        val salomMsg = salomActions.filterIsInstance<BotAction.SendMessage>().firstOrNull()
        assertTrue(salomMsg != null)
        assertTrue(salomMsg!!.text.contains("Vaalaykum assalom, Ali"))

        val cbActions = engine.processCallback(
            TelegramCallbackQuery(
                id = "cb1",
                data = "about_bot",
                chatId = 555L,
                messageId = 1L,
                userId = 10L,
                firstName = "Ali",
                username = "ali_uz"
            )
        )
        assertTrue(cbActions.any { it is BotAction.AnswerCallback })
        assertTrue(cbActions.any { it is BotAction.SendMessage && it.text.contains("bot.py") })
    }

    @Test
    fun testAdminBotDeletesBadLinks() {
        val adminTemplate = BuiltInTemplates.templates.first { it.id == "builtin_admin_bot" }
        val engine = PythonBotEngine(adminTemplate.pythonCode)

        val actions = engine.processMessage(
            TelegramIncomingMessage(
                messageId = 99L,
                chatId = 777L,
                text = "Kanalga kiring https://t.me/spam",
                userId = 22L,
                firstName = "Spammer",
                username = "spam"
            )
        )
        assertTrue(actions.any { it is BotAction.DeleteMessage && it.messageId == 99L })
        assertTrue(actions.any { it is BotAction.SendMessage && it.text.contains("taqiqlangan") })
    }
}
