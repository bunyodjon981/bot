package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.walletDataStore by preferencesDataStore(name = "bot_builders_prefs")

class WalletPreferences(private val context: Context) {

    private object Keys {
        val BALANCE_SOM = longPreferencesKey("balance_som")
        val USED_PROMOS = stringSetPreferencesKey("used_promos")
        val KEEP_WAKE_LOCK = booleanPreferencesKey("keep_wake_lock")
    }

    // Balance strictly starts at 0 so'm as requested
    val balanceSom: Flow<Long> = context.walletDataStore.data.map { prefs ->
        prefs[Keys.BALANCE_SOM] ?: 0L
    }

    val keepWakeLock: Flow<Boolean> = context.walletDataStore.data.map { prefs ->
        prefs[Keys.KEEP_WAKE_LOCK] ?: true
    }

    val usedPromos: Flow<Set<String>> = context.walletDataStore.data.map { prefs ->
        prefs[Keys.USED_PROMOS] ?: emptySet()
    }

    suspend fun getBalanceSync(): Long {
        return context.walletDataStore.data.first()[Keys.BALANCE_SOM] ?: 0L
    }

    suspend fun addBalance(amountSom: Long) {
        context.walletDataStore.edit { prefs ->
            val current = prefs[Keys.BALANCE_SOM] ?: 0L
            prefs[Keys.BALANCE_SOM] = (current + amountSom).coerceAtLeast(0L)
        }
    }

    suspend fun deductBalanceIfEnough(amountSom: Long): Boolean {
        var success = false
        context.walletDataStore.edit { prefs ->
            val current = prefs[Keys.BALANCE_SOM] ?: 0L
            if (current >= amountSom) {
                prefs[Keys.BALANCE_SOM] = current - amountSom
                success = true
            }
        }
        return success
    }

    suspend fun markPromoUsed(code: String): Boolean {
        var redeemed = false
        context.walletDataStore.edit { prefs ->
            val current = prefs[Keys.USED_PROMOS] ?: emptySet()
            if (!current.contains(code)) {
                prefs[Keys.USED_PROMOS] = current + code
                redeemed = true
            }
        }
        return redeemed
    }

    suspend fun setKeepWakeLock(enabled: Boolean) {
        context.walletDataStore.edit { prefs ->
            prefs[Keys.KEEP_WAKE_LOCK] = enabled
        }
    }
}
