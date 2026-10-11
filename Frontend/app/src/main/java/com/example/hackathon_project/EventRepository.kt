package com.example.hackathon_project

import androidx.compose.runtime.mutableStateListOf

object EventRepository {
    // Shared active events list
    val activeEvents = mutableStateListOf<Event>()

    // Shared inactive events list (fetched from backend or added dynamically)
    val inactiveEvents = mutableStateListOf<Event>()

    // Set of dismissed/deleted event unique keys (Name_Date) so they won't be re-fetched
    val dismissedEvents = mutableSetOf<String>()

    // Move an event from inactive to active
    fun promoteToActive(event: Event) {
        inactiveEvents.remove(event)
        val promotedEvent = event.copy(isActive = true)
        val key = "${event.name}_${event.date}"
        dismissedEvents.add(key)
        if (!activeEvents.any { "${it.name}_${it.date}" == key }) {
            activeEvents.add(promotedEvent)
        }
    }

    // Delete an event from inactive and remember it so it won't be re-added
    fun removeInactive(event: Event) {
        inactiveEvents.remove(event)
        dismissedEvents.add("${event.name}_${event.date}")
    }
}
