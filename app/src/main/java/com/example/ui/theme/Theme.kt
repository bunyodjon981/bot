package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BotBuildersDarkColorScheme = darkColorScheme(
    primary = TelegramCyan,
    onPrimary = Color(0xFF041E2F),
    primaryContainer = TelegramDeepBlue,
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = NeonEmerald,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = NeonEmeraldSoft,
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = GoldSom,
    onTertiary = Color(0xFF271300),
    tertiaryContainer = GoldSomSoft,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = StudioBg,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = StudioCardBorder,
    outlineVariant = Color(0xFF1E293B),
    error = CoralRed,
    onError = Color.White
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BotBuildersTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BotBuildersDarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
