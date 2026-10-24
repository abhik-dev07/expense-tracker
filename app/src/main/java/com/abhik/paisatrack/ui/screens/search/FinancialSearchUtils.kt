package com.abhik.paisatrack.ui.screens.search

import com.abhik.paisatrack.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FinancialSearchUtils {

    fun buildSearchTokens(raw: String): List<String> =
        raw.trim().lowercase().split(" ").filter { it.isNotBlank() }

    fun matchesFinancialSearch(
        tx: TransactionEntity,
        tokens: List<String>,
        collectionName: String = ""
    ): Boolean {
        if (tokens.isEmpty()) return true
        val dateFormat = SimpleDateFormat("d MMMM yyyy MMM dd/MM/yyyy dd-MM-yyyy yyyy", Locale.getDefault())
        val dateString = try {
            dateFormat.format(Date(tx.timestamp))
        } catch (e: Exception) {
            ""
        }
        val haystack = "${tx.description} $collectionName $dateString".lowercase()
        return tokens.all { haystack.contains(it) }
    }

    /**
     * Attempts to parse natural date strings like:
     * - "01/10/2026 - 14/10/2026" or "01-10-2026 - 14-10-2026"
     * - "01/10/2026" or "14-10-2026"
     * - "October 2026" or "Oct 2026"
     * - "2026"
     *
     * Returns Pair(startTimestampInclusive, endTimestampInclusive) or null if not a date query.
     */
    fun parseNaturalDateQuery(raw: String): Pair<Long, Long>? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        // 1. Range format: "dd/MM/yyyy - dd/MM/yyyy" or "dd-MM-yyyy - dd-MM-yyyy"
        if (trimmed.contains(" - ") || trimmed.contains(" to ", ignoreCase = true) || trimmed.contains(" – ")) {
            val delimiter = when {
                trimmed.contains(" - ") -> " - "
                trimmed.contains(" – ") -> " – "
                else -> " to "
            }
            val parts = trimmed.split(delimiter).map { it.trim() }
            if (parts.size == 2) {
                val start = parseSingleDate(parts[0], isEndOfDay = false)
                val end = parseSingleDate(parts[1], isEndOfDay = true)
                if (start != null && end != null) {
                    return Pair(minOf(start, end), maxOf(start, end))
                }
            }
        }

        // 2. Exact single date format: "dd/MM/yyyy" or "dd-MM-yyyy"
        val singleDate = parseSingleDate(trimmed, isEndOfDay = false)
        if (singleDate != null) {
            val endOfDay = parseSingleDate(trimmed, isEndOfDay = true) ?: singleDate
            return Pair(singleDate, endOfDay)
        }

        // 3. Month & Year format: "October 2026" or "Oct 2026"
        val monthYearRange = parseMonthYear(trimmed)
        if (monthYearRange != null) {
            return monthYearRange
        }

        // 4. Exact 4-digit Year format: "2026" (constrained between 1970 and 2100)
        if (trimmed.matches(Regex("^\\d{4}$"))) {
            val year = trimmed.toIntOrNull()
            if (year != null && year in 1970..2100) {
                val cal = Calendar.getInstance()
                cal.set(year, Calendar.JANUARY, 1, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                return Pair(start, end)
            }
        }

        return null
    }

    private fun parseSingleDate(str: String, isEndOfDay: Boolean): Long? {
        val formats = listOf("dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "d MMM yyyy", "d MMMM yyyy")
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }
                val parsed = sdf.parse(str) ?: continue
                val cal = Calendar.getInstance().apply { time = parsed }
                if (isEndOfDay) {
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                } else {
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                }
                return cal.timeInMillis
            } catch (e: Exception) {
                // Try next pattern
            }
        }
        return null
    }

    private fun parseMonthYear(str: String): Pair<Long, Long>? {
        val formats = listOf("MMMM yyyy", "MMM yyyy")
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }
                val parsed = sdf.parse(str) ?: continue
                val cal = Calendar.getInstance().apply { time = parsed }
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH)

                // Start of Month
                cal.set(year, month, 1, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                // End of Month (handles leap year, 28/29/30/31 days)
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(year, month, maxDay, 23, 59, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis

                return Pair(start, end)
            } catch (e: Exception) {
                // Try next
            }
        }
        return null
    }

    fun isPureDateQuery(raw: String): Boolean {
        return parseNaturalDateQuery(raw) != null
    }

    fun formatShortDate(ts: Long): String {
        return SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
    }

    fun formatFullDate(ts: Long): String {
        return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(ts))
    }

    fun formatDateLabel(start: Long?, end: Long?): String? {
        if (start == null) return null
        val full = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val short = SimpleDateFormat("MMM d", Locale.getDefault())
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val endCal = Calendar.getInstance().apply { timeInMillis = end ?: start }

        val sameDay = startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                startCal.get(Calendar.DAY_OF_YEAR) == endCal.get(Calendar.DAY_OF_YEAR)
        if (sameDay) {
            return full.format(Date(start))
        }

        val sameMonth = startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)
        val isFirstDay = startCal.get(Calendar.DAY_OF_MONTH) == 1
        val isLastDay = endCal.get(Calendar.DAY_OF_MONTH) == endCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        if (sameMonth && isFirstDay && isLastDay) {
            return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(start))
        }

        return "${short.format(Date(start))} – ${full.format(Date(end ?: start))}"
    }
}
