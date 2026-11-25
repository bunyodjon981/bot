package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BotInstanceEntity
import com.example.data.local.BotLogEntity
import com.example.data.local.BotTemplateStorage
import com.example.data.local.BuiltInTemplates
import com.example.data.local.WalletPreferences
import com.example.data.local.WalletTransactionEntity
import com.example.domain.model.BotTemplate
import com.example.domain.model.CatalogFilter
import com.example.domain.model.formatSom
import com.example.engine.BotAction
import com.example.engine.PythonBotEngine
import com.example.engine.TelegramApiClient
import com.example.engine.TelegramIncomingMessage
import com.example.service.BotRunnerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class DryRunMessage(
    val isUser: Boolean,
    val text: String,
    val buttons: List<String> = emptyList()
)

data class MainUiState(
    val balanceSom: Long = 0L,
    val allTemplates: List<BotTemplate> = BuiltInTemplates.templates,
    val customTemplates: List<BotTemplate> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: CatalogFilter = CatalogFilter.ALL,
    val isVerifyingToken: Boolean = false,
    val tokenError: String? = null,
    val statusBannerMessage: String? = null,
    val keepWakeLock: Boolean = true,
    val dryRunChat: List<DryRunMessage> = emptyList(),
    val externalBotsPath: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val db = AppDatabase.getInstance(appContext)
    private val botDao = db.botDao()
    private val walletPrefs = WalletPreferences(appContext)
    private val templateStorage = BotTemplateStorage(appContext)
    private val telegramApi = TelegramApiClient()

    private val _customTemplates = MutableStateFlow<List<BotTemplate>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(CatalogFilter.ALL)
    private val _isVerifyingToken = MutableStateFlow(false)
    private val _tokenError = MutableStateFlow<String?>(null)
    private val _statusBanner = MutableStateFlow<String?>(null)
    private val _dryRunChat = MutableStateFlow<List<DryRunMessage>>(emptyList())

    val balanceSom: StateFlow<Long> = walletPrefs.balanceSom
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val myBots: StateFlow<List<BotInstanceEntity>> = botDao.observeAllBots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<BotLogEntity>> = botDao.observeRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<WalletTransactionEntity>> = botDao.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<MainUiState> = combine(
        balanceSom,
        _customTemplates,
        _searchQuery,
        _selectedFilter,
        _isVerifyingToken,
        _tokenError,
        _statusBanner,
        walletPrefs.keepWakeLock,
        _dryRunChat
    ) { values ->
        val balance = values[0] as Long
        @Suppress("UNCHECKED_CAST")
        val custom = values[1] as List<BotTemplate>
        val query = values[2] as String
        val filter = values[3] as CatalogFilter
        val verifying = values[4] as Boolean
        val tErr = values[5] as String?
        val banner = values[6] as String?
        val wakeLock = values[7] as Boolean
        @Suppress("UNCHECKED_CAST")
        val dryChat = values[8] as List<DryRunMessage>

        val combined = custom + BuiltInTemplates.templates
        MainUiState(
            balanceSom = balance,
            allTemplates = combined,
            customTemplates = custom,
            searchQuery = query,
            selectedFilter = filter,
            isVerifyingToken = verifying,
            tokenError = tErr,
            statusBannerMessage = banner,
            keepWakeLock = wakeLock,
            dryRunChat = dryChat,
            externalBotsPath = templateStorage.externalBotsDir?.absolutePath ?: "files/bots"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())

    init {
        refreshTemplates()
    }

    fun refreshTemplates() {
        viewModelScope.launch(Dispatchers.IO) {
            val loaded = templateStorage.loadAllCustomTemplates()
            _customTemplates.value = loaded
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFilter(filter: CatalogFilter) {
        _selectedFilter.value = filter
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun clearTokenError() {
        _tokenError.value = null
    }

    fun getTemplateById(id: String): BotTemplate? {
        return uiState.value.allTemplates.firstOrNull { it.id == id }
    }

    fun createAndLaunchBot(
        template: BotTemplate,
        rawToken: String,
        onSuccess: (BotInstanceEntity) -> Unit
    ) {
        val token = rawToken.trim()
        if (token.isBlank()) {
            _tokenError.value = "Iltimos, Telegram bot tokenini kiriting"
            return
        }

        viewModelScope.launch {
            _isVerifyingToken.value = true
            _tokenError.value = null

            val currentBalance = walletPrefs.getBalanceSync()
            if (currentBalance < template.priceSom) {
                _isVerifyingToken.value = false
                _tokenError.value = "Balansingiz yetarli emas (${formatSom(template.priceSom)} kerak)"
                return@launch
            }

            val verifyResult = telegramApi.verifyToken(token)
            verifyResult.onSuccess { info ->
                val deducted = if (template.priceSom == 0L) true else walletPrefs.deductBalanceIfEnough(template.priceSom)
                if (!deducted) {
                    _isVerifyingToken.value = false
                    _tokenError.value = "Balans yetarli emas"
                    return@onSuccess
                }

                if (template.priceSom > 0L) {
                    botDao.insertTransaction(
                        WalletTransactionEntity(
                            title = "Bot ochildi: ${template.name}",
                            amountSom = -template.priceSom
                        )
                    )
                } else {
                    botDao.insertTransaction(
                        WalletTransactionEntity(
                            title = "Bepul bot ochildi: ${template.name}",
                            amountSom = 0L
                        )
                    )
                }

                val instance = BotInstanceEntity(
                    id = UUID.randomUUID().toString(),
                    templateId = template.id,
                    name = template.name,
                    token = token,
                    telegramUsername = info.username,
                    telegramBotName = info.firstName,
                    pythonCode = template.pythonCode,
                    infoJson = template.infoJson,
                    drawableResName = template.drawableResName,
                    iconFilePath = template.iconFilePath,
                    isRunning = true,
                    statusMessage = "24/7 ishga tushirilmoqda...",
                    pricePaidSom = template.priceSom,
                    createdAt = System.currentTimeMillis(),
                    startedAt = System.currentTimeMillis()
                )
                botDao.upsertBot(instance)
                botDao.insertLog(
                    BotLogEntity(
                        botId = instance.id,
                        level = "INFO",
                        message = "@${info.username} yaratildi va 24/7 fon rejimida ishga tushirildi"
                    )
                )

                BotRunnerService.start(appContext)
                _isVerifyingToken.value = false
                _statusBanner.value = "@${info.username} muvaffaqiyatli yaratildi va 24/7 ishga tushirildi!"
                onSuccess(instance)
            }.onFailure { err ->
                _isVerifyingToken.value = false
                _tokenError.value = "Token xatosi: ${err.message ?: "Telegram API ga ulanib bo'lmadi"}"
            }
        }
    }

    fun toggleBotRunning(bot: BotInstanceEntity) {
        viewModelScope.launch {
            val next = !bot.isRunning
            val msg = if (next) "24/7 ishga tushirilmoqda..." else "To'xtatilgan"
            botDao.updateBotRunningState(
                id = bot.id,
                running = next,
                status = msg,
                startedAt = if (next) System.currentTimeMillis() else null
            )
            botDao.insertLog(
                BotLogEntity(
                    botId = bot.id,
                    level = "INFO",
                    message = if (next) "Bot qayta ishga tushirildi" else "Bot to'xtatildi"
                )
            )
            if (next) {
                BotRunnerService.start(appContext)
            }
        }
    }

    fun updateBotCode(botId: String, newPythonCode: String) {
        viewModelScope.launch {
            botDao.updateBotCode(botId, newPythonCode)
            botDao.insertLog(
                BotLogEntity(
                    botId = botId,
                    level = "INFO",
                    message = "bot.py kodi yangilandi — Engine qayta yuklanmoqda"
                )
            )
            _statusBanner.value = "Python kodi yangilandi!"
        }
    }

    fun deleteBot(bot: BotInstanceEntity) {
        viewModelScope.launch {
            botDao.clearLogsForBot(bot.id)
            botDao.deleteBotById(bot.id)
            _statusBanner.value = "${bot.name} o'chirildi"
        }
    }

    fun topUpBalance(amountSom: Long, sourceLabel: String = "Hisob to'ldirish") {
        if (amountSom <= 0L) return
        viewModelScope.launch {
            walletPrefs.addBalance(amountSom)
            botDao.insertTransaction(
                WalletTransactionEntity(
                    title = sourceLabel,
                    amountSom = amountSom
                )
            )
            _statusBanner.value = "Balans +${formatSom(amountSom)} ga to'ldirildi!"
        }
    }

    fun redeemPromoCode(rawCode: String) {
        val code = rawCode.trim().uppercase()
        val reward = when (code) {
            "SALOM25", "TEST25" -> 25000L
            "BOT50", "START50" -> 50000L
            "UZ100" -> 100000L
            else -> 0L
        }
        if (reward == 0L) {
            _statusBanner.value = "Promo kod topilmadi (Mavjud: SALOM25, BOT50)"
            return
        }
        viewModelScope.launch {
            val firstTime = walletPrefs.markPromoUsed(code)
            if (firstTime) {
                walletPrefs.addBalance(reward)
                botDao.insertTransaction(
                    WalletTransactionEntity(
                        title = "Promo kod ($code)",
                        amountSom = reward
                    )
                )
                _statusBanner.value = "Tabriklaymiz! $code orqali +${formatSom(reward)} qo'shildi"
            } else {
                _statusBanner.value = "$code promo kodi avval ishlatilgan"
            }
        }
    }

    fun setKeepWakeLock(enabled: Boolean) {
        viewModelScope.launch {
            walletPrefs.setKeepWakeLock(enabled)
        }
    }

    // --- File Upload & Custom Bot Importer ---

    fun importFromFolderUri(treeUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = templateStorage.importFromFolderTree(treeUri)
            withContext(Dispatchers.Main) {
                res.onSuccess { tmpl ->
                    refreshTemplates()
                    _statusBanner.value = "\"${tmpl.name}\" papkadan yuklandi va BOT OCHISH bo'limiga qo'shildi!"
                }.onFailure { err ->
                    _statusBanner.value = "Yuklash xatosi: ${err.message}"
                }
            }
        }
    }

    fun importFromSelectedFiles(
        botPyUri: Uri,
        malumotJsonUri: Uri?,
        iconUri: Uri?,
        fallbackName: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = templateStorage.saveCustomBotFromUris(
                botPyUri = botPyUri,
                malumotJsonUri = malumotJsonUri,
                iconUri = iconUri,
                fallbackName = fallbackName.ifBlank { "Maxsus Bot" }
            )
            withContext(Dispatchers.Main) {
                res.onSuccess { tmpl ->
                    refreshTemplates()
                    _statusBanner.value = "\"${tmpl.name}\" BOT OCHISH bo'limiga avtomatik qo'shildi!"
                }.onFailure { err ->
                    _statusBanner.value = "Fayl xatosi: ${err.message}"
                }
            }
        }
    }

    fun saveCustomBotFromEditor(
        name: String,
        pythonCode: String,
        infoJson: String,
        iconUri: Uri?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val iconBytes = iconUri?.let { uri ->
                runCatching { appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            }
            val tmpl = templateStorage.saveCustomBotFromContent(
                folderSlug = "${name}_${System.currentTimeMillis() % 10000}",
                pythonCode = pythonCode,
                infoJson = infoJson,
                iconBytes = iconBytes
            )
            withContext(Dispatchers.Main) {
                refreshTemplates()
                _statusBanner.value = "\"${tmpl.name}\" saqlandi va BOT OCHISH ro'yxatiga chiqdi!"
            }
        }
    }

    fun exportTestBotSample() {
        viewModelScope.launch(Dispatchers.IO) {
            val res = templateStorage.exportTestSampleToExternalBotsDir()
            withContext(Dispatchers.Main) {
                res.onSuccess { dir ->
                    refreshTemplates()
                    _statusBanner.value = "Namuna saqlandi: ${dir.name}/ (bot.py, malumot.json, icon.png)"
                }.onFailure { err ->
                    _statusBanner.value = "Saqlashda xato: ${err.message}"
                }
            }
        }
    }

    fun deleteCustomTemplate(template: BotTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            templateStorage.deleteCustomBot(template)
            refreshTemplates()
            withContext(Dispatchers.Main) {
                _statusBanner.value = "${template.name} shabloni o'chirildi"
            }
        }
    }

    fun getFileName(uri: Uri): String = templateStorage.getDisplayFileName(uri)

    // --- Local Dry-Run Simulator so users can test bot.py before tokenizing ---

    fun resetDryRunChat(template: BotTemplate) {
        _dryRunChat.value = emptyList()
        runDryCommand(template, "/start")
    }

    fun runDryCommand(template: BotTemplate, inputText: String) {
        val clean = inputText.trim()
        if (clean.isEmpty()) return

        val engine = PythonBotEngine(template.pythonCode)
        val actions = engine.processMessage(
            TelegramIncomingMessage(
                messageId = System.currentTimeMillis(),
                chatId = 10001L,
                text = clean,
                userId = 777L,
                firstName = "Foydalanuvchi",
                username = "tester"
            )
        )

        val responses = actions.filterIsInstance<BotAction.SendMessage>().map { send ->
            val btns = mutableListOf<String>()
            val markupObj = send.replyMarkupJson?.let { runCatching { org.json.JSONObject(it) }.getOrNull() }
            val inline = markupObj?.optJSONArray("inline_keyboard")
            if (inline != null) {
                for (r in 0 until inline.length()) {
                    val row = inline.getJSONArray(r)
                    for (c in 0 until row.length()) {
                        btns.add(row.getJSONObject(c).optString("text"))
                    }
                }
            }
            DryRunMessage(isUser = false, text = send.text, buttons = btns)
        }.ifEmpty {
            listOf(DryRunMessage(isUser = false, text = "(Javob yuborilmadi)"))
        }

        _dryRunChat.update { current ->
            current + DryRunMessage(isUser = true, text = clean) + responses
        }
    }
}
