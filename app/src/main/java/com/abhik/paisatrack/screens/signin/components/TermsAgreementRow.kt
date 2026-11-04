package com.abhik.paisatrack.screens.signin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R

@Composable
fun TermsAgreementRow(
    isAgreed: Boolean,
    onAgreementChanged: (Boolean) -> Unit,
    onPlunkHaptic: () -> Unit,
    onNavigateToTermsConditions: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                    painter = painterResource(id = R.drawable.ic_tick),
                    contentDescription = "Checked Icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Description with inline clickable links
        val annotatedText = buildAnnotatedString {
            append("By continuing, you agree to our ")
            pushStringAnnotation(
                tag = "terms",
                annotation = "terms"
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
                annotation = "privacy"
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
                        .firstOrNull()?.let {
                            onPlunkHaptic()
                            onNavigateToTermsConditions()
                        }
                    annotatedText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onPlunkHaptic()
                            onNavigateToPrivacyPolicy()
                        }
                }
            )
        }
    }
}
