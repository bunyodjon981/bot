package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TelegramAzure
import com.example.ui.theme.TelegramCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletTech
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSplashScreen(
    onSplashFinished: () -> Unit
) {
    val logoScale = remember { Animatable(0.55f) }
    val contentAlpha = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_orbit")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitAngle"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            contentAlpha.animateTo(1f, animationSpec = tween(700))
        }
        launch {
            progressAnim.animateTo(1f, animationSpec = tween(1700, easing = FastOutSlowInEasing))
        }
        delay(1900)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F2147),
                        StudioBg
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated Cyber Orbit Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f - 40.dp.toPx())
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TelegramCyan.copy(alpha = 0.22f), Color.Transparent),
                    center = center,
                    radius = 190.dp.toPx() * glowPulse
                ),
                center = center,
                radius = 190.dp.toPx() * glowPulse
            )

            rotate(orbitAngle, pivot = center) {
                drawCircle(
                    color = TelegramCyan.copy(alpha = 0.35f),
                    radius = 105.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = TelegramCyan,
                    radius = 5.dp.toPx(),
                    center = Offset(center.x + 105.dp.toPx(), center.y)
                )
                drawCircle(
                    color = NeonEmerald,
                    radius = 4.dp.toPx(),
                    center = Offset(center.x - 105.dp.toPx(), center.y)
                )
            }

            rotate(-orbitAngle * 0.7f, pivot = center) {
                drawCircle(
                    color = VioletTech.copy(alpha = 0.25f),
                    radius = 138.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
                drawCircle(
                    color = VioletTech,
                    radius = 4.dp.toPx(),
                    center = Offset(center.x, center.y - 138.dp.toPx())
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .alpha(contentAlpha.value)
        ) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .scale(logoScale.value)
                    .clip(RoundedCornerShape(30.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(TelegramCyan, NeonEmerald)),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .background(StudioSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_art),
                    contentDescription = "BOT BUILDERS uz emblemasi",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "BOT BUILDERS uz",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Telegram Python Botlar • 24/7 Studio",
                style = MaterialTheme.typography.bodyLarge,
                color = TelegramCyan
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(220.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progressAnim.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = TelegramCyan,
                    trackColor = StudioSurfaceElevated
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Python Engine yuklanmoqda...",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
