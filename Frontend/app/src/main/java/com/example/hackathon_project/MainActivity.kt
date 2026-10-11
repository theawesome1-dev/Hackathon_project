package com.example.hackathon_project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hackathon_project.ui.theme.Hackathon_projectTheme
import com.example.hackathon_project.ui.theme.InstagramBlue
import com.example.hackathon_project.ui.theme.InstagramDivider
import com.example.hackathon_project.ui.theme.InstagramLightBackground
import com.example.hackathon_project.ui.theme.InstagramRed
import com.example.hackathon_project.ui.theme.InstagramSurface
import com.example.hackathon_project.ui.theme.InstagramTextMuted
import com.example.hackathon_project.ui.theme.InstagramTextPrimary
import com.example.hackathon_project.ui.theme.InstagramTextSecondary
import com.example.hackathon_project.ui.theme.InstagramWhite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hackathon_projectTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = InstagramLightBackground
                ) { innerPadding ->
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

    val timeParts = event.start.trim().uppercase().split(" ")
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
    EventList(
        events = EventRepository.activeEvents,
        onDeleteEvent = { eventToDelete ->
            EventRepository.activeEvents.remove(eventToDelete)
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventList(
    events: List<Event>,
    onDeleteEvent: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(InstagramLightBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Instagram Header Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Active Events (${events.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = InstagramTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            context.startActivity(intent)
                            (context as? Activity)?.finish()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = InstagramTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = InstagramLightBackground,
                    titleContentColor = InstagramTextPrimary
                )
            )

            HorizontalDivider(color = InstagramDivider, thickness = 1.dp)

            if (events.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Active Events",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = InstagramTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Promote events from the Inactive Events tab to see them listed here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InstagramTextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val intent = Intent(context, InactiveEventsActivity::class.java)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = InstagramBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Go to Inactive Events", color = InstagramWhite)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        EventItem(
                            event = event,
                            onDelete = { onDeleteEvent(event) },
                            onClick = { selectedEvent = event }
                        )
                    }
                }
            }
        }

        if (selectedEvent != null) {
            EventDetailDialog(
                event = selectedEvent!!,
                onDismiss = { selectedEvent = null },
                onDelete = {
                    EventRepository.activeEvents.remove(selectedEvent!!)
                    selectedEvent = null
                }
            )
        }
    }
}

@Composable
fun EventItem(
    event: Event,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Clean single-surface row container (Light Mode surface)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(InstagramSurface)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InstagramTextPrimary,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Event",
                    tint = InstagramRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Date",
                tint = InstagramTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${event.date} from ${event.start} to ${event.end}",
                style = MaterialTheme.typography.bodyMedium,
                color = InstagramTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = InstagramTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = event.location,
                style = MaterialTheme.typography.bodySmall,
                color = InstagramTextMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onClick() }
        ) {
            Text(
                text = "View Details",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = InstagramBlue
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View Details",
                tint = InstagramBlue,
                modifier = Modifier.size(14.dp)
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
