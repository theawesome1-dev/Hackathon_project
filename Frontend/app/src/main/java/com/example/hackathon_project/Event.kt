package com.example.hackathon_project

import java.text.SimpleDateFormat
import java.util.Locale

// Data class representing an event
data class Event(
    var id: Long = 0,
    var name: String = "",
    var date: String = "",       // e.g., "10/13/2026" (American MM/DD/YYYY)
    var startTime: String = "",  // e.g., "12:00 PM" (American 12-hr AM/PM)
    var endTime: String = "",    // e.g., "3:00 PM" (American 12-hr AM/PM)
    var location: String = "",
    var description: String = "",
    var postUrl: String = "",
    var isActive: Boolean = false,
) {
    // Combined time for display convenience
    val time: String
        get() = when {
            startTime.isNotBlank() && endTime.isNotBlank() -> "$startTime – $endTime"
            startTime.isNotBlank() -> startTime
            else -> "TBD"
        }
}

// Converts ISO 8601 date string (e.g., "2026-10-13T12:00:00-04:00") to American Date format ("10/13/2026")
fun parseAmericanDate(isoString: String): String {
    if (isoString.isBlank()) return "TBD"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val datePart = isoString.split("T").getOrNull(0) ?: isoString
        val parsed = inputFormat.parse(datePart)
        if (parsed != null) outputFormat.format(parsed) else datePart
    } catch (e: Exception) {
        if (isoString.length >= 10) isoString.substring(0, 10) else isoString
    }
}

// Converts ISO 8601 timestamp string (e.g., "2026-10-13T12:00:00-04:00") to American 12-hr Time format ("12:00 PM")
fun parseAmericanTime(isoString: String): String {
    if (isoString.isBlank()) return ""
    return try {
        val timePart = isoString.split("T").getOrNull(1)?.split("-")?.getOrNull(0)?.split("+")?.getOrNull(0) ?: ""
        if (timePart.isBlank()) return ""
        val inputFormat = SimpleDateFormat("HH:mm", Locale.US)
        val outputFormat = SimpleDateFormat("h:mm a", Locale.US)
        val parsed = inputFormat.parse(timePart)
        if (parsed != null) outputFormat.format(parsed) else timePart
    } catch (e: Exception) {
        if (isoString.length >= 16) isoString.substring(11, 16) else isoString
    }
}
