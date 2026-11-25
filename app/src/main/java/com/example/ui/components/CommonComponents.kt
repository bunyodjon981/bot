package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.BotTemplate
import com.example.domain.model.formatSom
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GoldSom
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TelegramAzure
import com.example.ui.theme.TelegramCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

enum class MainTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    OPEN_BOT("open_bot", "BOT OCHISH", Icons.Rounded.Storefront, "tab_bot_ochish"),
    MY_BOTS("my_bots", "BOTLARIM", Icons.Rounded.SmartToy, "tab_botlarim"),
    FILES("files", "FAYLLAR", Icons.Rounded.FolderCopy, "tab_fayllar"),
    WALLET("wallet", "HISOB", Icons.Rounded.AccountBalanceWallet, "tab_hisob")
}

@Composable
fun BotAvatarImage(
    drawableResName: String?,
    iconFilePath: String?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val file = remember(iconFilePath) {
        iconFilePath?.let { File(it) }?.takeIf { it.exists() }
    }
    if (file != null) {
        AsyncImage(
            model = file,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        val resId = when (drawableResName) {
            "img_bot_shop" -> R.drawable.img_bot_shop
            "img_bot_admin" -> R.drawable.img_bot_admin
            "img_bot_channel" -> R.drawable.img_bot_channel
            else -> R.drawable.img_bot_test
        }
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun BotBuildersTopBar(
    balanceSom: Long,
    activeBotsCount: Int,
    onBalanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StudioBg.copy(alpha = 0.96f),
        tonalElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(TelegramCyan, TelegramAzure)
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_art),
                            contentDescription = "BOT BUILDERS uz logotipi",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "BOT BUILDERS uz",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activeBotsCount > 0) {
                                PulsingStatusDot(color = NeonEmerald)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$activeBotsCount ta bot 24/7 faol",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeonEmerald
                                )
                            } else {
                                Text(
                                    text = "Python Telegram Studio",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Top-right Balance Pill (starts at 0 so'm)
                Surface(
                    modifier = Modifier
                        .testTag("top_balance_pill")
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onBalanceClick),
                    shape = RoundedCornerShape(50),
                    color = StudioCardBg,
                    border = BorderStroke(1.2.dp, GoldSom.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = "Balans",
                            tint = GoldSom,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatSom(balanceSom),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(GoldSom.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Balansni to'ldirish",
                                tint = GoldSom,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StudioCardBorder.copy(alpha = 0.55f))
            )
        }
    }
}

@Composable
fun BotBuildersBottomBar(
    selectedTab: MainTab,
    activeBotsCount: Int,
    onSelectTab: (MainTab) -> Unit
) {
    Surface(
        color = StudioSurface,
        tonalElevation = 8.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StudioCardBorder.copy(alpha = 0.7f))
            )
            NavigationBar(
                containerColor = StudioSurface,
                contentColor = TextPrimary,
                windowInsets = WindowInsets.navigationBars,
                tonalElevation = 0.dp
            ) {
                MainTab.entries.forEach { tab ->
                    val selected = tab == selectedTab
                    val tintColor by animateColorAsState(
                        targetValue = if (selected) TelegramCyan else TextSecondary,
                        label = "navTint"
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onSelectTab(tab) },
                        modifier = Modifier.testTag(tab.testTag),
                        icon = {
                            if (tab == MainTab.MY_BOTS && activeBotsCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = NeonEmerald, contentColor = Color.Black) {
                                            Text(activeBotsCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                ) {
                                    Icon(tab.icon, contentDescription = tab.label, tint = tintColor)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.label, tint = tintColor)
                            }
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TelegramCyan,
                            selectedTextColor = TelegramCyan,
                            indicatorColor = TelegramCyan.copy(alpha = 0.16f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PulsingStatusDot(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
fun TokenInputDialog(
    template: BotTemplate,
    isVerifying: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreateClick: (String) -> Unit
) {
    var tokenText by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = { if (!isVerifying) onDismiss() },
        containerColor = StudioSurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TelegramCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Key, contentDescription = null, tint = TelegramCyan)
            }
        },
        title = {
            Text(
                text = "Tokeningizni kiriting",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "\"${template.name}\" botini yaratish va 24/7 ishga tushirish uchun @BotFather dan olingan API tokenni kiriting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StudioCardBg,
                    border = BorderStroke(1.dp, StudioCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Narxi:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            text = if (template.priceSom == 0L) "0 so'm (Bepul)" else formatSom(template.priceSom),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (template.priceSom == 0L) NeonEmerald else GoldSom
                        )
                    }
                }

                OutlinedTextField(
                    value = tokenText,
                    onValueChange = { tokenText = it },
                    enabled = !isVerifying,
                    label = { Text("Bot Token (@BotFather)") },
                    placeholder = { Text("123456789:AAHxyz...", fontFamily = JetBrainsMonoFontFamily) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("token_input_field"),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                clipboard.getText()?.text?.let { pasted ->
                                    tokenText = pasted.trim()
                                }
                            },
                            enabled = !isVerifying
                        ) {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = "Buferdan qo'yish", tint = TelegramCyan)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelegramCyan,
                        unfocusedBorderColor = StudioCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralRed
                    )
                }

                if (isVerifying) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = TelegramCyan)
                        Text(
                            text = "Telegram API bilan tekshirilmoqda...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TelegramCyan
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateClick(tokenText) },
                enabled = !isVerifying && tokenText.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_bot_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TelegramCyan, contentColor = Color(0xFF041E2F))
            ) {
                Icon(Icons.Rounded.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Yaratish", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isVerifying) {
                Text("Bekor qilish", color = TextSecondary)
            }
        }
    )
}

