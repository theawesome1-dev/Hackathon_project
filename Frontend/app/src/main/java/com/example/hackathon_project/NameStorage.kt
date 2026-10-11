package com.example.hackathon_project

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(
    name = "name_storage"
)

private val NAMES_KEY = stringPreferencesKey("names")

suspend fun saveNames(context: Context, names: List<String>) {
    context.dataStore.edit { preferences ->
        preferences[NAMES_KEY] = Json.encodeToString(names)
    }
}

suspend fun loadNames(context: Context): List<String> {
    return try {
        val preferences = context.dataStore.data.first()
        val json = preferences[NAMES_KEY] ?: return emptyList()
        Json.decodeFromString<List<String>>(json)
    } catch (_: Exception) {
        emptyList()
    }
}
