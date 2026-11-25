package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bots")
data class BotInstanceEntity(
    @PrimaryKey val id: String,
    val templateId: String,
    val name: String,
    val token: String,
    val telegramUsername: String,
    val telegramBotName: String,
    val pythonCode: String,
    val infoJson: String,
    val drawableResName: String?,
    val iconFilePath: String?,
    val isRunning: Boolean,
    val statusMessage: String,
    val lastUpdateOffset: Long = 0L,
    val messagesHandled: Int = 0,
    val pricePaidSom: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null
)

@Entity(tableName = "bot_logs")
data class BotLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val botId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val level: String, // INFO, IN, OUT, ERROR
    val message: String
)

@Entity(tableName = "transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val amountSom: Long,
    val timestamp: Long = System.currentTimeMillis()
)
