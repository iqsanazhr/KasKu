package com.example.kasku.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoCoral
import com.example.kasku.ui.theme.MonzoNavy
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTealBorder
import com.example.kasku.ui.theme.MonzoTealLight
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import com.example.kasku.ui.theme.MonzoTextTertiary
import kotlin.math.roundToInt

/**
 * Representasi satu langkah dalam interactive spotlight tour.
 */
data class TutorialStep(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val icon: ImageVector,
    val targetRect: Rect?
)

/**
 * Overlay panduan interaktif Monzo dengan spotlight cutout,
 * pulsing glow selection border, dan organic curved pointer dengan glowing beacon.
 */
@Composable
fun FeatureTutorialOverlay(
    steps: List<TutorialStep>,
    currentStepIndex: Int,
    onNextStep: () -> Unit,
    onSkipTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (steps.isEmpty() || currentStepIndex !in steps.indices) return

    val currentStep = steps[currentStepIndex]
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "tutorial_glow")

    // Animasi denyut halus untuk border seleksi cutout
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Animasi riak beacon penunjuk
    val beaconPulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beaconPulse"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .zIndex(9999f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Menahan klik agar tidak tembus ke background */ }
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val targetRect = currentStep.targetRect

        // Padding tambahan di sekeliling elemen yang di-highlight
        val cutoutPaddingPx = with(density) { 6.dp.toPx() }
        val cornerRadiusPx = with(density) { 18.dp.toPx() }

        val paddedCutout = targetRect?.let { rect ->
            Rect(
                left = (rect.left - cutoutPaddingPx).coerceAtLeast(4f),
                top = (rect.top - cutoutPaddingPx).coerceAtLeast(4f),
                right = (rect.right + cutoutPaddingPx).coerceAtMost(screenWidthPx - 4f),
                bottom = (rect.bottom + cutoutPaddingPx).coerceAtMost(screenHeightPx - 4f)
            )
        }

        // Tentukan apakah dialog diletakkan di bawah atau di atas target
        val estimatedDialogHeightPx = with(density) { 240.dp.toPx() }
        val isDialogBelow = if (paddedCutout != null) {
            (paddedCutout.bottom + estimatedDialogHeightPx + 40f) <= screenHeightPx
        } else {
            true
        }

        // =========================================================================
        // 1. CANVAS SPOTLIGHT MASK & ORGANIC CURVED POINTER
        // =========================================================================
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            // A. Darkened Scrim (Monzo Dark Navy Overlay)
            drawRect(
                color = Color(0xC408131E)
            )

            // B. Cutout Spotlight pada area target
            if (paddedCutout != null) {
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(paddedCutout.left, paddedCutout.top),
                    size = Size(paddedCutout.width, paddedCutout.height),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    blendMode = BlendMode.Clear
                )

                // C. Glowing Selection Ring di sekeliling cutout
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MonzoTeal.copy(alpha = pulseAlpha),
                            MonzoCoral.copy(alpha = pulseAlpha * 0.9f),
                            MonzoTeal.copy(alpha = pulseAlpha)
                        ),
                        start = Offset(paddedCutout.left, paddedCutout.top),
                        end = Offset(paddedCutout.right, paddedCutout.bottom)
                    ),
                    topLeft = Offset(paddedCutout.left, paddedCutout.top),
                    size = Size(paddedCutout.width, paddedCutout.height),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // D. Organic Curved Pointer & Glowing Beacon
                // Titik target pada tepi cutout
                val targetCenterPoint = if (isDialogBelow) {
                    Offset(
                        x = (paddedCutout.left + paddedCutout.right) / 2f,
                        y = paddedCutout.bottom
                    )
                } else {
                    Offset(
                        x = (paddedCutout.left + paddedCutout.right) / 2f,
                        y = paddedCutout.top
                    )
                }

                // Titik sambungan dialog (Anchor)
                val dialogAnchorY = if (isDialogBelow) {
                    (paddedCutout.bottom + with(density) { 32.dp.toPx() })
                } else {
                    (paddedCutout.top - with(density) { 32.dp.toPx() })
                }
                val dialogAnchorX = (screenWidthPx / 2f).coerceIn(
                    paddedCutout.left - 20f,
                    paddedCutout.right + 20f
                )
                val dialogAnchorPoint = Offset(dialogAnchorX, dialogAnchorY)

                // Organic Spline Path (Bukan panah lancip biasa!)
                val path = Path().apply {
                    moveTo(dialogAnchorPoint.x, dialogAnchorPoint.y)
                    val deltaY = (targetCenterPoint.y - dialogAnchorPoint.y)
                    val controlXOffset = if (targetCenterPoint.x < screenWidthPx / 2f) 30f else -30f

                    cubicTo(
                        x1 = dialogAnchorPoint.x + controlXOffset,
                        y1 = dialogAnchorPoint.y + deltaY * 0.45f,
                        x2 = targetCenterPoint.x - controlXOffset,
                        y2 = dialogAnchorPoint.y + deltaY * 0.85f,
                        x3 = targetCenterPoint.x,
                        y3 = targetCenterPoint.y
                    )
                }

                // Gambar Garis Kurva Halus
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(MonzoTeal, MonzoCoral),
                        start = dialogAnchorPoint,
                        end = targetCenterPoint
                    ),
                    style = Stroke(
                        width = 2.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // Titik jangkar pada dialog
                drawCircle(
                    color = MonzoTeal,
                    radius = 3.5.dp.toPx(),
                    center = dialogAnchorPoint
                )

                // Pulsing Beacon Ripple Halo di titik target
                val rippleMaxRadius = with(density) { 16.dp.toPx() }
                val currentRippleRadius = rippleMaxRadius * beaconPulseProgress
                val rippleAlpha = (1f - beaconPulseProgress).coerceIn(0f, 1f) * 0.8f

                drawCircle(
                    color = MonzoCoral.copy(alpha = rippleAlpha),
                    radius = currentRippleRadius,
                    center = targetCenterPoint
                )

                // Solid Core Beacon Dot
                drawCircle(
                    color = MonzoCoral,
                    radius = 5.dp.toPx(),
                    center = targetCenterPoint
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = targetCenterPoint
                )
            }
        }

        // =========================================================================
        // 2. MONZO GUIDANCE DIALOG CARD
        // =========================================================================
        val dialogVerticalOffset = if (paddedCutout != null) {
            if (isDialogBelow) {
                (paddedCutout.bottom + with(density) { 32.dp.toPx() }).roundToInt()
            } else {
                (paddedCutout.top - estimatedDialogHeightPx - with(density) { 32.dp.toPx() })
                    .coerceAtLeast(with(density) { 16.dp.toPx() }).roundToInt()
            }
        } else {
            // Fallback terpusat secara vertikal jika target belum siap
            ((screenHeightPx - estimatedDialogHeightPx) / 2f).roundToInt()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = 0, y = dialogVerticalOffset) }
                .padding(horizontal = 20.dp)
                .zIndex(10f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0x33000000),
                        spotColor = Color(0x660B1C2A)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = MonzoSurface,
                border = BorderStroke(1.dp, MonzoTealBorder.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Card: Badge Langkah & Tombol Lewati / Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MonzoTealLight,
                            border = BorderStroke(0.8.dp, MonzoTeal.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = currentStep.icon,
                                    contentDescription = null,
                                    tint = MonzoTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "LANGKAH ${currentStepIndex + 1} DARI ${steps.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MonzoTeal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        letterSpacing = 0.6.sp
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onSkipTutorial,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Tutup Tutorial",
                                tint = MonzoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Konten Teks Langkah (dengan transisi halus saat berganti langkah)
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220)) + slideInHorizontally { width -> width / 4 })
                                .togetherWith(
                                    fadeOut(animationSpec = tween(180)) + slideOutHorizontally { width -> -width / 4 }
                                )
                        },
                        label = "TutorialTextAnimation"
                    ) { step ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MonzoNavy,
                                    fontSize = 17.5.sp
                                )
                            )
                            Text(
                                text = step.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.5.sp
                                )
                            )
                        }
                    }

                    // Bagian Bawah: Step Progress Dots & Tombol Aksi Lanjut / Selesai
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dot Indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            steps.indices.forEach { index ->
                                val isCurrent = index == currentStepIndex
                                Box(
                                    modifier = Modifier
                                        .height(5.dp)
                                        .width(if (isCurrent) 18.dp else 5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCurrent) MonzoTeal else MonzoBorder
                                        )
                                )
                            }
                        }

                        // Tombol Aksi
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isLastStep = currentStepIndex == steps.size - 1

                            TextButton(
                                onClick = onSkipTutorial,
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(
                                    text = "Lewati",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MonzoTextTertiary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            Button(
                                onClick = onNextStep,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLastStep) MonzoCoral else MonzoTeal,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = if (isLastStep) "Selesai" else "Lanjut",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                    )
                                    Icon(
                                        imageVector = if (isLastStep) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
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
