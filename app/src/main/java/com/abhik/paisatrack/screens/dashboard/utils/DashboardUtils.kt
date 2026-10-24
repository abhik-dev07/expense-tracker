package com.abhik.paisatrack.screens.dashboard.utils

import java.text.DecimalFormat

/**
 * Screen-specific utility helpers for the Dashboard screen.
 */
object DashboardUtils {
    val dollarFormat = DecimalFormat("#,##0.00")

    fun formatCurrency(amount: Double): String {
        return dollarFormat.format(amount)
    }

    /**
     * Determines whether back-to-top button should be visible based on scroll offset and scroll direction.
     */
    fun shouldShowBackToTop(isScrollingUp: Boolean, scrollOffset: Int, threshold: Int = 400): Boolean {
        return isScrollingUp && scrollOffset > threshold
    }
}
