package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.service.BotRunnerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BotBuildersApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Resume any bots that were left running 24/7
        appScope.launch {
            val running = AppDatabase.getInstance(this@BotBuildersApp)
                .botDao()
                .getRunningBotsSync()
            if (running.isNotEmpty()) {
                BotRunnerService.start(this@BotBuildersApp)
            }
        }
    }
}
