package com.abhik.paisatrack.screens.collections.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R
import com.abhik.paisatrack.utils.CollectionSummary
import com.abhik.paisatrack.utils.getIconByName
import com.abhik.paisatrack.ui.theme.SearchBorderDark
import com.abhik.paisatrack.ui.theme.SearchBorderLight
import com.abhik.paisatrack.utils.findActivity
import com.abhik.paisatrack.utils.getSafePresets
import com.abhik.paisatrack.utils.safeParseColor
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionGridCard(
    summary: CollectionSummary,
    dollarFormat: DecimalFormat,
    onEditClick: () -> Unit,
    onClick: (Rect?) -> Unit
) {
    val colColor = remember(summary.collection.hexColor) {
        safeParseColor(summary.collection.hexColor)
    }
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }

    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates = it }
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                presets.ping()
                val rect = coordinates?.let { coords ->
                    if (coords.isAttached) coords.boundsInRoot() else null
                }
                onClick(rect)
            },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (com.abhik.paisatrack.ui.theme.isAppInDarkTheme()) SearchBorderDark else SearchBorderLight),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon representation
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(MaterialShapes.Cookie4Sided.toShape())
                        .background(colColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconByName(summary.collection.iconName),
                        contentDescription = null,
                        tint = colColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Edit button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit Collection",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info text
            Text(
                text = summary.collection.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${summary.transactionCount} records",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            // Net balance of the collection card
            val balance = summary.totalIncome - summary.totalExpense

            Column {
                Text(
                    text = "Remaining",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dollarFormat.format(balance),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (balance >= 0.0) Color(0xFF10B981) else Color(0xFFFFB8A9)
                )
            }
        }
    }
}
