package com.example.hackathon_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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

// List of events to be displayed
@Composable
fun MyScreenContent(modifier: Modifier = Modifier) {
    val sampleEvents = listOf(
        Event(id = 1, name = "Hackathon Kickoff", date = "March 30", time = "09:00 AM", location = "Main Hall"),
        Event(id = 2, name = "Coding Sprint", date = "March 30", time = "01:00 PM", location = "Lab 101"),
        Event(id = 3, name = "Project Presentations", date = "March 31", time = "04:00 PM", location = "Auditorium"),
        Event(id = 4, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage"),
        Event(id = 5, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage"),
        Event(id = 6, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage"),
        Event(id = 7, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage"),
        Event(id = 8, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage"),
        Event(id = 9, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage"),
        Event(id = 10, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage"),
        Event(id = 11, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage"),
        Event(id = 12, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage")
    )

    EventList(
        events = sampleEvents,
        modifier = modifier
    )
}

// Formats the column of events
@Composable
fun EventList(
    events: List<Event>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 32.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Event Manager",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            items(events, key = { event -> event.id }) { event ->
                EventCard(event = event)
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
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
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${event.date} at ${event.time}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "📍 ${event.location}",
                style = MaterialTheme.typography.bodySmall
            )
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
