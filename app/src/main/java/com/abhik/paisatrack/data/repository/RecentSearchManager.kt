package com.abhik.paisatrack.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecentSearchManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _recentSearches = MutableStateFlow<List<String>>(loadRecentSearches())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private fun loadRecentSearches(): List<String> {
        val raw = prefs.getString(KEY_RECENT_SEARCHES, null)
        if (raw.isNullOrBlank()) {
            return DEFAULT_SUGGESTIONS
        }
        val items = raw.split(DELIMITER).map { it.trim() }.filter { it.isNotBlank() }
        return if (items.isEmpty()) DEFAULT_SUGGESTIONS else items
    }

    fun addSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        val current = _recentSearches.value.toMutableList()
        // Case-insensitive removal of existing duplicate
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        // Prepend new search
        current.add(0, trimmed)
        // Keep max 15 recent queries
        val capped = current.take(15)

        _recentSearches.value = capped
        prefs.edit().putString(KEY_RECENT_SEARCHES, capped.joinToString(DELIMITER)).apply()
    }

    fun removeSearchQuery(query: String) {
        val current = _recentSearches.value.toMutableList()
        current.removeAll { it.equals(query.trim(), ignoreCase = true) }
        _recentSearches.value = current
        prefs.edit().putString(KEY_RECENT_SEARCHES, current.joinToString(DELIMITER)).apply()
    }

    fun clearAll() {
        _recentSearches.value = emptyList()
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
    }

    companion object {
        private const val PREFS_NAME = "paisa_track_search_prefs"
        private const val KEY_RECENT_SEARCHES = "key_recent_searches"
        private const val DELIMITER = "\u001F" // Unit separator for safe splitting

        val DEFAULT_SUGGESTIONS = listOf(
            "Rent",
            "Salary",
            "Monthly expenses",
            "October 2026",
            "Electricity bill"
        )
    }
}
