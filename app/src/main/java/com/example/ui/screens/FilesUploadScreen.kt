package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.UploadFile
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.BuiltInTemplates
import com.example.domain.model.BotTemplate
import com.example.domain.model.formatSom
import com.example.ui.components.BotAvatarImage
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

@Composable
fun FilesUploadScreen(
    customTemplates: List<BotTemplate>,
    externalBotsPath: String,
    onImportFolderUri: (Uri) -> Unit,
    onImportSelectedFiles: (botPy: Uri, malumotJson: Uri?, iconPng: Uri?, fallbackName: String) -> Unit,
    onSaveFromEditor: (name: String, pythonCode: String, infoJson: String, iconUri: Uri?) -> Unit,
    onExportTestSample: () -> Unit,
    onDeleteCustomTemplate: (BotTemplate) -> Unit,
    onRefreshTemplates: () -> Unit,
    getFileName: (Uri) -> String,
    onOpenInCatalog: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    var selectedBotPyUri by remember { mutableStateOf<Uri?>(null) }
    var selectedJsonUri by remember { mutableStateOf<Uri?>(null) }
    var selectedIconUri by remember { mutableStateOf<Uri?>(null) }
    var customBotName by remember { mutableStateOf("") }

    var showTestSampleCode by remember { mutableStateOf(false) }
    var showInAppEditor by remember { mutableStateOf(false) }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri -> uri?.let { onImportFolderUri(it) } }
    )

    val pyPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) selectedBotPyUri = uri }
    )

    val jsonPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) selectedJsonUri = uri }
    )

    // Zero-permission Android Photo Picker for icon.png
    val iconPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) selectedIconUri = uri }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .testTag("files_upload_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Upload Bot Package Card (bot.py + malumot.json + icon.png)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = BorderStroke(1.3.dp, TelegramCyan.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "YANGI BOT YUKLASH",
                                style = MaterialTheme.typography.titleMedium,
                                color = TelegramCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "bot.py + malumot.json + icon.png yuklasangiz, BOT OCHISH bo'limida avtomatik chiqadi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = onRefreshTemplates) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Yangilash", tint = TelegramCyan)
                        }
                    }

                    // 1-tap Folder Import & In-App Editor buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { folderPicker.launch(null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_folder_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramCyan, contentColor = Color(0xFF041E2F)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Papkadan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showInAppEditor = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_code_editor_button"),
                            border = BorderStroke(1.dp, VioletTech),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = VioletTech, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kod yozish", color = VioletTech, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Individual 3-file slots
                    FileSlotRow(
                        fileName = "bot.py (Majburiy)",
                        selectedLabel = selectedBotPyUri?.let { getFileName(it) },
                        icon = Icons.Rounded.Code,
                        accent = TelegramCyan,
                        onPickClick = { pyPicker.launch(arrayOf("*/*")) }
                    )

                    FileSlotRow(
                        fileName = "malumot.json (Tavsiya etiladi)",
                        selectedLabel = selectedJsonUri?.let { getFileName(it) },
                        icon = Icons.Rounded.UploadFile,
                        accent = GoldSom,
                        onPickClick = { jsonPicker.launch(arrayOf("application/json", "text/*", "*/*")) }
                    )

                    FileSlotRow(
                        fileName = "icon.png (Rasm)",
                        selectedLabel = selectedIconUri?.let { getFileName(it) },
                        icon = Icons.Rounded.Image,
                        accent = NeonEmerald,
                        onPickClick = {
                            iconPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    if (selectedBotPyUri != null && selectedJsonUri == null) {
                        OutlinedTextField(
                            value = customBotName,
                            onValueChange = { customBotName = it },
                            label = { Text("Bot nomi (malumot.json bo'lmasa)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TelegramCyan,
                                unfocusedBorderColor = StudioCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Button(
                        onClick = {
                            val py = selectedBotPyUri ?: return@Button
                            onImportSelectedFiles(py, selectedJsonUri, selectedIconUri, customBotName)
                            selectedBotPyUri = null
                            selectedJsonUri = null
                            selectedIconUri = null
                            customBotName = ""
                        },
                        enabled = selectedBotPyUri != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_files_upload_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BOT OCHISH ga avtomatik qo'shish", fontWeight = FontWeight.Bold)
                    }

                    // Auto-scan folder info
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioSurfaceElevated,
                        border = BorderStroke(1.dp, StudioCardBorder)
                    ) {
                        Text(
                            text = "Avto-skaner papkasi: $externalBotsPath\nShu papkaga bot_nomi/ ichida bot.py, malumot.json, icon.png joylasangiz ham avtomatik chiqadi.",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = TextSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Built-in Test Bot Example Showcase (so user can inspect bot.py, malumot.json, icon.png!)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = BorderStroke(1.2.dp, NeonEmerald.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_bot_test),
                            contentDescription = "Test Bot icon.png namunasi",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TEST BOT NAMUNASI (O'rganish uchun)",
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonEmerald
                            )
                            Text(
                                text = "Tayyor bot.py, malumot.json va icon.png tuzilishini ko'rib chiqing",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showTestSampleCode = !showTestSampleCode },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, TelegramCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                if (showTestSampleCode) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = TelegramCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showTestSampleCode) "Yopish" else "Kodni ko'rish",
                                color = TelegramCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onExportTestSample,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_sample_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Namuna saqlash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = showTestSampleCode) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CodeBoxWithCopy(
                                title = "bot.py",
                                content = BuiltInTemplates.testBotPythonCode,
                                onCopy = {
                                    clipboard.setText(AnnotatedString(BuiltInTemplates.testBotPythonCode))
                                }
                            )
                            CodeBoxWithCopy(
                                title = "malumot.json",
                                content = BuiltInTemplates.testBotInfoJson,
                                onCopy = {
                                    clipboard.setText(AnnotatedString(BuiltInTemplates.testBotInfoJson))
                                }
                            )
                        }
                    }
                }
            }
        }

        // Uploaded Custom Bots List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Yuklangan maxsus botlar (${customTemplates.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (customTemplates.isNotEmpty()) {
                    TextButton(onClick = onOpenInCatalog) {
                        Text("BOT OCHISH da ko'rish →", color = TelegramCyan)
                    }
                }
            }
        }

        if (customTemplates.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioCardBg,
                    border = BorderStroke(1.dp, StudioCardBorder)
                ) {
                    Text(
                        text = "Hozircha yuklangan maxsus bot yo'q. Yuqoridagi tugmalar orqali bot.py va malumot.json yuklang — ular darhol BOT OCHISH bo'limida paydo bo'ladi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(customTemplates, key = { it.id }) { tmpl ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = BorderStroke(1.dp, VioletTech.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            BotAvatarImage(
                                drawableResName = tmpl.drawableResName,
                                iconFilePath = tmpl.iconFilePath,
                                contentDescription = tmpl.name,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tmpl.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (tmpl.priceSom == 0L) "0 so'm • Bepul" else formatSom(tmpl.priceSom),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (tmpl.priceSom == 0L) NeonEmerald else GoldSom
                                )
                            }
                        }
                        IconButton(onClick = { onDeleteCustomTemplate(tmpl) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "O'chirish", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }

    if (showInAppEditor) {
        InAppBotCreatorDialog(
            onDismiss = { showInAppEditor = false },
            onSave = { name, py, json, iconUri ->
                onSaveFromEditor(name, py, json, iconUri)
                showInAppEditor = false
            }
        )
    }
}

