package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnimatedAmountCounter(
    amount: Double,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    color: Color = textStyle.color,
    animationDurationMillis: Int = 1200,
    staggerMillis: Int = 40,
    animateOnInitialMount: Boolean = true
) {
    var oldAmount by remember { mutableDoubleStateOf(amount) }
    val isIncreasing = amount >= oldAmount
    var isInitialMount by remember { mutableStateOf(!animateOnInitialMount) }

    LaunchedEffect(amount) {
        if (isInitialMount) {
            isInitialMount = false
        }
        oldAmount = amount
    }

    val formattedString = remember(amount) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        if (amount % 1.0 == 0.0) {
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0
        } else {
            formatter.maximumFractionDigits = 2
            formatter.minimumFractionDigits = 2
        }
        formatter.format(amount)
    }

    // Use tabular numbers (tnum) to prevent layout jitter when digits roll
    val tabularStyle = textStyle.copy(
        fontFeatureSettings = textStyle.fontFeatureSettings?.let { "$it, tnum" } ?: "tnum"
    )

    Row(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .semantics(mergeDescendants = true) {
                contentDescription = formattedString
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val decimalIndex = formattedString.indexOf('.').let { if (it == -1) formattedString.length else it }
        
        formattedString.forEachIndexed { i, char ->
            val placeIndex = decimalIndex - i
            val charKey = if (char.isDigit()) "digit_$placeIndex" else "char_${placeIndex}_$char"
            val delay = (formattedString.length - 1 - i) * staggerMillis
            
            key(charKey) {
                if (char.isDigit()) {
                    RollingDigit(
                        targetDigit = char,
                        isIncreasing = isIncreasing,
                        isInitial = isInitialMount,
                        textStyle = tabularStyle,
                        color = color,
                        durationMillis = animationDurationMillis,
                        delayMillis = delay
                    )
                } else {
                    Text(
                        text = char.toString(),
                        style = tabularStyle,
                        color = color,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
private fun RollingDigit(
    targetDigit: Char,
    isIncreasing: Boolean,
    isInitial: Boolean,
    textStyle: TextStyle,
    color: Color,
    durationMillis: Int,
    delayMillis: Int
) {
    val targetVal = targetDigit.digitToInt()
    var currentDigit by remember { mutableFloatStateOf(if (isInitial) targetVal.toFloat() else 0f) }
    var startDigit by remember { mutableFloatStateOf(currentDigit) }
    var targetDigitValue by remember { mutableFloatStateOf(currentDigit) }

    LaunchedEffect(targetVal, isIncreasing) {
        if (targetVal.toFloat() == currentDigit && !isInitial) return@LaunchedEffect
        
        if (isInitial) {
            currentDigit = targetVal.toFloat()
            startDigit = currentDigit
            targetDigitValue = currentDigit
            return@LaunchedEffect
        }
        
        startDigit = currentDigit
        var target = targetVal.toFloat()
        
        // Determine shortest directional path
        if (isIncreasing && target < currentDigit) {
            target += 10f
        } else if (!isIncreasing && target > currentDigit) {
            target -= 10f
        }
        
        targetDigitValue = target
        
        animate(
            initialValue = currentDigit,
            targetValue = target,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.05f) // Slight visual overshoot
            )
        ) { value, _ ->
            currentDigit = value
        }
        
        // Settle perfectly on the integer
        currentDigit = targetVal.toFloat()
        startDigit = currentDigit
        targetDigitValue = currentDigit
    }

    val progress = if (targetDigitValue == startDigit) 0f else {
        ((currentDigit - startDigit) / (targetDigitValue - startDigit)).coerceIn(0f, 1f)
    }
    
    // Max blur when half-way through the animation
    val maxBlur = 3f
    val blurIntensity = (Math.sin(progress.toDouble() * Math.PI).toFloat() * maxBlur).coerceAtLeast(0f)
    
    val positiveValue = (currentDigit % 10f).let { if (it < 0) it + 10f else it }
    val currentIndex = positiveValue.toInt()
    val nextIndex = (currentIndex + 1) % 10
    val fraction = positiveValue - currentIndex
    
    Box(modifier = Modifier.clipToBounds()) {
        // Hidden text purely for reserving precise bounding box size
        Text(
            text = "8",
            style = textStyle,
            color = Color.Transparent
        )
        
        // --- Current Digit (Moving Out) ---
        Text(
            text = currentIndex.toString(),
            style = textStyle,
            color = color,
            modifier = Modifier
                .graphicsLayer {
                    translationY = -fraction * size.height
                    // Optional: Fade out slightly as it moves away from center
                    alpha = 1f - (fraction * 0.5f)
                }
                .blur(
                    radiusX = 0.dp,
                    radiusY = blurIntensity.dp,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                )
        )
        
        // --- Next Digit (Moving In) ---
        Text(
            text = nextIndex.toString(),
            style = textStyle,
            color = color,
            modifier = Modifier
                .graphicsLayer {
                    translationY = (1f - fraction) * size.height
                    // Optional: Fade in slightly as it moves towards center
                    alpha = 0.5f + (fraction * 0.5f)
                }
                .blur(
                    radiusX = 0.dp,
                    radiusY = blurIntensity.dp,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                )
        )
    }
}