@Composable
fun InsufficientBalanceDialog(
    requiredSom: Long,
    currentSom: Long,
    onDismiss: () -> Unit,
    onGoToTopUp: () -> Unit
) {
    val needed = (requiredSom - currentSom).coerceAtLeast(0L)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurfaceElevated,
        icon = {
            Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = GoldSom, modifier = Modifier.size(36.dp))
        },
        title = {
            Text("Balans yetarli emas", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ushbu botni ochish uchun ${formatSom(requiredSom)} kerak. Sizning balansingiz: ${formatSom(currentSom)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "Yetishmayotgan summa: ${formatSom(needed)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldSom
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onGoToTopUp,
                colors = ButtonDefaults.buttonColors(containerColor = GoldSom, contentColor = Color.Black)
            ) {
                Text("Hisobni to'ldirish", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Yopish", color = TextSecondary)
            }
        }
    )
}

@Composable
fun QuickTopUpDialog(
    onDismiss: () -> Unit,
    onTopUp: (Long, String) -> Unit,
    onRedeemPromo: (String) -> Unit
) {
    var customAmount by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf("") }
    val presets = listOf(15000L, 25000L, 50000L, 100000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurfaceElevated,
        title = {
            Text("Hisobni to'ldirish", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tezkor summa tanlang yoki promo kod kiriting:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(2).forEach { amt ->
                        OutlinedButton(
                            onClick = {
                                onTopUp(amt, "Tezkor to'lov")
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, GoldSom.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("+${formatSom(amt)}", color = GoldSom, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.drop(2).forEach { amt ->
                        OutlinedButton(
                            onClick = {
                                onTopUp(amt, "Tezkor to'lov")
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, GoldSom.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("+${formatSom(amt)}", color = GoldSom, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = customAmount,
                    onValueChange = { ch -> customAmount = ch.filter { it.isDigit() } },
                    label = { Text("Boshqa summa (so'm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldSom,
                        unfocusedBorderColor = StudioCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = promoCode,
                    onValueChange = { promoCode = it },
                    label = { Text("Promo kod (masalan: SALOM25)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                if (promoCode.isNotBlank()) {
                                    onRedeemPromo(promoCode)
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("Qo'llash", color = TelegramCyan, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelegramCyan,
                        unfocusedBorderColor = StudioCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = customAmount.toLongOrNull() ?: 0L
                    if (amt > 0L) {
                        onTopUp(amt, "Hisob to'ldirish")
                        onDismiss()
                    }
                },
                enabled = (customAmount.toLongOrNull() ?: 0L) > 0L,
                colors = ButtonDefaults.buttonColors(containerColor = GoldSom, contentColor = Color.Black)
            ) {
                Text("To'ldirish", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Yopish", color = TextSecondary)
            }
        }
    )
}
