package com.abhik.paisatrack.ui.screens.collections.utils

import com.abhik.paisatrack.ui.CollectionSummary

/**
 * Utility functions for sorting and filtering collections in the Collections screen.
 */
object CollectionFilters {

    /**
     * Filters and sorts [CollectionSummary] items according to the active tab:
     * - "All": Returns all prebuilt and custom collections.
     * - "Prebuild": Returns only system-provided prebuilt collections.
     * - "Owned": Returns only user-created custom collections.
     *
     * Prebuilt collections are sorted first, followed by custom collections by creation timestamp descending.
     */
    fun filterAndSortSummaries(
        summaries: List<CollectionSummary>,
        activeTab: String
    ): List<CollectionSummary> {
        val sorted = summaries.sortedWith(
            compareBy<CollectionSummary> { it.collection.isPrebuilt }
                .thenByDescending { if (!it.collection.isPrebuilt) it.collection.createdTimestamp else 0L }
                .thenBy { if (it.collection.isPrebuilt) it.collection.id else 0L }
        )
        return when (activeTab) {
            "Prebuild" -> sorted.filter { it.collection.isPrebuilt }
            "Owned" -> sorted.filter { !it.collection.isPrebuilt }
            else -> sorted
        }
    }
}
