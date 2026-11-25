package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.BotInstanceEntity
import com.example.data.local.BotLogEntity
import com.example.ui.components.BotAvatarImage
import com.example.ui.components.PulsingStatusDot
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GoldSom
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TelegramCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletTech
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyBotsScreen(
    bots: List<BotInstanceEntity>,
    logs: List<BotLogEntity>,
    onToggleBot: (BotInstanceEntity) -> Unit,
    onUpdateCode: (String, String) -> Unit,
    onDeleteBot: (BotInstanceEntity) -> Unit,
    onGoToCatalog: () -> Unit
) {
    val context = LocalContext.current
    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasNotifPermission = granted }
    )

    var editingCodeBot by remember { mutableStateOf<BotInstanceEntity?>(null) }
    var deletingBot by remember { mutableStateOf<BotInstanceEntity?>(null) }
    var expandedLogBotId by remember { mutableStateOf<String?>(null) }

    val runningCount = bots.count { it.isRunning }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .testTag("my_bots_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // 24/7 Service Header Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = BorderStroke(
                    1.2.dp,
                    if (runningCount > 0) NeonEmerald.copy(alpha = 0.7f) else StudioCardBorder
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (runningCount > 0) {
                                PulsingStatusDot(color = NeonEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = "24/7 Fon Xizmati (Python Engine)",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (runningCount > 0) NeonEmerald.copy(alpha = 0.16f) else StudioSurfaceElevated
                        ) {
                            Text(
                                text = if (runningCount > 0) "$runningCount ta faol" else "Kutish holati",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (runningCount > 0) NeonEmerald else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Botlaringiz Telegram Bot API (getUpdates) orqali to'g'ridan-to'g'ri telefonda 24/7 ishlaydi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        OutlinedButton(
                            onClick = { notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            border = BorderStroke(1.dp, GoldSom),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = GoldSom, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("24/7 bildirishnomaga ruxsat berish", color = GoldSom, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (bots.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = BorderStroke(1.dp, StudioCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.SmartToy,
                            contentDescription = null,
                            tint = TelegramCyan,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hozircha yaratilgan botlar yo'q",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "BOT OCHISH bo'limidan bepul Test botni yoki boshqa botlarni ochib 24/7 ishga tushiring!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onGoToCatalog,
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramCyan, contentColor = Color(0xFF041E2F))
                        ) {
                            Text("BOT OCHISH ga o'tish", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(bots, key = { it.id }) { bot ->
                val botLogs = logs.filter { it.botId == bot.id }
                val logsExpanded = expandedLogBotId == bot.id
                BotInstanceCard(
                    bot = bot,
                    logs = botLogs,
                    logsExpanded = logsExpanded,
                    onToggleExpandLogs = {
                        expandedLogBotId = if (logsExpanded) null else bot.id
                    },
                    onToggleRunning = { onToggleBot(bot) },
                    onEditCode = { editingCodeBot = bot },
                    onDelete = { deletingBot = bot }
                )
            }
        }
    }

    editingCodeBot?.let { bot ->
        var codeDraft by remember(bot.id) { mutableStateOf(bot.pythonCode) }
        AlertDialog(
            onDismissRequest = { editingCodeBot = null },
            containerColor = StudioSurfaceElevated,
            title = { Text("${bot.name} — bot.py tahrirlash", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = codeDraft,
                    onValueChange = { codeDraft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    textStyle = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelegramCyan,
                        unfocusedBorderColor = StudioCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateCode(bot.id, codeDraft)
                        editingCodeBot = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramCyan, contentColor = Color(0xFF041E2F))
                ) {
                    Text("Saqlash", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCodeBot = null }) {
                    Text("Bekor qilish", color = TextSecondary)
                }
            }
        )
    }

    deletingBot?.let { bot ->
        AlertDialog(
            onDismissRequest = { deletingBot = null },
            containerColor = StudioSurfaceElevated,
            title = { Text("Botni o'chirish", color = TextPrimary) },
            text = { Text("@${bot.telegramUsername} botini o'chirishni tasdiqlaysizmi?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBot(bot)
                        deletingBot = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed, contentColor = Color.White)
                ) {
                    Text("O'chirish")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingBot = null }) {
                    Text("Bekor qilish", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun BotInstanceCard(
    bot: BotInstanceEntity,
    logs: List<BotLogEntity>,
    logsExpanded: Boolean,
    onToggleExpandLogs: () -> Unit,
    onToggleRunning: () -> Unit,
    onEditCode: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = BorderStroke(
            1.4.dp,
            if (bot.isRunning) NeonEmerald.copy(alpha = 0.65f) else StudioCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    BotAvatarImage(
                        drawableResName = bot.drawableResName,
                        iconFilePath = bot.iconFilePath,
                        contentDescription = bot.name,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = bot.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${bot.telegramUsername}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TelegramCyan
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (bot.isRunning) {
                                PulsingStatusDot(color = NeonEmerald)
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = bot.statusMessage,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (bot.isRunning) NeonEmerald else TextSecondary
                            )
                        }
                    }
                }

                Switch(
                    checked = bot.isRunning,
                    onCheckedChange = { onToggleRunning() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonEmerald,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = StudioSurfaceElevated
                    )
                )
            }

            // Stats & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioSurfaceElevated
                ) {
                    Text(
                        text = "Xabarlar: ${bot.messagesHandled}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = onToggleExpandLogs,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, StudioCardBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.Terminal, contentDescription = null, tint = TelegramCyan, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Konsol (${logs.size})", color = TelegramCyan, fontSize = 11.sp)
                    }
                    IconButton(onClick = onEditCode) {
                        Icon(Icons.Rounded.Code, contentDescription = "bot.py tahrirlash", tint = VioletTech)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "O'chirish", tint = CoralRed)
                    }
                }
            }

            AnimatedVisibility(visible = logsExpanded) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF050811),
                    border = BorderStroke(1.dp, StudioCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (logs.isEmpty()) {
                            Text(
                                text = "Hozircha loglar yo'q. Telegramda @${bot.telegramUsername} ga /start yuboring!",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        } else {
                            logs.take(15).forEach { log ->
                                val lvlColor = when (log.level) {
                                    "IN" -> TelegramCyan
                                    "OUT" -> NeonEmerald
                                    "ERROR" -> CoralRed
                                    else -> GoldSom
                                }
                                Text(
                                    text = "[${timeFormatter.format(Date(log.timestamp))}] [${log.level}] ${log.message}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp
                                    ),
                                    color = lvlColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
