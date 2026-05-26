package com.example.pdfviewer.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pdfviewer.model.DocumentProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

private const val STORE_NAME = "recent_documents"
private val Context.dataStore by preferencesDataStore(STORE_NAME)

class RecentStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val key: Preferences.Key<String> = stringPreferencesKey("recents_json")

    val recentsFlow: Flow<List<DocumentProgress>> = context.dataStore.data.map { prefs ->
        val raw = prefs[key]
        if (raw.isNullOrBlank()) {
            emptyList()
        } else {
            runCatching { json.decodeFromString<List<DocumentProgress>>(raw) }
                .getOrDefault(emptyList())
        }
    }

    suspend fun upsert(progress: DocumentProgress, maxItems: Int = 10) {
        context.dataStore.edit { prefs ->
            val current = prefs[key]?.let {
                runCatching { json.decodeFromString<List<DocumentProgress>>(it) }.getOrDefault(emptyList())
            } ?: emptyList()
            val filtered = current.filterNot { it.uri == progress.uri }
            val updated = listOf(progress) + filtered
            val trimmed = if (updated.size > maxItems) updated.take(maxItems) else updated
            prefs[key] = json.encodeToString(trimmed)
        }
    }

    suspend fun remove(uri: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[key]?.let {
                runCatching { json.decodeFromString<List<DocumentProgress>>(it) }.getOrDefault(emptyList())
            } ?: emptyList()
            val updated = current.filterNot { it.uri == uri }
            prefs[key] = json.encodeToString(updated)
        }
    }
}
