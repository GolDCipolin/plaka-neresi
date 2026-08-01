package com.plakaneresi.app.settings

import android.content.Context
import com.plakaneresi.app.ui.theme.ThemeMode

/**
 * Persists the handful of user choices the app has.
 *
 * SharedPreferences rather than DataStore on purpose: there is exactly one value, it is
 * read once at startup, and DataStore would pull in coroutines plumbing for no benefit.
 */
class SettingsStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("plaka_neresi_settings", Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() {
            val stored = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
            // A value written by a future version we do not understand must not crash us.
            return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
        }
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value.name).apply()
        }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
