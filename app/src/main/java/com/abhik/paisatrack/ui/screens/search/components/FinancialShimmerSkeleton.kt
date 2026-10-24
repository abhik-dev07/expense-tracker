package com.abhik.paisatrack.ui.screens.search.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.abhik.paisatrack.ui.components.commonUi.shimmerEffect
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FinancialShimmerSkeleton(
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) Color(0xFF1E1E24) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2C2C36) else Color(0xFFE2E8E5)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Results Heading Shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect()
            )

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect()
            )
        }

        // 2. Compact Financial Summary Card Shimmer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(75.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .width(75.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .width(75.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .shimmerEffect()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Record Rows Shimmer
        repeat(6) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category icon placeholder
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialShapes.Cookie4Sided.toShape())
                            .shimmerEffect()
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    // Title & Collection/Date placeholder
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }

                    // Amount & Time placeholder
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(65.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(45.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }
                }
            }
        }
    }
}
