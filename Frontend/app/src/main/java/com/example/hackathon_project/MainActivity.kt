package com.example.hackathon_project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hackathon_project.ui.theme.Hackathon_projectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hackathon_projectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyScreenContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Helper function to convert event date/time to a sortable string (Soonest -> Latest)
private fun parseToChronologicalOrder(event: Event): String {
    val monthMap = mapOf(
        "jan" to "01", "feb" to "02", "mar" to "03", "apr" to "04",
        "may" to "05", "jun" to "06", "jul" to "07", "aug" to "08",
        "sep" to "09", "oct" to "10", "nov" to "11", "dec" to "12"
    )
    val dateParts = event.date.trim().lowercase().split(" ")
    val monthStr = dateParts.getOrNull(0)?.take(3) ?: "jan"
    val monthNum = monthMap[monthStr] ?: "01"
    val dayNum = dateParts.getOrNull(1)?.padStart(2, '0') ?: "01"

    val timeParts = event.time.trim().uppercase().split(" ")
    val clock = timeParts.getOrNull(0) ?: "00:00"
    val ampm = timeParts.getOrNull(1) ?: "AM"
    val clockParts = clock.split(":")
    val rawHour = clockParts.getOrNull(0)?.toIntOrNull() ?: 0
    val min = clockParts.getOrNull(1) ?: "00"

    var hour = rawHour
    if (ampm == "PM" && hour < 12) hour += 12
    if (ampm == "AM" && hour == 12) hour = 0
    val hourStr = hour.toString().padStart(2, '0')

    return "$monthNum-$dayNum $hourStr:$min"
}

// List of active events to be displayed
@Composable
fun MyScreenContent(modifier: Modifier = Modifier) {
    val sampleEvents = listOf(
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

    // Observable state list sorted chronologically from soonest to latest
    val events = remember {
        mutableStateListOf<Event>().apply {
            addAll(sampleEvents.sortedBy { parseToChronologicalOrder(it) })
        }
    }

    EventList(
        events = events,
        onDeleteEvent = { eventToDelete ->
            events.remove(eventToDelete)
        },
        modifier = modifier
    )
}

// Formats the column of events
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventList(
    events: List<Event>,
    onDeleteEvent: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "    Active Events (${events.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(context, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            context.startActivity(intent)
                            activity?.finish()
                        },
                        modifier = Modifier.padding(start = 11.dp)
                    ) {
                        Text("Back to Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            if (events.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text(
                        text = "No active events remaining.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onDelete = { onDeleteEvent(event) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${event.date} at ${event.time}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "📍 ${event.location}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TextButton(
                onClick = onDelete,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun GreetingPreview() {
    Hackathon_projectTheme {
        MyScreenContent()
    }
}
