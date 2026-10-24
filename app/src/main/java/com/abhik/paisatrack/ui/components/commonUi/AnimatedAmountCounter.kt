package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.DpSize
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

private val RollEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f) // ease-out-cubic, no overshoot

// Plain holders: mutating these must NOT trigger recomposition.
private class AmountTracker(var last: Double)
private class FirstFlag(var value: Boolean = true)

@Composable
fun AnimatedAmountCounter(
    amount: Double,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    color: Color = textStyle.color,
    animationDurationMillis: Int = 900,
    staggerMillis: Int = 30,
    animateOnInitialMount: Boolean = true
) {
    // Formatter is created once, not per amount change.
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")) }
    val formatted = remember(amount) {
        val whole = Math.round(amount * 100) % 100L == 0L
        val digits = if (whole) 0 else 2
        formatter.minimumFractionDigits = digits
        formatter.maximumFractionDigits = digits
        formatter.format(amount)
    }

    // Direction is computed once per amount change and never flips on the follow-up recomposition.
    val tracker = remember { AmountTracker(amount) }
    val direction = remember(amount) {
        val d = if (amount >= tracker.last) 1 else -1
        tracker.last = amount
        d
    }

    val first = remember { FirstFlag() }
    val startAtTarget = first.value && !animateOnInitialMount
    SideEffect { first.value = false }

    // Resolve style/color exactly as Text() would, so drawn glyphs match the plain Text() chars.
    val baseStyle = LocalTextStyle.current.merge(textStyle)
    val style = remember(baseStyle) {
        baseStyle.copy(
            fontFeatureSettings = baseStyle.fontFeatureSettings?.let { "$it, tnum" } ?: "tnum"
        )
    }
    val resolvedColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

    // Measure 0-9 once; every digit cell shares these layouts.
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val glyphs = remember(style, measurer) { List(10) { measurer.measure("$it", style) } }
    val cell = remember(glyphs, density) {
        val w = glyphs.maxOf { it.size.width }
        val h = glyphs.maxOf { it.size.height }
        with(density) { DpSize(w.toDp(), h.toDp()) }
    }

    Row(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            .clearAndSetSemantics { contentDescription = formatted },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val decimalIndex = formatted.indexOf('.').let { if (it == -1) formatted.length else it }

        formatted.forEachIndexed { i, ch ->
            val place = decimalIndex - i
            val delay = (formatted.length - 1 - i) * staggerMillis

            key(if (ch.isDigit()) "d$place" else "c${place}_$ch") {
                if (ch.isDigit()) {
                    RollingDigit(
                        digit = ch.digitToInt(),
                        direction = direction,
                        glyphs = glyphs,
                        cell = cell,
                        color = resolvedColor,
                        startAtTarget = startAtTarget,
                        durationMillis = animationDurationMillis,
                        delayMillis = delay
                    )
                } else {
                    Text(text = ch.toString(), style = style, color = resolvedColor)
                }
            }
        }
    }
}

@Composable
private fun RollingDigit(
    digit: Int,
    direction: Int,
    glyphs: List<TextLayoutResult>,
    cell: DpSize,
    color: Color,
    startAtTarget: Boolean,
    durationMillis: Int,
    delayMillis: Int
) {
    // Unbounded "odometer position". Only ever read in the draw phase -> zero recomposition per frame.
    val position = remember { Animatable(if (startAtTarget) digit.toFloat() else 0f) }

    LaunchedEffect(digit) {
        val from = position.value
        val settled = abs(from - from.roundToInt()) < 0.001f

        val delta = when {
            // Mid-flight retarget: take the shortest path so it never does a full lap.
            !settled -> (digit - from).posMod(10f).let { if (it > 5f) it - 10f else it }
            direction >= 0 -> (digit - from).posMod(10f)
            else -> -(from - digit).posMod(10f)
        }

        if (abs(delta) < 0.001f) {
            position.snapTo(digit.toFloat())
            return@LaunchedEffect
        }

        position.animateTo(
            targetValue = from + delta,
            animationSpec = tween(durationMillis, delayMillis, RollEasing)
        )
        position.snapTo(digit.toFloat()) // normalize back to 0..9, visually identical
    }

    Canvas(Modifier.size(cell).clipToBounds()) {
        val p = position.value
        val base = floor(p)
        val frac = p - base
        val cur = base.toInt().mod(10)
        val next = (cur + 1) % 10
        val h = size.height

        drawGlyph(glyphs[cur], color, y = -frac * h, alpha = 1f - frac)
        if (frac > 0.001f) {
            drawGlyph(glyphs[next], color, y = (1f - frac) * h, alpha = frac)
        }
    }
}

private fun DrawScope.drawGlyph(layout: TextLayoutResult, color: Color, y: Float, alpha: Float) {
    drawText(
        textLayoutResult = layout,
        color = color,
        topLeft = Offset(
            x = (size.width - layout.size.width) / 2f,
            y = y + (size.height - layout.size.height) / 2f
        ),
        alpha = alpha
    )
}

private fun Float.posMod(m: Float): Float = ((this % m) + m) % m