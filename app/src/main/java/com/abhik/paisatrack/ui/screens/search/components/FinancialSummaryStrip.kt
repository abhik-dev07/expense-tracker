package com.abhik.paisatrack.ui.screens.search.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.ui.SearchSummary
import com.abhik.paisatrack.ui.screens.search.FinancialSearchUtils
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme
import java.text.DecimalFormat

@Composable
fun FinancialSummaryStrip(
    summary: SearchSummary,
    dollarFormat: DecimalFormat,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) Color(0xFF1E1E24) else Color(0xFFF7FAF8)
    val cardBorder = if (isDark) Color(0xFF2C2C36) else Color(0xFFE2E8E5)

    val dateSpan = remember(summary.firstTimestamp, summary.lastTimestamp) {
        val startStr = FinancialSearchUtils.formatShortDate(summary.firstTimestamp)
        val endStr = FinancialSearchUtils.formatShortDate(summary.lastTimestamp)
        if (startStr == endStr) startStr else "$startStr – $endStr"
    }

    val netBalance = summary.totalReceived - summary.totalSent

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Row: Match count & Date span
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${summary.matchCount} ${if (summary.matchCount == 1) "record" else "records"} found",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = dateSpan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Sent (Expenses), Received (Income), Net Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sent / Expenses
                Column {
                    Text(
                        text = "Total spent",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-${dollarFormat.format(summary.totalSent)}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }

                // Received / Income
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total income",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+${dollarFormat.format(summary.totalReceived)}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                // Net Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net cash flow",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${if (netBalance >= 0) "+" else ""}${dollarFormat.format(netBalance)}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}
