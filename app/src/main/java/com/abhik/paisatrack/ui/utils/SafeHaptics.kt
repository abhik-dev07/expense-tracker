package com.abhik.paisatrack.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.swmansion.pulsar.Pulsar
import com.swmansion.pulsar.types.RealtimeComposerStrategy

/**
 * Resolve the hosting [Activity] from any [Context]. Inside a Compose Dialog/Popup
 * (e.g. ExpandedFullScreenSearchBar, ModalBottomSheet) `LocalContext.current` is a
 * ContextThemeWrapper rather than the Activity. Pulsar casts its context to Activity
 * in createPresets(), so passing the wrapper directly crashes with ClassCastException.
 * Walk the baseContext chain to recover the Activity before constructing Pulsar.
 */
fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

class SafePresets(private val delegate: Any?) {
    private fun invokeSafe(methodName: String) {
        val target = delegate ?: return
        try {
            val method = target.javaClass.getMethod(methodName)
            method.invoke(target)
        } catch (t: Throwable) {
            // Suppress haptic vibration errors on devices with invalid amplitude support
        }
    }

    fun ping() = invokeSafe("ping")
    fun plunk() = invokeSafe("plunk")
    fun boulder() = invokeSafe("boulder")
    fun catPaw() = invokeSafe("catPaw")
    fun bassDrop() = invokeSafe("bassDrop")
    fun cleave() = invokeSafe("cleave")
    fun flick() = invokeSafe("flick")
    fun systemNotificationSuccess() = invokeSafe("systemNotificationSuccess")
    fun systemNotificationError() = invokeSafe("systemNotificationError")
}

fun Pulsar.getSafePresets(): SafePresets {
    // getPresets() itself can throw (it casts Pulsar's context to Activity), so the
    // acquisition has to be guarded too — not just the individual preset calls.
    return SafePresets(try { getPresets() } catch (t: Throwable) { null })
}

class SafeRealtimeComposer(private val delegate: Any?) {
    fun start() {
        val target = delegate ?: return
        try {
            target.javaClass.getMethod("start").invoke(target)
        } catch (t: Throwable) {}
    }

    fun stop() {
        val target = delegate ?: return
        try {
            target.javaClass.getMethod("stop").invoke(target)
        } catch (t: Throwable) {}
    }

    fun set(amplitude: Float, frequency: Float, startIfNeeded: Boolean = true) {
        val target = delegate ?: return
        try {
            val method = target.javaClass.methods.firstOrNull { it.name == "set" }
            method?.invoke(target, amplitude, frequency, startIfNeeded)
        } catch (t: Throwable) {}
    }
}

fun Pulsar.getSafeRealtimeComposer(strategy: RealtimeComposerStrategy): SafeRealtimeComposer {
    return SafeRealtimeComposer(try { getRealtimeComposer(strategy) } catch (t: Throwable) { null })
}
