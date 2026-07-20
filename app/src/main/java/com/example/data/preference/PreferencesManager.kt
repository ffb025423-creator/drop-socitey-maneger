package com.example.data.preference

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "drop_society_prefs",
        Context.MODE_PRIVATE
    )

    fun isDarkTheme(): Boolean = prefs.getBoolean("is_dark_theme", true)
    fun setDarkTheme(value: Boolean) {
        prefs.edit().putBoolean("is_dark_theme", value).apply()
    }

    fun getCurrencySymbol(): String = prefs.getString("currency_symbol", "৳") ?: "৳"
    fun setCurrencySymbol(value: String) {
        prefs.edit().putString("currency_symbol", value).apply()
    }

    fun getDateFormat(): String = prefs.getString("date_format", "dd MMM yyyy") ?: "dd MMM yyyy"
    fun setDateFormat(value: String) {
        prefs.edit().putString("date_format", value).apply()
    }

    fun getSecurityPin(): String = prefs.getString("security_pin", "") ?: ""
    fun setSecurityPin(value: String) {
        prefs.edit().putString("security_pin", value).apply()
    }

    fun isFingerprintEnabled(): Boolean = prefs.getBoolean("is_fingerprint_enabled", false)
    fun setFingerprintEnabled(value: Boolean) {
        prefs.edit().putBoolean("is_fingerprint_enabled", value).apply()
    }

    fun isAutoBackupEnabled(): Boolean = prefs.getBoolean("is_auto_backup_enabled", false)
    fun setAutoBackupEnabled(value: Boolean) {
        prefs.edit().putBoolean("is_auto_backup_enabled", value).apply()
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
