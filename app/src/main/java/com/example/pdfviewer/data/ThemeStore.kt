package com.example.pdfviewer.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pdfviewer.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val STORE_NAME = "theme_settings"
private val Context.dataStore by preferencesDataStore(STORE_NAME)

class ThemeStore(private val context: Context) {
    private val key: Preferences.Key<String> = stringPreferencesKey("theme_mode")

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.fromStorage(prefs[key])
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[key] = mode.storageValue
        }
    }
}
