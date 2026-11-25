package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BotDao {
    @Query("SELECT * FROM bots ORDER BY createdAt DESC")
    fun observeAllBots(): Flow<List<BotInstanceEntity>>

    @Query("SELECT * FROM bots WHERE isRunning = 1 ORDER BY createdAt DESC")
    fun observeRunningBots(): Flow<List<BotInstanceEntity>>

    @Query("SELECT * FROM bots WHERE isRunning = 1")
    suspend fun getRunningBotsSync(): List<BotInstanceEntity>

    @Query("SELECT * FROM bots WHERE id = :id LIMIT 1")
    suspend fun getBotById(id: String): BotInstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBot(bot: BotInstanceEntity)

    @Query("UPDATE bots SET isRunning = :running, statusMessage = :status, startedAt = :startedAt WHERE id = :id")
    suspend fun updateBotRunningState(id: String, running: Boolean, status: String, startedAt: Long?)

    @Query("UPDATE bots SET statusMessage = :status WHERE id = :id")
    suspend fun updateBotStatusMessage(id: String, status: String)

    @Query("UPDATE bots SET lastUpdateOffset = :offset, messagesHandled = messagesHandled + :increment WHERE id = :id")
    suspend fun updateOffsetAndCount(id: String, offset: Long, increment: Int)

    @Query("UPDATE bots SET pythonCode = :code WHERE id = :id")
    suspend fun updateBotCode(id: String, code: String)

    @Query("UPDATE bots SET token = :token, telegramUsername = :username, telegramBotName = :botName WHERE id = :id")
    suspend fun updateBotToken(id: String, token: String, username: String, botName: String)

    @Query("DELETE FROM bots WHERE id = :id")
    suspend fun deleteBotById(id: String)

    @Query("SELECT * FROM bot_logs WHERE botId = :botId ORDER BY id DESC LIMIT 80")
    fun observeLogsForBot(botId: String): Flow<List<BotLogEntity>>

    @Query("SELECT * FROM bot_logs ORDER BY id DESC LIMIT 120")
    fun observeRecentLogs(): Flow<List<BotLogEntity>>

    @Insert
    suspend fun insertLog(log: BotLogEntity)

    @Query("DELETE FROM bot_logs WHERE botId = :botId")
    suspend fun clearLogsForBot(botId: String)

    @Query("DELETE FROM bot_logs WHERE id NOT IN (SELECT id FROM bot_logs ORDER BY id DESC LIMIT 300)")
    suspend fun trimOldLogs()

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert
    suspend fun insertTransaction(tx: WalletTransactionEntity)
}
