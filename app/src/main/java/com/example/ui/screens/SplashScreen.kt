package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1500, easing = EaseInOutCubic),
        label = "LogoAlpha"
    )

    val glowScale by rememberInfiniteTransition(label = "GlowPulse").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Red radial glowing background effect
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(glowScale)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryRed.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
        ) {
            Text(
                text = "DROP",
                fontSize = 58.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 8.sp,
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "SOCIETY",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed,
                letterSpacing = 12.sp,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
