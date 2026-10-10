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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hackathon_project.ui.theme.Hackathon_projectTheme

class InactiveEventsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hackathon_projectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InactiveEventsContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun InactiveEventsContent(modifier: Modifier = Modifier) {
    val sampleInactiveEvents = listOf(
        Event(id = 101, name = "Past Planning Meeting", date = "Feb 10", time = "10:00 AM", location = "Room 202", isActive = false),
        Event(id = 102, name = "Old Workshop", date = "Feb 15", time = "02:00 PM", location = "Lab 5", isActive = false),
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Inactive Events",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                )
            }
            items(sampleInactiveEvents, key = { eventItem -> eventItem.id }) { eventItem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = eventItem.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "${eventItem.date} at ${eventItem.time}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "📍 ${eventItem.location} (Inactive)",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun InactiveEventsPreview() {
    Hackathon_projectTheme {
        InactiveEventsContent()
    }
}
