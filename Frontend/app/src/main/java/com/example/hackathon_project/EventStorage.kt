package com.example.hackathon_project

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

private val Context.eventDataStore by preferencesDataStore(
    name = "event_storage"
)

private val ACTIVE_EVENTS_KEY = stringPreferencesKey("active_events")
private val INACTIVE_EVENTS_KEY = stringPreferencesKey("inactive_events")
private val DISMISSED_EVENTS_KEY = stringPreferencesKey("dismissed_events")

private val jsonConfig = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

suspend fun saveEventsToStorage(context: Context) {
    try {
        context.eventDataStore.edit { preferences ->
            preferences[ACTIVE_EVENTS_KEY] = jsonConfig.encodeToString(EventRepository.activeEvents.toList())
            preferences[INACTIVE_EVENTS_KEY] = jsonConfig.encodeToString(EventRepository.inactiveEvents.toList())
            preferences[DISMISSED_EVENTS_KEY] = jsonConfig.encodeToString(EventRepository.dismissedEvents.toList())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun loadEventsFromStorage(context: Context) {
    try {
        val preferences = context.eventDataStore.data.first()

        val activeJson = preferences[ACTIVE_EVENTS_KEY]
        if (!activeJson.isNullOrBlank()) {
            val activeList: List<Event> = jsonConfig.decodeFromString(activeJson)
            EventRepository.activeEvents.clear()
            EventRepository.activeEvents.addAll(activeList)
        }

        val inactiveJson = preferences[INACTIVE_EVENTS_KEY]
        if (!inactiveJson.isNullOrBlank()) {
            val inactiveList: List<Event> = jsonConfig.decodeFromString(inactiveJson)
            EventRepository.inactiveEvents.clear()
            EventRepository.inactiveEvents.addAll(inactiveList)
        }

        val dismissedJson = preferences[DISMISSED_EVENTS_KEY]
        if (!dismissedJson.isNullOrBlank()) {
            val dismissedList: List<String> = jsonConfig.decodeFromString(dismissedJson)
            EventRepository.dismissedEvents.clear()
            EventRepository.dismissedEvents.addAll(dismissedList)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
