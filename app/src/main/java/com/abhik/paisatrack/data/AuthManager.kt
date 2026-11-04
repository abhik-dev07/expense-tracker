package com.abhik.paisatrack.data

import android.content.Context
import com.mmk.kmpauth.google.GoogleAuthProvider

object AuthManager {
    private const val PREFS_NAME = "paisa_track_prefs"
    private const val KEY_SIGNED_IN = "is_signed_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_LAST_USER_ID = "last_user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_SEEN_ONBOARDING = "has_seen_onboarding"
    private const val KEY_AGREED_TERMS = "paisa_track_terms_agreed"
    private const val KEY_PROFILE_PIC_URL = "profile_pic_url"

    fun hasSeenOnboarding(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SEEN_ONBOARDING, false)
    }

    fun setSeenOnboarding(context: Context, seen: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SEEN_ONBOARDING, seen).apply()
    }

    fun hasAgreedToTerms(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AGREED_TERMS, false)
    }

    fun setAgreedToTerms(context: Context, agreed: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AGREED_TERMS, agreed).apply()
    }

    fun isUserSignedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SIGNED_IN, false)
    }

    fun setUserSignedIn(
        context: Context,
        signedIn: Boolean,
        name: String? = null,
        email: String? = null,
        profilePicUrl: String? = null,
        userId: String? = null,
        authToken: String? = null
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_SIGNED_IN, signedIn)
            if (name != null) {
                putString(KEY_USER_NAME, name)
            }
            if (email != null) {
                putString(KEY_USER_EMAIL, email)
            }
            if (profilePicUrl != null) {
                putString(KEY_PROFILE_PIC_URL, profilePicUrl)
            } else if (!signedIn) {
                remove(KEY_PROFILE_PIC_URL)
            }
            if (userId != null) {
                putString(KEY_USER_ID, userId)
                putString(KEY_LAST_USER_ID, userId)
            } else if (!signedIn) {
                remove(KEY_USER_ID)
            }
            if (authToken != null) {
                putString(KEY_AUTH_TOKEN, authToken)
            } else if (!signedIn) {
                remove(KEY_AUTH_TOKEN)
            }
            apply()
        }
    }

    fun getUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ID, null)
    }

    fun getLastUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_USER_ID, null)
    }

    fun getAuthToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun setAuthToken(context: Context, token: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (token != null) {
                putString(KEY_AUTH_TOKEN, token)
            } else {
                remove(KEY_AUTH_TOKEN)
            }
            apply()
        }
    }

    fun getUserName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_NAME, "User") ?: "User"
    }

    fun getUserEmail(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun getProfilePicUrl(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PROFILE_PIC_URL, null)
    }

    private var googleAuthProvider: GoogleAuthProvider? = null

    fun setGoogleAuthProvider(provider: GoogleAuthProvider?) {
        googleAuthProvider = provider
    }

    suspend fun signOut(context: Context) {
        try {
            googleAuthProvider?.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_SIGNED_IN, false)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_PROFILE_PIC_URL)
            remove(KEY_AUTH_TOKEN)
            apply()
        }
    }

    fun clearAllPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
