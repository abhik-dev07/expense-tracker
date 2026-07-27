package com.abhik.paisatrack.ui.utils

import com.swmansion.pulsar.Pulsar
import com.swmansion.pulsar.types.RealtimeComposerStrategy

class SafePresets(private val delegate: Any) {
    private fun invokeSafe(methodName: String) {
        try {
            val method = delegate.javaClass.getMethod(methodName)
            method.invoke(delegate)
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
    return SafePresets(this.getPresets())
}

class SafeRealtimeComposer(private val delegate: Any) {
    fun start() {
        try {
            delegate.javaClass.getMethod("start").invoke(delegate)
        } catch (t: Throwable) {}
    }

    fun stop() {
        try {
            delegate.javaClass.getMethod("stop").invoke(delegate)
        } catch (t: Throwable) {}
    }

    fun set(amplitude: Float, frequency: Float, startIfNeeded: Boolean = true) {
        try {
            val method = delegate.javaClass.methods.firstOrNull { it.name == "set" }
            method?.invoke(delegate, amplitude, frequency, startIfNeeded)
        } catch (t: Throwable) {}
    }
}

fun Pulsar.getSafeRealtimeComposer(strategy: RealtimeComposerStrategy): SafeRealtimeComposer {
    return SafeRealtimeComposer(this.getRealtimeComposer(strategy))
}
