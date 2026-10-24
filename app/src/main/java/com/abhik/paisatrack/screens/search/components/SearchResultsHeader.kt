package com.abhik.paisatrack.screens.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R
import com.abhik.paisatrack.ui.theme.SleekPrimary
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme

@Composable
fun SearchResultsHeader(
    query: String,
    resultCount: Int,
    dateLabel: String? = null,
    isFilterActive: Boolean = false,
    activeFiltersCount: Int = 0,
    onFilterClick: () -> Unit,
    onClearDate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val titleText = when {
                query.isNotBlank() && dateLabel != null -> "Results for \"$query\""
                query.isNotBlank() -> "Results for \"$query\""
                dateLabel != null -> "$dateLabel records"
                else -> "All records"
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "$resultCount ${if (resultCount == 1) "record" else "records"} found",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val filterTint = if (isFilterActive) {
                if (isDark) Color(0xFFFFFFFF) else Color(0xFF1FB47B)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            // Filter Action Button with badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .then(if (!isFilterActive) Modifier.clip(CircleShape) else Modifier)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 19.dp)
                    ) { onFilterClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isFilterActive) R.drawable.ic_filter_active else R.drawable.ic_filter
                    ),
                    contentDescription = "Filter",
                    tint = filterTint,
                    modifier = Modifier.size(20.dp)
                )

                if (isFilterActive && activeFiltersCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(SleekPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$activeFiltersCount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Active Date Range Badge if active
        if (dateLabel != null && query.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SleekPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_date),
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = dateLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SleekPrimary
                )
                if (onClearDate != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_small),
                        contentDescription = "Clear date filter",
                        tint = SleekPrimary,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onClearDate() }
                    )
                }
            }
        }
    }
}
