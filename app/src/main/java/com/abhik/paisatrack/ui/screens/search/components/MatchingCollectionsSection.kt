package com.abhik.paisatrack.ui.screens.search.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.components.getIconByName
import com.abhik.paisatrack.ui.utils.safeParseColor
import com.abhik.paisatrack.ui.theme.SleekPrimary
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MatchingCollectionsSection(
    collections: List<CollectionEntity>,
    rawTransactions: List<TransactionEntity>,
    dollarFormat: DecimalFormat,
    onCollectionClick: (CollectionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (collections.isEmpty()) return

    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) Color(0xFF1E1E24) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2C2C36) else Color(0xFFE2E8E5)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Matching collections",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SleekPrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${collections.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(collections, key = { it.id }) { col ->
                val colColor = safeParseColor(col.hexColor)
                val matchingTx = rawTransactions.filter { it.collectionId == col.id }
                var totalSpent = 0.0
                matchingTx.forEach {
                    if (it.type.uppercase() != "INCOME") totalSpent += it.amount
                }

                Surface(
                    modifier = Modifier
                        .width(200.dp)
                        .shadow(
                            elevation = 1.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.05f),
                            spotColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.05f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onCollectionClick(col) },
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(MaterialShapes.Cookie4Sided.toShape())
                                    .background(colColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(col.iconName),
                                    contentDescription = null,
                                    tint = colColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View Collection",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = col.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${matchingTx.size} records · ${dollarFormat.format(totalSpent)} spent",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
