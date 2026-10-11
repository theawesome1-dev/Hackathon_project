package com.example.hackathon_project

import androidx.compose.runtime.mutableStateListOf

object EventRepository {
    // Shared active events list
    val activeEvents = mutableStateListOf(
        Event(id = 5, name = "Tech Workshop", date = "April 01", time = "10:00 AM", location = "Room 102", isActive = true),
        Event(id = 1, name = "Hackathon Kickoff", date = "March 30", time = "09:00 AM", location = "Main Hall", isActive = true),
        Event(id = 3, name = "Project Presentations", date = "March 31", time = "04:00 PM", location = "Auditorium", isActive = true),
        Event(id = 2, name = "Coding Sprint", date = "March 30", time = "01:00 PM", location = "Lab 101", isActive = true),
        Event(id = 6, name = "Networking Lunch", date = "April 01", time = "12:30 PM", location = "Cafeteria", isActive = true),
        Event(id = 4, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage", isActive = true),
        Event(id = 8, name = "Hackathon Judging", date = "April 02", time = "02:00 PM", location = "Lab 101", isActive = true),
        Event(id = 7, name = "Keynote Speech", date = "April 02", time = "09:30 AM", location = "Main Hall", isActive = true),
        Event(id = 9, name = "Closing Ceremony", date = "April 02", time = "05:00 PM", location = "Auditorium", isActive = true),
    )

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
