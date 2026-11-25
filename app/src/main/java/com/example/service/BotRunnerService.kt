package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.BotDao
import com.example.data.local.BotInstanceEntity
import com.example.data.local.BotLogEntity
import com.example.data.local.WalletPreferences
import com.example.engine.BotAction
import com.example.engine.PythonBotEngine
import com.example.engine.TelegramApiClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BotRunnerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var botDao: BotDao
    private lateinit var walletPrefs: WalletPreferences
    private val apiClient = TelegramApiClient()
    private val activeJobs = mutableMapOf<String, Job>()
    private val activeCodeSnapshots = mutableMapOf<String, String>()
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        botDao = AppDatabase.getInstance(applicationContext).botDao()
        walletPrefs = WalletPreferences(applicationContext)
        createNotificationChannel()
        startForegroundWithCount(0)

        serviceScope.launch {
            val useWakeLock = walletPrefs.keepWakeLock.first()
            if (useWakeLock) {
                acquireWakeLock()
            }
            botDao.observeRunningBots().collect { runningBots ->
                syncRunningBots(runningBots)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALL) {
            serviceScope.launch {
                val running = botDao.getRunningBotsSync()
                running.forEach { bot ->
                    botDao.updateBotRunningState(bot.id, false, "To'xtatilgan", null)
                    botDao.insertLog(BotLogEntity(botId = bot.id, level = "INFO", message = "Foydalanuvchi tomonidan barcha botlar to'xtatildi"))
                }
                stopSelf()
            }
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private suspend fun syncRunningBots(runningBots: List<BotInstanceEntity>) {
        val runningIds = runningBots.map { it.id }.toSet()

        // Cancel stopped bots
        val toStop = activeJobs.keys.filter { it !in runningIds }
        toStop.forEach { id ->
            activeJobs.remove(id)?.cancel()
            activeCodeSnapshots.remove(id)
        }

        // Start or restart modified bots
        runningBots.forEach { bot ->
            val existingJob = activeJobs[bot.id]
            val codeChanged = activeCodeSnapshots[bot.id] != bot.pythonCode
            if (existingJob == null || !existingJob.isActive || codeChanged) {
                existingJob?.cancel()
                activeCodeSnapshots[bot.id] = bot.pythonCode
                activeJobs[bot.id] = serviceScope.launch {
                    runBotLoop(bot)
                }
            }
        }

        startForegroundWithCount(runningBots.size)

        if (runningBots.isEmpty()) {
            delay(1500)
            if (activeJobs.isEmpty()) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private suspend fun runBotLoop(initialBot: BotInstanceEntity) {
        val botId = initialBot.id
        var offset = initialBot.lastUpdateOffset
        val engine = PythonBotEngine(initialBot.pythonCode)

        botDao.updateBotStatusMessage(botId, "24/7 Faol (@${initialBot.telegramUsername})")
        botDao.insertLog(
            BotLogEntity(
                botId = botId,
                level = "INFO",
                message = "24/7 Python Engine ishga tushdi (@${initialBot.telegramUsername})"
            )
        )

        while (serviceScope.isActive) {
            try {
                val latest = botDao.getBotById(botId) ?: break
                if (!latest.isRunning) break

                val result = apiClient.getUpdates(latest.token, offset, timeoutSeconds = 25)
                result.onSuccess { updates ->
                    if (updates.isNotEmpty()) {
                        var processedCount = 0
                        for (upd in updates) {
                            offset = upd.updateId + 1
                            upd.message?.let { msg ->
                                botDao.insertLog(
                                    BotLogEntity(
                                        botId = botId,
                                        level = "IN",
                                        message = "${msg.firstName}: ${msg.text.ifBlank { "[Media]" }}"
                                    )
                                )
                                val actions = engine.processMessage(msg)
                                dispatchActions(latest.token, botId, actions)
                                processedCount++
                            }
                            upd.callbackQuery?.let { cb ->
                                botDao.insertLog(
                                    BotLogEntity(
                                        botId = botId,
                                        level = "IN",
                                        message = "${cb.firstName} [Tugma]: ${cb.data}"
                                    )
                                )
                                val actions = engine.processCallback(cb)
                                dispatchActions(latest.token, botId, actions)
                                processedCount++
                            }
                        }
                        botDao.updateOffsetAndCount(botId, offset, processedCount)
                        botDao.trimOldLogs()
                    }
                    if (!latest.statusMessage.startsWith("24/7 Faol")) {
                        botDao.updateBotStatusMessage(botId, "24/7 Faol (@${latest.telegramUsername})")
                    }
                }.onFailure { err ->
                    val msg = err.message ?: "Tarmoq xatosi"
                    botDao.updateBotStatusMessage(botId, "Qayta ulanmoqda...")
                    botDao.insertLog(
                        BotLogEntity(
                            botId = botId,
                            level = "ERROR",
                            message = "Aloqa xatosi: $msg"
                        )
                    )
                    if (msg.contains("Unauthorized", ignoreCase = true) || msg.contains("401")) {
                        botDao.updateBotRunningState(botId, false, "Token yaroqsiz (401)", null)
                        break
                    }
                    delay(4000)
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                botDao.insertLog(
                    BotLogEntity(
                        botId = botId,
                        level = "ERROR",
                        message = "Jarayon xatosi: ${e.message}"
                    )
                )
                delay(4000)
            }
        }
    }

    private suspend fun dispatchActions(token: String, botId: String, actions: List<BotAction>) {
        for (action in actions) {
            when (action) {
                is BotAction.SendMessage -> {
                    val markupJson = action.replyMarkupJson?.let { runCatching { org.json.JSONObject(it) }.getOrNull() }
                    val res = apiClient.sendMessage(
                        token = token,
                        chatId = action.chatId,
                        text = action.text,
                        replyToMessageId = action.replyToMessageId,
                        replyMarkup = markupJson,
                        parseMode = action.parseMode
                    )
                    res.onSuccess {
                        botDao.insertLog(
                            BotLogEntity(
                                botId = botId,
                                level = "OUT",
                                message = action.text.replace("\n", " ").take(90)
                            )
                        )
                    }.onFailure { err ->
                        botDao.insertLog(
                            BotLogEntity(
                                botId = botId,
                                level = "ERROR",
                                message = "Yuborish xatosi: ${err.message}"
                            )
                        )
                    }
                }
                is BotAction.AnswerCallback -> {
                    apiClient.answerCallbackQuery(token, action.callbackId, action.text)
                }
                is BotAction.DeleteMessage -> {
                    apiClient.deleteMessage(token, action.chatId, action.messageId)
                    botDao.insertLog(
                        BotLogEntity(
                            botId = botId,
                            level = "OUT",
                            message = "Taqiqlangan xabar o'chirildi (#${action.messageId})"
                        )
                    )
                }
                is BotAction.LogInfo -> {
                    botDao.insertLog(
                        BotLogEntity(botId = botId, level = "INFO", message = action.message)
                    )
                }
            }
        }
    }

    private fun startForegroundWithCount(count: Int) {
        val notification = buildNotification(count)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(activeCount: Int): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, BotRunnerService::class.java).apply {
            action = ACTION_STOP_ALL
        }
        val stopPending = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = if (activeCount > 0) {
            "$activeCount ta Telegram Python bot 24/7 rejimida faol"
        } else {
            "24/7 Bot Studio xizmati tayyor"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("BOT BUILDERS uz • 24/7")
            .setContentText(contentText)
            .setOngoing(true)
            .setContentIntent(openPending)
            .addAction(0, "To'xtatish", stopPending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(PowerManager::class.java)
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BotBuildersUZ::24x7Runner").apply {
                setReferenceCounted(false)
                acquire(24 * 60 * 60 * 1000L)
            }
        }
    }

    override fun onDestroy() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        serviceScope.cancel()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "bot_builders_24x7_channel"
        private const val NOTIFICATION_ID = 2407
        private const val ACTION_STOP_ALL = "com.example.action.STOP_ALL_BOTS"

        fun start(context: Context) {
            val intent = Intent(context, BotRunnerService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
