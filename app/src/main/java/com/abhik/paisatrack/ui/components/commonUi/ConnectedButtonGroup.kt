package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Connected Button Group.
 * Replaces deprecated SegmentedButton according to Material 3 Expressive specs & guidelines:
 * - https://m3.material.io/components/button-groups/specs
 * - https://m3.material.io/components/button-groups/guidelines
 * - https://m3.material.io/components/button-groups/accessibility
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ConnectedButtonGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ToggleButtonColors = ToggleButtonDefaults.toggleButtonColors(),
    isSelected: (T) -> Boolean = { it == selectedOption },
    itemContent: @Composable RowScope.(item: T, isSelected: Boolean) -> Unit
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val selected = isSelected(option)
            val shapes = when {
                options.size == 1 -> ToggleButtonDefaults.shapes()
                index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                index == options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }

            ToggleButton(
                checked = selected,
                onCheckedChange = { if (!selected) onOptionSelected(option) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                enabled = enabled,
                shapes = shapes,
                colors = colors,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                itemContent(option, selected)
            }
        }
    }
}

/**
 * Index-based Material 3 Expressive Connected Button Group.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConnectedButtonGroup(
    count: Int,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ToggleButtonColors = ToggleButtonDefaults.toggleButtonColors(),
    itemContent: @Composable RowScope.(index: Int, isSelected: Boolean) -> Unit
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until count) {
            val selected = index == selectedIndex
            val shapes = when {
                count == 1 -> ToggleButtonDefaults.shapes()
                index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                index == count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }

            ToggleButton(
                checked = selected,
                onCheckedChange = { if (!selected) onSelectIndex(index) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                enabled = enabled,
                shapes = shapes,
                colors = colors,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                itemContent(index, selected)
            }
        }
    }
}
