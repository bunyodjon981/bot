package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BotTemplate
import com.example.domain.model.CatalogFilter
import com.example.domain.model.formatSom
import com.example.ui.components.BotAvatarImage
import com.example.ui.theme.GoldSom
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TelegramAzure
import com.example.ui.theme.TelegramCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletTech

@Composable
fun BotCatalogScreen(
    templates: List<BotTemplate>,
    searchQuery: String,
    selectedFilter: CatalogFilter,
    onSearchChange: (String) -> Unit,
    onFilterSelect: (CatalogFilter) -> Unit,
    onRefreshTemplates: () -> Unit,
    onBotClick: (BotTemplate) -> Unit
) {
    val filtered = templates.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
            item.name.contains(searchQuery, ignoreCase = true) ||
            item.category.contains(searchQuery, ignoreCase = true) ||
            item.shortTagline.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            CatalogFilter.ALL -> true
            CatalogFilter.FREE -> item.priceSom == 0L
            CatalogFilter.PAID -> item.priceSom > 0L
            CatalogFilter.CUSTOM -> item.isCustomImported
        }
        matchesQuery && matchesFilter
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
    ) {
        // Strictly 2 columns on handheld phones ("har bitta joyiga 2 ta DAN bo'ladi"), adaptive on tablets
        val columnsCount = if (maxWidth >= 720.dp) 3 else 2

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("bot_catalog_grid")
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Promo Studio Header Banner
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, TelegramCyan.copy(alpha = 0.4f)),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0E294B),
                                            Color(0xFF13203D),
                                            Color(0xFF1B1942)
                                        )
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = TelegramCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "BOT OCHISH KATALOGI",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = TelegramCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Python Telegram botni tanlang va telefonda 24/7 ishga tushiring",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary.copy(alpha = 0.9f)
                                    )
                                }
                                IconButton(
                                    onClick = onRefreshTemplates,
                                    modifier = Modifier.testTag("refresh_catalog_button")
                                ) {
                                    Icon(
                                        Icons.Rounded.Refresh,
                                        contentDescription = "Fayllarni yangilash",
                                        tint = TelegramCyan
                                    )
                                }
                            }
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Bot qidirish...", color = TextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = TelegramCyan)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Tozalash", tint = TextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("catalog_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = StudioCardBg,
                            unfocusedContainerColor = StudioCardBg,
                            focusedBorderColor = TelegramCyan,
                            unfocusedBorderColor = StudioCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CatalogFilter.entries) { filter ->
                            val selected = filter == selectedFilter
                            FilterChip(
                                selected = selected,
                                onClick = { onFilterSelect(filter) },
                                label = {
                                    Text(
                                        text = filter.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TelegramCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = TelegramCyan,
                                    containerColor = StudioCardBg,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = StudioCardBorder,
                                    selectedBorderColor = TelegramCyan
                                )
                            )
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp)
                    ) {
                        Icon(
                            Icons.Rounded.SmartToy,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bot topilmadi",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fayllar bo'limidan yangi bot.py yuklashingiz mumkin",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(filtered, key = { it.id }) { bot ->
                    AnimatedVisibility(visible = true, enter = fadeIn()) {
                        BotFramedGridCard(
                            bot = bot,
                            onClick = { onBotClick(bot) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BotFramedGridCard(
    bot: BotTemplate,
    onClick: () -> Unit
) {
    val isFree = bot.priceSom == 0L
    val frameColor = when {
        bot.isCustomImported -> VioletTech.copy(alpha = 0.75f)
        isFree -> NeonEmerald.copy(alpha = 0.75f)
        else -> StudioCardBorder
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bot_card_${bot.id}")
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = BorderStroke(1.6.dp, frameColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bot Image inside inner frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioSurfaceElevated)
            ) {
                BotAvatarImage(
                    drawableResName = bot.drawableResName,
                    iconFilePath = bot.iconFilePath,
                    contentDescription = "${bot.name} rasmi",
                    modifier = Modifier.fillMaxSize()
                )

                // Top badge (Bepul / Maxsus / Version)
                val badgeText = when {
                    bot.isCustomImported -> "YUKLANGAN"
                    isFree -> "BEPUL"
                    else -> "v${bot.version}"
                }
                val badgeColor = when {
                    bot.isCustomImported -> VioletTech
                    isFree -> NeonEmerald
                    else -> TelegramAzure
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(7.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC070C18),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bot Name below image
            Text(
                text = bot.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = bot.category,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Price Tag Box at bottom of card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = if (isFree) NeonEmerald.copy(alpha = 0.14f) else GoldSom.copy(alpha = 0.14f),
                border = BorderStroke(
                    1.dp,
                    if (isFree) NeonEmerald.copy(alpha = 0.5f) else GoldSom.copy(alpha = 0.55f)
                )
            ) {
                Text(
                    text = if (isFree) "0 so'm • Bepul" else formatSom(bot.priceSom),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isFree) NeonEmerald else GoldSom,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }
        }
    }
}
