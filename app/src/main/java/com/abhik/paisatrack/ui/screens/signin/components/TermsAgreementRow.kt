package com.abhik.paisatrack.ui.screens.signin.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsAgreementRow(
    isAgreed: Boolean,
    onAgreementChanged: (Boolean) -> Unit,
    onPlunkHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val view = LocalView.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Agreement Checkbox
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isAgreed) MaterialTheme.colorScheme.primary else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (isAgreed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable {
                    onPlunkHaptic()
                    onAgreementChanged(!isAgreed)
                },
            contentAlignment = Alignment.Center
        ) {
            if (isAgreed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checked Icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Description with inline clickable links
        val annotatedText = buildAnnotatedString {
            append("By continuing, you agree to our ")
            pushStringAnnotation(
                tag = "terms",
                annotation = "https://www.notion.so/Terms-Conditions-for-Paisa-Track-34005815a54c800680a4d955016f7a3f"
            )
            pushStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            )
            append("Terms & Conditions")
            pop()
            pop()

            append(" and ")

            pushStringAnnotation(
                tag = "privacy",
                annotation = "https://www.notion.so/Privacy-Policy-for-Paisa-Track-34005815a54c803aae7bdc7f040cc59f"
            )
            pushStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            )
            append("Privacy Policy")
            pop()
            pop()

            append(".")
        }

        Box(modifier = Modifier.weight(1f)) {
            @Suppress("DEPRECATION")
            ClickableText(
                text = annotatedText,
                style = LocalTextStyle.current.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "terms", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            onPlunkHaptic()
                            uriHandler.openUri(annotation.item)
                        }
                    annotatedText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            uriHandler.openUri(annotation.item)
                        }
                }
            )
        }
    }
}
