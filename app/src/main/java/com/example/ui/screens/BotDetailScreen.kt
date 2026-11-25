package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Terminal
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BotTemplate
import com.example.domain.model.formatSom
import com.example.ui.DryRunMessage
import com.example.ui.components.BotAvatarImage
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
import com.example.ui.theme.VioletTech

@Composable
fun BotDetailScreen(
    template: BotTemplate,
    balanceSom: Long,
    dryRunChat: List<DryRunMessage>,
    onBack: () -> Unit,
    onOpenBotClick: () -> Unit,
    onInitDryRun: () -> Unit,
    onSendDryCommand: (String) -> Unit
) {
    var showCodeSection by remember { mutableStateOf(false) }
    var showTestConsole by remember { mutableStateOf(false) }
    var testInput by remember { mutableStateOf("") }

    LaunchedEffect(template.id) {
        onInitDryRun()
    }

    Scaffold(
        containerColor = StudioBg,
        bottomBar = {
            Surface(
                color = StudioSurface,
                tonalElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(StudioCardBorder)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Narxi",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = if (template.priceSom == 0L) "0 so'm (Bepul)" else formatSom(template.priceSom),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (template.priceSom == 0L) NeonEmerald else GoldSom,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = onOpenBotClick,
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("detail_bot_ochish_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TelegramCyan,
                                contentColor = Color(0xFF041E2F)
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Icon(Icons.Rounded.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BOT OCHISH",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Hero Image with Back Button & Glowing Backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0E2346), StudioBg)
                        )
                    )
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(CircleShape)
                        .background(StudioCardBg.copy(alpha = 0.85f))
                        .border(1.dp, StudioCardBorder, CircleShape)
                        .testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Orqaga",
                        tint = TextPrimary
                    )
                }

                // Balance Indicator at Top Right
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(50),
                    color = StudioCardBg.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, GoldSom.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "Balans: ${formatSom(balanceSom)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldSom,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(164.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(listOf(TelegramCyan, VioletTech)),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .background(StudioSurfaceElevated)
                    ) {
                        BotAvatarImage(
                            drawableResName = template.drawableResName,
                            iconFilePath = template.iconFilePath,
                            contentDescription = "${template.name} rasmi",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DetailPill(text = template.category, color = TelegramCyan)
                        DetailPill(text = "v${template.version}", color = VioletTech)
                        DetailPill(text = "Python 24/7", color = NeonEmerald)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Bot Description ("Bot haqida ma'lumot")
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = BorderStroke(1.dp, StudioCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bot haqida ma'lumot",
                            style = MaterialTheme.typography.titleMedium,
                            color = TelegramCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Muallif: ${template.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Beautiful Price & Analytics Graphic Card ("narxlari bilan chiroyli grafikada")
                PriceAndAnalyticsGraphicCard(template = template)

                // Features & Commands
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = BorderStroke(1.dp, StudioCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Asosiy imkoniyatlar va buyruqlar",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        template.features.forEach { feat ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = feat, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        template.commands.forEach { cmd ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StudioSurfaceElevated,
                                border = BorderStroke(1.dp, StudioCardBorder)
                            ) {
                                Text(
                                    text = cmd,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = TelegramCyan,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // Interactive Dry-Run Simulator ("Sinab ko'rish")
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTestConsole = !showTestConsole },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Terminal, contentDescription = null, tint = NeonEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Botni hoziroq sinab ko'rish",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Python (bot.py) javobini jonli tekshiring",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Icon(
                                if (showTestConsole) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }

                        AnimatedVisibility(visible = showTestConsole) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = StudioBg,
                                    border = BorderStroke(1.dp, StudioCardBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        dryRunChat.takeLast(6).forEach { msg ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (msg.isUser) TelegramAzure.copy(alpha = 0.25f) else StudioSurfaceElevated,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (msg.isUser) TelegramCyan.copy(alpha = 0.5f) else StudioCardBorder
                                                    )
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Text(
                                                            text = msg.text,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = TextPrimary
                                                        )
                                                        if (msg.buttons.isNotEmpty()) {
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            Row(
                                                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                msg.buttons.forEach { btn ->
                                                                    Surface(
                                                                        shape = RoundedCornerShape(6.dp),
                                                                        color = TelegramCyan.copy(alpha = 0.16f)
                                                                    ) {
                                                                        Text(
                                                                            text = btn,
                                                                            style = MaterialTheme.typography.labelSmall,
                                                                            color = TelegramCyan,
                                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = testInput,
                                        onValueChange = { testInput = it },
                                        placeholder = { Text("/start, /help yoki salom...") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonEmerald,
                                            unfocusedBorderColor = StudioCardBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                    IconButton(
                                        onClick = {
                                            if (testInput.isNotBlank()) {
                                                onSendDryCommand(testInput)
                                                testInput = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(NeonEmerald)
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Yuborish", tint = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                // Expandable Python Code & malumot.json Preview
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = BorderStroke(1.dp, StudioCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCodeSection = !showCodeSection },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Code, contentDescription = null, tint = VioletTech)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "bot.py va malumot.json kodi",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                if (showCodeSection) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }

                        AnimatedVisibility(visible = showCodeSection) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("bot.py", style = MaterialTheme.typography.labelLarge, color = TelegramCyan)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF050811),
                                    border = BorderStroke(1.dp, StudioCardBorder)
                                ) {
                                    Text(
                                        text = template.pythonCode,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        ),
                                        color = Color(0xFFCBD5E1),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PriceAndAnalyticsGraphicCard(template: BotTemplate) {
    val isFree = template.priceSom == 0L
    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.3.dp, GoldSom.copy(alpha = 0.55f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF131F3A), Color(0xFF191D36))
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NARX VA ISHLASH GRAFIKASI",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldSom
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFree) "0 so'm — Bepul" else formatSom(template.priceSom),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isFree) NeonEmerald else TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Custom Canvas Circular Uptime Ring
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 6.dp.toPx()
                        drawArc(
                            color = StudioCardBorder,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(listOf(TelegramCyan, NeonEmerald, TelegramCyan)),
                            startAngle = -90f,
                            sweepAngle = 350f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "24/7",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = template.uptimeSla,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Custom Canvas Performance Bars
            MetricBarRow(label = "Tezlik (Response)", percent = template.speedScore, color = TelegramCyan)
            MetricBarRow(label = "24/7 Barqarorlik", percent = template.reliabilityScore, color = NeonEmerald)
            MetricBarRow(label = "Ommaboplik", percent = template.popularityScore, color = GoldSom)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = StudioBg.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, StudioCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Oylik server to'lovi:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Text(
                        text = if (template.monthlyServerFeeSom == 0L) "0 so'm (Telefonda bepul)" else "${formatSom(template.monthlyServerFeeSom)} / oy",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBarRow(label: String, percent: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text("$percent%", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        ) {
            drawRect(color = StudioBg)
            val fillWidth = size.width * (percent.coerceIn(0, 100) / 100f)
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = Size(fillWidth, size.height)
            )
        }
    }
}

@Composable
private fun DetailPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
