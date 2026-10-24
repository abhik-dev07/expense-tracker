package com.abhik.paisatrack.ui.screens.signin.utils

import android.util.Base64
import java.util.regex.Pattern

object SignInAuthUtils {

    fun getEmailFromIdToken(idToken: String?): String? {
        if (idToken.isNullOrEmpty()) return null
        val parts = idToken.split(".")
        if (parts.size < 2) return null
        return try {
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
            val payloadString = String(payloadBytes, Charsets.UTF_8)
            val pattern = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"")
            val matcher = pattern.matcher(payloadString)
            if (matcher.find()) {
                matcher.group(1)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getGoogleIdFromIdToken(idToken: String?): String? {
        if (idToken.isNullOrEmpty()) return null
        val parts = idToken.split(".")
        if (parts.size < 2) return null
        return try {
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
            val payloadString = String(payloadBytes, Charsets.UTF_8)
            val pattern = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"")
            val matcher = pattern.matcher(payloadString)
            if (matcher.find()) {
                matcher.group(1)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
