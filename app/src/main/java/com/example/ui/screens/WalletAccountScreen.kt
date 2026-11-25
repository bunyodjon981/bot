package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.WalletTransactionEntity
import com.example.domain.model.formatSom
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GoldSom
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TelegramCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletAccountScreen(
    balanceSom: Long,
    keepWakeLock: Boolean,
    transactions: List<WalletTransactionEntity>,
    onOpenTopUp: () -> Unit,
    onRedeemPromo: (String) -> Unit,
    onToggleWakeLock: (Boolean) -> Unit
) {
    var promoInput by remember { mutableStateOf("") }
    val dateFmt = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .testTag("wallet_account_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Balance Hero Card
            Card(
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.5.dp, GoldSom.copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1A233A), Color(0xFF231B12))
                            )
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = GoldSom,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MENING BALANSIM",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldSom
                        )
                    }

                    Text(
                        text = formatSom(balanceSom),
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onOpenTopUp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("wallet_topup_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldSom, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hisobni to'ldirish", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Promo code card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CardGiftcard, contentDescription = null, tint = TelegramCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Promo kod orqali bonus olish",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Sinab ko'rish uchun promo kodlar: SALOM25 (+25 000 so'm) yoki BOT50 (+50 000 so'm)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { promoInput = it },
                            placeholder = { Text("SALOM25") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TelegramCyan,
                                unfocusedBorderColor = StudioCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Button(
                            onClick = {
                                if (promoInput.isNotBlank()) {
                                    onRedeemPromo(promoInput)
                                    promoInput = ""
                                }
                            },
                            modifier = Modifier.height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramCyan, contentColor = Color(0xFF041E2F))
                        ) {
                            Text("Faollashtirish", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 24/7 Background Setting & BotFather Guide
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = NeonEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "24/7 Uyg'oq rejim (WakeLock)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ekran o'chganda ham botlar uzilmasdan ishlaydi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = keepWakeLock,
                            onCheckedChange = onToggleWakeLock,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonEmerald)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StudioSurfaceElevated,
                        border = BorderStroke(1.dp, StudioCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Info, contentDescription = null, tint = TelegramCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("@BotFather dan Token olish qo'llanmasi", style = MaterialTheme.typography.labelLarge, color = TelegramCyan)
                            }
                            Text(
                                text = "1. Telegramda @BotFather ga kiring\n2. /newbot buyrug'ini yuboring\n3. Bot nomi va @username kiriting\n4. Berilgan tokenni nusxalab BOT OCHISH ga kiriting",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Transaction History
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.History, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Amaliyotlar tarixi",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioCardBg,
                    border = BorderStroke(1.dp, StudioCardBorder)
                ) {
                    Text(
                        text = "Hozircha moliyaviy amaliyotlar mavjud emas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    )
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioCardBg,
                    border = BorderStroke(1.dp, StudioCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text(
                                text = dateFmt.format(Date(tx.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        val color = when {
                            tx.amountSom > 0 -> NeonEmerald
                            tx.amountSom < 0 -> CoralRed
                            else -> TelegramCyan
                        }
                        val prefix = if (tx.amountSom > 0) "+" else ""
                        Text(
                            text = "$prefix${formatSom(tx.amountSom)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
