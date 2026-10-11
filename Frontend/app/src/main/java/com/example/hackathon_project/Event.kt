package com.example.hackathon_project

import kotlinx.serialization.Serializable

// Data class representing an event (supports mutable properties and persistence)
@Serializable
data class Event(
    var id: Long = 0,
    var name: String = "",
    var date: String = "",
    var start: String = "",
    var end: String = "",
    var location: String = "",
    var description: String = "",
    var postUrl: String = "",
    var isActive: Boolean = false,
)
