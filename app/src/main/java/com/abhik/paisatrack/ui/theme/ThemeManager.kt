package com.abhik.paisatrack.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    Light, Dark, System
}

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    private val _themeMode = MutableStateFlow(ThemeMode.System)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _requestedTheme = MutableStateFlow<ThemeMode?>(null)
    val requestedTheme: StateFlow<ThemeMode?> = _requestedTheme.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedMode = prefs.getString(KEY_THEME_MODE, ThemeMode.System.name) ?: ThemeMode.System.name
        val mode = ThemeMode.valueOf(savedMode)
        _themeMode.value = mode
    }

    fun requestTheme(context: Context, mode: ThemeMode) {
        if (_themeMode.value == mode || _requestedTheme.value != null) return
        _requestedTheme.value = mode
    }

    fun clearRequest() {
        _requestedTheme.value = null
    }

    fun setTheme(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }
}
