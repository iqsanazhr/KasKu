package com.example.kasku.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.ui.theme.CeramicBackground
import com.example.kasku.ui.theme.MonzoNavy
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTextSecondary
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/**
 * Super Smooth Liquid Splash Screen with Continuous Master Timeline & Wave Loading
 */
@Composable
fun KasKuSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Single Continuous Master Progress (0f -> 1f) for buttery 120fps fluid motion
    val masterTimeline = remember { Animatable(0f) }

    // Subtle gentle breathing pulse after drawing completes
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    LaunchedEffect(Unit) {
        // Run single continuous fluid bezier animation without any abrupt step delays
        masterTimeline.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1400,
                easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f) // Apple standard fluid curve
            )
        )

        // Hold smoothly so user experiences the complete splash branding
        delay(600)
        onSplashFinished()
    }

    val progress = masterTimeline.value

    // Smoothstep mapping helper: provides silky S-curve acceleration & deceleration
    fun smoothRange(start: Float, end: Float): Float {
        if (progress <= start) return 0f
        if (progress >= end) return 1f
        val t = (progress - start) / (end - start)
        return t * t * (3f - 2f * t)
    }

    // Dynamic timeline sub-progresses
    val cardScale = 0.5f + 0.5f * smoothRange(0.0f, 0.40f)
    val stemProgress = smoothRange(0.12f, 0.48f)
    val armProgress = smoothRange(0.32f, 0.65f)
    val legProgress = smoothRange(0.48f, 0.80f)
    val expProgress = smoothRange(0.68f, 0.95f)
    val textAlpha = smoothRange(0.60f, 0.95f)
    val textSlideY = (1f - textAlpha) * 14f // 14dp subtle upward slide

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CeramicBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Vector Canvas Logo with Smooth Shadow
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .scale(cardScale * pulseScale),
                contentAlignment = Alignment.Center
            ) {
                SuperSmoothK2Canvas(
                    stemProgress = stemProgress,
                    armProgress = armProgress,
                    legProgress = legProgress,
                    expProgress = expProgress,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Branding Text with Smooth Fade & Upward Slide
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textSlideY.dp)
                    .alpha(textAlpha)
            ) {
                Text(
                    text = "KasKu",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MonzoNavy,
                        fontSize = 28.sp,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "Smart Cash Flow & Personal AI",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MonzoTextSecondary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(38.dp))

            // Wave Loading Dots (Undulating Sinusoidal Wave)
            UndulatingWaveLoadingDots(modifier = Modifier.alpha(textAlpha))
        }
    }
}

/**
 * Super Smooth Vector Canvas with Anti-Aliased Rounded Cap Terminals
 */
@Composable
private fun SuperSmoothK2Canvas(
    stemProgress: Float,
    armProgress: Float,
    legProgress: Float,
    expProgress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cardRadius = w * 0.28f

        // 1. Soft Gradient Card
        val cardBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFF0654F), Color(0xFFEB5B44))
        )
        drawRoundRect(
            brush = cardBrush,
            size = Size(w, h),
            cornerRadius = CornerRadius(cardRadius, cardRadius)
        )

        // Coordinates & Stroke Widths
        val strokeK = w * 0.088f
        val stroke2 = w * 0.044f
        val white = Color.White

        val stemX = w * 0.355f
        val stemY1 = h * 0.29f
        val stemY2 = h * 0.71f

        // Draw Vertical Stem of K
        if (stemProgress > 0f) {
            val curStemY2 = stemY1 + (stemY2 - stemY1) * stemProgress
            drawLine(
                color = white,
                start = Offset(stemX, stemY1),
                end = Offset(stemX, curStemY2),
                strokeWidth = strokeK,
                cap = StrokeCap.Round
            )
        }

        // Draw Upper Arm of K
        if (armProgress > 0f) {
            val armStartX = stemX
            val armStartY = h * 0.51f
            val armEndX = w * 0.585f
            val armEndY = h * 0.29f

            val curArmX = armStartX + (armEndX - armStartX) * armProgress
            val curArmY = armStartY + (armEndY - armStartY) * armProgress

            drawLine(
                color = white,
                start = Offset(armStartX, armStartY),
                end = Offset(curArmX, curArmY),
                strokeWidth = strokeK,
                cap = StrokeCap.Round
            )
        }

        // Draw Lower Leg of K
        if (legProgress > 0f) {
            val legStartX = w * 0.435f
            val legStartY = h * 0.455f
            val legEndX = w * 0.605f
            val legEndY = h * 0.71f

            val curLegX = legStartX + (legEndX - legStartX) * legProgress
            val curLegY = legStartY + (legEndY - legStartY) * legProgress

            drawLine(
                color = white,
                start = Offset(legStartX, legStartY),
                end = Offset(curLegX, curLegY),
                strokeWidth = strokeK,
                cap = StrokeCap.Round
            )
        }

        // Draw Exponent '²'
        if (expProgress > 0f) {
            scale(scale = expProgress, pivot = Offset(w * 0.68f, h * 0.37f)) {
                val path2 = Path().apply {
                    val expX = w * 0.655f
                    val expY = h * 0.32f
                    val arcR = w * 0.038f

                    moveTo(expX, expY + arcR)
                    cubicTo(
                        expX, expY,
                        expX + arcR * 2, expY,
                        expX + arcR * 2, expY + arcR
                    )
                    lineTo(expX, expY + arcR * 2.3f)
                    lineTo(expX + arcR * 2.1f, expY + arcR * 2.3f)
                }

                drawPath(
                    path = path2,
                    color = white.copy(alpha = expProgress.coerceIn(0f, 1f)),
                    style = Stroke(
                        width = stroke2,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

/**
 * Undulating Wave Loading Dots (Bulatan Bergerak Bergelombang Sinusoidal)
 */
@Composable
private fun UndulatingWaveLoadingDots(
    modifier: Modifier = Modifier,
    dotCount: Int = 4,
    dotSize: Float = 7.5f,
    waveAmplitude: Float = 6.5f // Jarak gelombang naik-turun (dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveLoadingTransition")

    // Continuous time variable 0f -> 2*PI for seamless sinusoidal cycle
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until dotCount) {
            // Phase offset creates the travelling sine wave across dots
            val phaseOffset = i * 0.65f
            val sineVal = sin(wavePhase - phaseOffset)

            // Wave offset on Y axis (-waveAmplitude .. +waveAmplitude)
            val yOffset = sineVal * waveAmplitude
            // Synchronized scale and alpha for high-end polish
            val scaleVal = 0.85f + 0.25f * ((sineVal + 1f) / 2f)
            val alphaVal = 0.45f + 0.55f * ((sineVal + 1f) / 2f)

            Box(
                modifier = Modifier
                    .offset(y = yOffset.dp)
                    .scale(scaleVal)
                    .size(dotSize.dp)
                    .alpha(alphaVal)
                    .background(MonzoTeal, CircleShape)
            )
        }
    }
}