@Composable
private fun FileSlotRow(
    fileName: String,
    selectedLabel: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onPickClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onPickClick),
        shape = RoundedCornerShape(12.dp),
        color = StudioSurfaceElevated,
        border = BorderStroke(
            1.dp,
            if (selectedLabel != null) NeonEmerald.copy(alpha = 0.7f) else StudioCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (selectedLabel != null) Icons.Rounded.CheckCircle else icon,
                    contentDescription = null,
                    tint = if (selectedLabel != null) NeonEmerald else accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(fileName, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                    Text(
                        text = selectedLabel ?: "Tanlash uchun bosing...",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedLabel != null) NeonEmerald else TextSecondary
                    )
                }
            }
            Text("Tanlash", style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CodeBoxWithCopy(
    title: String,
    content: String,
    onCopy: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = TelegramCyan)
            TextButton(onClick = onCopy, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = TelegramCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nusxalash", color = TelegramCyan, fontSize = 11.sp)
            }
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF050811),
            border = BorderStroke(1.dp, StudioCardBorder)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                ),
                color = Color(0xFFCBD5E1),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(10.dp)
            )
        }
    }
}

@Composable
private fun InAppBotCreatorDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Uri?) -> Unit
) {
    var botName by remember { mutableStateOf("Mening Yangi Botim") }
    var priceStr by remember { mutableStateOf("0") }
    var pyCode by remember {
        mutableStateOf(
            """
import telebot

@bot.message_handler(commands=['start'])
def start_cmd(message):
    bot.reply_to(message, f"Salom, {message.from_user.first_name}! Bu mening shaxsiy Python botim.")

@bot.message_handler(func=lambda m: True)
def echo(message):
    bot.reply_to(message, f"Siz yozdingiz: {message.text}")
            """.trimIndent()
        )
    }
    var iconUri by remember { mutableStateOf<Uri?>(null) }

    val iconPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) iconUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurfaceElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CreateNewFolder, contentDescription = null, tint = TelegramCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yangi bot yaratish", color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = botName,
                    onValueChange = { botName = it },
                    label = { Text("Bot nomi (malumot.json)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { s -> priceStr = s.filter { it.isDigit() } },
                    label = { Text("Narxi (so'm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        iconPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Image, contentDescription = null, tint = NeonEmerald)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (iconUri != null) "icon.png tanlandi ✓" else "icon.png tanlash (ixtiyoriy)", color = TextPrimary)
                }
                OutlinedTextField(
                    value = pyCode,
                    onValueChange = { pyCode = it },
                    label = { Text("bot.py kodi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    textStyle = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toLongOrNull() ?: 0L
                    val json = """
                        {
                          "name": "${botName.replace("\"", "'")}",
                          "tagline": "Ichki muharrirda yaratilgan bot",
                          "description": "BOT BUILDERS uz ichki kod muharriri orqali yaratilgan maxsus Python Telegram boti.",
                          "price": $price,
                          "category": "Maxsus Bot",
                          "version": "1.0",
                          "author": "Foydalanuvchi",
                          "commands": ["/start - Botni ishga tushirish"],
                          "features": ["24/7 fon rejimida ishlaydi", "Shaxsiy bot.py"]
                        }
                    """.trimIndent()
                    onSave(botName, pyCode, json, iconUri)
                },
                enabled = botName.isNotBlank() && pyCode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TelegramCyan, contentColor = Color(0xFF041E2F))
            ) {
                Text("Saqlash", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish", color = TextSecondary)
            }
        }
    )
}
