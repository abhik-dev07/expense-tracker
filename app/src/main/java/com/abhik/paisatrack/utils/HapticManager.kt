package com.abhik.paisatrack.utils

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object HapticManager {
    private const val PREFS_NAME = "haptic_prefs"
    private const val KEY_HAPTICS_ENABLED = "haptics_enabled"

    private val _isHapticEnabled = MutableStateFlow(true)
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    val isEnabled: Boolean
        get() = _isHapticEnabled.value

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        _isHapticEnabled.value = saved && isSystemHapticEnabled(context)
    }

    fun setHapticsEnabled(context: Context, enabled: Boolean): Boolean {
        if (enabled && !isSystemHapticEnabled(context)) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, false).apply()
            _isHapticEnabled.value = false
            return false
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        _isHapticEnabled.value = enabled
        return true
    }

    @Suppress("DEPRECATION")
    fun isSystemHapticEnabled(context: Context): Boolean {
        return try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == false) return false

            val feedbackEnabled = Settings.System.getInt(
                context.contentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                0
            )
            if (feedbackEnabled == 0) return false

            val intensity = Settings.System.getInt(
                context.contentResolver,
                "haptic_feedback_intensity",
                1
            )
            if (intensity == 0) return false

            true
        } catch (e: Exception) {
            false
        }
    }
}
