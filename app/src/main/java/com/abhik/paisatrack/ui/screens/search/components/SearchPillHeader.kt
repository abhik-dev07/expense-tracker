package com.abhik.paisatrack.ui.screens.search.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R
import com.abhik.paisatrack.ui.theme.HeaderGradient0Dark
import com.abhik.paisatrack.ui.theme.HeaderGradient0Light
import com.abhik.paisatrack.ui.theme.HeaderGradient100Dark
import com.abhik.paisatrack.ui.theme.HeaderGradient100Light
import com.abhik.paisatrack.ui.theme.HeaderGradient18Dark
import com.abhik.paisatrack.ui.theme.HeaderGradient18Light
import com.abhik.paisatrack.ui.theme.HeaderGradient38Dark
import com.abhik.paisatrack.ui.theme.HeaderGradient38Light
import com.abhik.paisatrack.ui.theme.HeaderGradient58Dark
import com.abhik.paisatrack.ui.theme.HeaderGradient58Light
import com.abhik.paisatrack.ui.theme.HeaderGradient75Dark
import com.abhik.paisatrack.ui.theme.HeaderGradient75Light
import com.abhik.paisatrack.ui.theme.HeaderGradient90Dark
import com.abhik.paisatrack.ui.theme.HeaderGradient90Light
import com.abhik.paisatrack.ui.theme.SearchBackgroundDark
import com.abhik.paisatrack.ui.theme.SearchBackgroundLight
import com.abhik.paisatrack.ui.theme.SearchBorderDark
import com.abhik.paisatrack.ui.theme.SearchBorderLight
import com.abhik.paisatrack.ui.theme.SearchDividerDark
import com.abhik.paisatrack.ui.theme.SearchDividerLight
import com.abhik.paisatrack.ui.theme.SearchIconDark
import com.abhik.paisatrack.ui.theme.SearchIconLight
import com.abhik.paisatrack.ui.theme.SearchPlaceholderDark
import com.abhik.paisatrack.ui.theme.SearchPlaceholderLight
import com.abhik.paisatrack.ui.theme.SearchTextDark
import com.abhik.paisatrack.ui.theme.SearchTextLight
import com.abhik.paisatrack.ui.theme.SleekPrimary
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme

@Composable
fun SearchPillHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onImeSearch: () -> Unit,
    onBack: () -> Unit,
    onCalendarClick: () -> Unit,
    onFilterClick: (() -> Unit)? = null,
    isDateFilterActive: Boolean = false,
    isFilterActive: Boolean = false,
    isLoading: Boolean = false,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()

    // Reuse actual Profile / Dashboard gradient styling and color stops
    val headerGradient = Brush.verticalGradient(
        colorStops = if (isDark) arrayOf(
            0.0f to HeaderGradient0Dark,
            0.18f to HeaderGradient18Dark,
            0.38f to HeaderGradient38Dark,
            0.58f to HeaderGradient58Dark,
            0.75f to HeaderGradient75Dark,
            0.90f to HeaderGradient90Dark,
            1.0f to HeaderGradient100Dark
        ) else arrayOf(
            0.0f to HeaderGradient0Light,
            0.18f to HeaderGradient18Light,
            0.38f to HeaderGradient38Light,
            0.58f to HeaderGradient58Light,
            0.75f to HeaderGradient75Light,
            0.90f to HeaderGradient90Light,
            1.0f to HeaderGradient100Light
        )
    )

    val searchBg = if (isDark) SearchBackgroundDark else SearchBackgroundLight
    val searchBorder = if (isDark) SearchBorderDark else SearchBorderLight
    val searchPlaceholder = if (isDark) SearchPlaceholderDark else SearchPlaceholderLight
    val searchText = if (isDark) SearchTextDark else SearchTextLight
    val searchIconTint = if (isDark) SearchIconDark else SearchIconLight
    val searchDivider = if (isDark) SearchDividerDark else SearchDividerLight

    val activeAccentColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1FB47B)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(headerGradient)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.08f),
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.08f)
                ),
            shape = RoundedCornerShape(26.dp),
            color = searchBg,
            border = BorderStroke(1.dp, searchBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 20.dp)
                        ) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_back),
                        contentDescription = "Back",
                        tint = searchIconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Search Input Field - clicking anywhere in this area requests focus and brings up keyboard
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            focusRequester?.requestFocus()
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search records, collections, names...",
                            style = TextStyle(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = searchPlaceholder
                            ),
                            maxLines = 1
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
                        singleLine = true,
                        maxLines = 1,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = searchText
                        ),
                        cursorBrush = SolidColor(SleekPrimary),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { onImeSearch() }
                        )
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Trailing Actions: Clear 'X', Loading Spinner, Calendar Icon (replacing former filter icon)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }

                    if (query.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false, radius = 16.dp)
                                ) { onClearQuery() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close_small),
                                contentDescription = "Clear Search",
                                tint = if (isDark) Color(0xFFA0AAB5) else Color(0xFF8A8F98),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Calendar Action (now placed at the end of the pill in place of the filter icon)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .then(if (!isDateFilterActive) Modifier.clip(CircleShape) else Modifier)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 17.dp)
                            ) { onCalendarClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_date),
                            contentDescription = "Select Date or Date Range",
                            tint = if (isDateFilterActive) activeAccentColor else searchIconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
