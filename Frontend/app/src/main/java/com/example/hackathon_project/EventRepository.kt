package com.example.hackathon_project

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object EventRepository {
    private var isLoaded = false

    // Shared active events list
    val activeEvents = mutableStateListOf<Event>()

    // Shared inactive events list (fetched from backend or added dynamically)
    val inactiveEvents = mutableStateListOf<Event>()

    // Set of dismissed/deleted event unique keys (Name_Date or postUrl)
    val dismissedEvents = mutableSetOf<String>()

    suspend fun ensureLoaded(context: Context) {
        if (!isLoaded) {
            loadEventsFromStorage(context)
            isLoaded = true
        }
    }

    // Move an event from inactive to active
    fun promoteToActive(event: Event) {
        inactiveEvents.remove(event)
        val promotedEvent = event.copy(isActive = true)
        val key = "${event.name}_${event.date}"
        dismissedEvents.add(key)
        if (event.postUrl.isNotBlank()) dismissedEvents.add(event.postUrl)
        if (!activeEvents.any { "${it.name}_${it.date}" == key || (it.postUrl.isNotBlank() && it.postUrl == event.postUrl) }) {
            activeEvents.add(promotedEvent)
        }
    }

    // Delete an event from inactive and remember it so it won't be re-added
    fun removeInactive(event: Event) {
        inactiveEvents.remove(event)
        dismissedEvents.add("${event.name}_${event.date}")
        if (event.postUrl.isNotBlank()) dismissedEvents.add(event.postUrl)
    }

    // Delete an event from active
    fun removeActive(event: Event) {
        activeEvents.remove(event)
        dismissedEvents.add("${event.name}_${event.date}")
        if (event.postUrl.isNotBlank()) dismissedEvents.add(event.postUrl)
    }

    fun saveAsync(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            saveEventsToStorage(context)
        }
    }
}
