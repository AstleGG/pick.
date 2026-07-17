package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SpinWheel(
    options: List<String>,
    isSpinning: Boolean,
    onSpinFinished: (String) -> Unit,
    viewModel: PickViewModel,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    val sectorAngle = 360f / options.size
    val hapticFeedback = LocalHapticFeedback.current

    // State to track if spin was triggered locally
    var lastSpinTriggered by remember { mutableStateOf(false) }

    // Sound tick tracking
    var lastTickSector by remember { mutableStateOf(-1) }

    LaunchedEffect(isSpinning) {
        if (isSpinning && !lastSpinTriggered) {
            lastSpinTriggered = true
            
            // Trigger haptic feedback on start
            if (viewModel.isHapticEnabled) {
                try {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            val winningIndex = Random.nextInt(options.size)
            
            // Align the winning sector under the top pointer (at 270 degrees)
            val targetAngle = 270f - (winningIndex * sectorAngle + sectorAngle / 2f)
            val totalTargetRotation = (360f * 5f) + targetAngle
            
            rotation.animateTo(
                targetValue = totalTargetRotation,
                animationSpec = tween(durationMillis = 3500, easing = EaseOutQuart)
            ) {
                val currentSector = ((value % 360f) / sectorAngle).toInt()
                if (currentSector != lastTickSector) {
                    lastTickSector = currentSector
                    try {
                        SoundEffects.playTick()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            // Trigger haptic feedback on stop
            if (viewModel.isHapticEnabled) {
                try {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            lastSpinTriggered = false
            onSpinFinished(options[winningIndex])
        } else if (!isSpinning) {
            rotation.snapTo(rotation.value % 360f)
        }
    }

    Box(
        modifier = modifier
            .size(280.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val surfaceColor = MaterialTheme.colorScheme.surface
        val variantColor = MaterialTheme.colorScheme.surfaceVariant
        val outlineColor = MaterialTheme.colorScheme.outline
        val primaryColor = MaterialTheme.colorScheme.primary
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, outlineColor, CircleShape)
                .clip(CircleShape)
        ) {
            val radius = size.minDimension / 2f
            val canvasCenter = Offset(size.width / 2f, size.height / 2f)

            // Draw alternating colored sectors
            for (i in options.indices) {
                val startAngle = i * sectorAngle
                val sectorBgColor = when {
                    options.size % 2 != 0 && i == options.size - 1 -> outlineColor.copy(alpha = 0.2f)
                    i % 2 == 0 -> surfaceColor
                    else -> variantColor
                }

                // Draw sector arc
                drawArc(
                    color = sectorBgColor,
                    startAngle = startAngle + rotation.value,
                    sweepAngle = sectorAngle,
                    useCenter = true,
                    size = size
                )

                // Draw sector divider lines
                val angleRad = Math.toRadians((startAngle + rotation.value).toDouble())
                val edgePoint = Offset(
                    x = canvasCenter.x + radius * cos(angleRad).toFloat(),
                    y = canvasCenter.y + radius * sin(angleRad).toFloat()
                )
                drawLine(
                    color = outlineColor,
                    start = canvasCenter,
                    end = edgePoint,
                    strokeWidth = 1.dp.toPx()
                )

                // Draw sector text
                withTransform({
                    rotate(
                        degrees = startAngle + rotation.value + sectorAngle / 2f,
                        pivot = canvasCenter
                    )
                }) {
                    val maxTextWidth = radius * 0.65f
                    val textLayoutResult = textMeasurer.measure(
                        text = options[i],
                        style = TextStyle(
                            color = onSurfaceColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        ),
                        constraints = Constraints(
                            maxWidth = maxTextWidth.toInt()
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val textWidth = textLayoutResult.size.width
                    val textHeight = textLayoutResult.size.height
                    
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = canvasCenter.x + radius - textWidth - 16.dp.toPx(),
                            y = canvasCenter.y - textHeight / 2f
                        )
                    )
                }
            }

            // Draw clean outer border
            drawCircle(
                color = outlineColor,
                radius = radius,
                center = canvasCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Center hub (physical anchor point)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .border(2.dp, outlineColor, CircleShape)
                .clickable(enabled = !isSpinning) {
                    if (!isSpinning) {
                        viewModel.startSpinning()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SPIN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = if (isSpinning) MaterialTheme.colorScheme.secondary else primaryColor
            )
        }

        // Pointer (always at the very top pointing downwards)
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasCenter = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            val pointerWidth = 14.dp.toPx()
            val pointerHeight = 16.dp.toPx()
            val pointerTopY = canvasCenter.y - radius - 6.dp.toPx()

            val path = Path().apply {
                moveTo(canvasCenter.x - pointerWidth / 2f, pointerTopY)
                lineTo(canvasCenter.x + pointerWidth / 2f, pointerTopY)
                lineTo(canvasCenter.x, pointerTopY + pointerHeight)
                close()
            }

            drawPath(
                path = path,
                color = outlineColor
            )
            
            val innerPath = Path().apply {
                moveTo(canvasCenter.x - (pointerWidth - 3f) / 2f, pointerTopY)
                lineTo(canvasCenter.x + (pointerWidth - 3f) / 2f, pointerTopY)
                lineTo(canvasCenter.x, pointerTopY + pointerHeight - 2f)
                close()
            }
            drawPath(
                path = innerPath,
                color = primaryColor
            )
        }
    }
}
