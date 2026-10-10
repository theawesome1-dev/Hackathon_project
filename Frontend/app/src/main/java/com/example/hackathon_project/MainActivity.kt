package com.example.hackathon_project

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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

// List of active events to be displayed
@Composable
fun MyScreenContent(modifier: Modifier = Modifier) {
    val sampleEvents = listOf(
        Event(id = 1, name = "Hackathon Kickoff", date = "March 30", time = "09:00 AM", location = "Main Hall", isActive = true),
        Event(id = 2, name = "Coding Sprint", date = "March 30", time = "01:00 PM", location = "Lab 101", isActive = true),
        Event(id = 3, name = "Project Presentations", date = "March 31", time = "04:00 PM", location = "Auditorium", isActive = true),
        Event(id = 4, name = "Awards Ceremony", date = "March 31", time = "06:00 PM", location = "Main Stage", isActive = true),
        Event(id = 5, name = "Tech Workshop", date = "April 01", time = "10:00 AM", location = "Room 102", isActive = true),
        Event(id = 6, name = "Networking Lunch", date = "April 01", time = "12:30 PM", location = "Cafeteria", isActive = true),
        Event(id = 7, name = "Keynote Speech", date = "April 02", time = "09:30 AM", location = "Main Hall", isActive = true),
        Event(id = 8, name = "Hackathon Judging", date = "April 02", time = "02:00 PM", location = "Lab 101", isActive = true),
        Event(id = 9, name = "Closing Ceremony", date = "April 02", time = "05:00 PM", location = "Auditorium", isActive = true),
    )

    EventList(
        events = sampleEvents,
        modifier = modifier
    )
}

// Formats the column of events
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventList(
    events: List<Event>,
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
                        text = "Active Events",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (activity != null) {
                        OutlinedButton(
                            onClick = { activity.finish() },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

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
                    EventButton(event = event)
                }
            }
        }
    }
}
@Composable
fun EventButton(
    event: Event,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = {

        },
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
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun GreetingPreview() {
    Hackathon_projectTheme {
        MyScreenContent()
    }
}

