package com.abhik.paisatrack.ui.screens.profile.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.abhik.paisatrack.R
import com.abhik.paisatrack.ui.theme.ThemeMode

/**
 * Screen-specific utility helpers for the Profile screen.
 */
object ProfileUtils {

    fun getThemeIcon(mode: ThemeMode): Int {
        return when (mode) {
            ThemeMode.Light -> R.drawable.ic_sun_custom
            ThemeMode.Dark -> R.drawable.ic_moon_custom
            ThemeMode.System -> R.drawable.ic_system_theme_custom
        }
    }

    fun getAppVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    fun shareApp(context: Context) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Download Paisa Track app at https://play.google.com/store/apps/details?id=${context.packageName}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, null))
    }

    fun openPlayStore(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${context.packageName}")
                setPackage("com.android.vending")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}
