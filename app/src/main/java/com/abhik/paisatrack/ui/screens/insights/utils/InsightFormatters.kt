package com.abhik.paisatrack.ui.screens.insights.utils

import java.text.DecimalFormat
import java.util.Locale

/**
 * Utility functions for formatting and presentation calculations in the Insights screen.
 */
object InsightFormatters {

    /**
     * Formats an amount into a compact human-readable string.
     * e.g. 1500 -> "₹1.5K", 250 -> "₹250", 0 -> "₹0"
     */
    fun formatCompactAmount(amount: Double): String {
        return when {
            amount >= 10_00_000 -> "₹${String.format(Locale.US, "%.1f", amount / 10_00_000)}M"
            amount >= 1_000 -> "₹${String.format(Locale.US, "%.1f", amount / 1_000)}K"
            amount > 0 -> "₹${amount.toInt()}"
            else -> "₹0"
        }
    }

    /**
     * Formats total expenditure for donut chart center label or summaries.
     */
    fun formatCenterAmount(totalExp: Double, dollarFormat: DecimalFormat): String {
        return if (totalExp >= 100000) {
            String.format(Locale.US, "%.1fK", totalExp / 1000.0)
        } else {
            dollarFormat.format(totalExp)
        }
    }
}
