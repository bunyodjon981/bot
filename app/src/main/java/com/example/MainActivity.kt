package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.BotTemplate
import com.example.ui.MainViewModel
import com.example.ui.components.BotBuildersBottomBar
import com.example.ui.components.BotBuildersTopBar
import com.example.ui.components.InsufficientBalanceDialog
import com.example.ui.components.MainTab
import com.example.ui.components.QuickTopUpDialog
import com.example.ui.components.TokenInputDialog
import com.example.ui.screens.AnimatedSplashScreen
import com.example.ui.screens.BotCatalogScreen
import com.example.ui.screens.BotDetailScreen
import com.example.ui.screens.FilesUploadScreen
import com.example.ui.screens.MyBotsScreen
import com.example.ui.screens.WalletAccountScreen
import com.example.ui.theme.BotBuildersTheme
import com.example.ui.theme.StudioBg

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            BotBuildersTheme {
                BotBuildersRoot()
            }
        }
    }
}

@Composable
fun BotBuildersRoot(
    viewModel: MainViewModel = viewModel()
) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.OPEN_BOT) }
    var selectedDetailBotId by rememberSaveable { mutableStateOf<String?>(null) }

    var showTopUpDialog by remember { mutableStateOf(false) }
    var tokenDialogBot by remember { mutableStateOf<BotTemplate?>(null) }
    var insufficientBalanceBot by remember { mutableStateOf<BotTemplate?>(null) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val myBots by viewModel.myBots.collectAsStateWithLifecycle()
    val logs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusBannerMessage) {
        val msg = uiState.statusBannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanner()
        }
    }

    if (showSplash) {
        AnimatedSplashScreen(
            onSplashFinished = { showSplash = false }
        )
        return
    }

    val activeBotsCount = myBots.count { it.isRunning }
    val detailTemplate = selectedDetailBotId?.let { viewModel.getTemplateById(it) }

    if (detailTemplate != null) {
        BackHandler {
            selectedDetailBotId = null
        }
        BotDetailScreen(
            template = detailTemplate,
            balanceSom = uiState.balanceSom,
            dryRunChat = uiState.dryRunChat,
            onBack = { selectedDetailBotId = null },
            onOpenBotClick = {
                if (uiState.balanceSom >= detailTemplate.priceSom) {
                    viewModel.clearTokenError()
                    tokenDialogBot = detailTemplate
                } else {
                    insufficientBalanceBot = detailTemplate
                }
            },
            onInitDryRun = { viewModel.resetDryRunChat(detailTemplate) },
            onSendDryCommand = { cmd -> viewModel.runDryCommand(detailTemplate, cmd) }
        )
    } else {
        Scaffold(
            containerColor = StudioBg,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                BotBuildersTopBar(
                    balanceSom = uiState.balanceSom,
                    activeBotsCount = activeBotsCount,
                    onBalanceClick = { showTopUpDialog = true }
                )
            },
            bottomBar = {
                BotBuildersBottomBar(
                    selectedTab = selectedTab,
                    activeBotsCount = activeBotsCount,
                    onSelectTab = { selectedTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = selectedTab, label = "mainTabCrossfade") { tab ->
                    when (tab) {
                        MainTab.OPEN_BOT -> {
                            BotCatalogScreen(
                                templates = uiState.allTemplates,
                                searchQuery = uiState.searchQuery,
                                selectedFilter = uiState.selectedFilter,
                                onSearchChange = viewModel::updateSearchQuery,
                                onFilterSelect = viewModel::selectFilter,
                                onRefreshTemplates = viewModel::refreshTemplates,
                                onBotClick = { bot -> selectedDetailBotId = bot.id }
                            )
                        }
                        MainTab.MY_BOTS -> {
                            MyBotsScreen(
                                bots = myBots,
                                logs = logs,
                                onToggleBot = viewModel::toggleBotRunning,
                                onUpdateCode = viewModel::updateBotCode,
                                onDeleteBot = viewModel::deleteBot,
                                onGoToCatalog = { selectedTab = MainTab.OPEN_BOT }
                            )
                        }
                        MainTab.FILES -> {
                            FilesUploadScreen(
                                customTemplates = uiState.customTemplates,
                                externalBotsPath = uiState.externalBotsPath,
                                onImportFolderUri = viewModel::importFromFolderUri,
                                onImportSelectedFiles = viewModel::importFromSelectedFiles,
                                onSaveFromEditor = viewModel::saveCustomBotFromEditor,
                                onExportTestSample = viewModel::exportTestBotSample,
                                onDeleteCustomTemplate = viewModel::deleteCustomTemplate,
                                onRefreshTemplates = viewModel::refreshTemplates,
                                getFileName = viewModel::getFileName,
                                onOpenInCatalog = { selectedTab = MainTab.OPEN_BOT }
                            )
                        }
                        MainTab.WALLET -> {
                            WalletAccountScreen(
                                balanceSom = uiState.balanceSom,
                                keepWakeLock = uiState.keepWakeLock,
                                transactions = transactions,
                                onOpenTopUp = { showTopUpDialog = true },
                                onRedeemPromo = viewModel::redeemPromoCode,
                                onToggleWakeLock = viewModel::setKeepWakeLock
                            )
                        }
                    }
                }
            }
        }
    }

    tokenDialogBot?.let { bot ->
        TokenInputDialog(
            template = bot,
            isVerifying = uiState.isVerifyingToken,
            errorMessage = uiState.tokenError,
            onDismiss = {
                tokenDialogBot = null
                viewModel.clearTokenError()
            },
            onCreateClick = { enteredToken ->
                viewModel.createAndLaunchBot(bot, enteredToken) {
                    tokenDialogBot = null
                    selectedDetailBotId = null
                    selectedTab = MainTab.MY_BOTS
                }
            }
        )
    }

    insufficientBalanceBot?.let { bot ->
        InsufficientBalanceDialog(
            requiredSom = bot.priceSom,
            currentSom = uiState.balanceSom,
            onDismiss = { insufficientBalanceBot = null },
            onGoToTopUp = {
                insufficientBalanceBot = null
                showTopUpDialog = true
            }
        )
    }

    if (showTopUpDialog) {
        QuickTopUpDialog(
            onDismiss = { showTopUpDialog = false },
            onTopUp = { amount, label -> viewModel.topUpBalance(amount, label) },
            onRedeemPromo = { code -> viewModel.redeemPromoCode(code) }
        )
    }
}
