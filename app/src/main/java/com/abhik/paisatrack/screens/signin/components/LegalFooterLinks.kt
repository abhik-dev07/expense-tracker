package com.abhik.paisatrack.screens.signin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LegalFooterLinks(
    onPlunkHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "About Us",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                onPlunkHaptic()
                uriHandler.openUri("https://www.notion.so/About-Us-Paisa-Track-34005815a54c8002926bfee1bf0f8efb")
            }
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .size(width = 1.dp, height = 12.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f))
        )

        Text(
            text = "Disclaimer",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                onPlunkHaptic()
                uriHandler.openUri("https://www.notion.so/Disclaimer-for-Paisa-Track-34005815a54c8016827bd3d0374f5dd4")
            }
        )
    }
}
