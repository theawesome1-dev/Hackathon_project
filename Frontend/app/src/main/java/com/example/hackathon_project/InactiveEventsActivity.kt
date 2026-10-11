package com.example.hackathon_project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class InactiveEventsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        fetchEventFromFastApi()
        setContent {
            Hackathon_projectTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = InstagramLightBackground
                ) { innerPanelPadding ->
                    InactiveEventsContent(
                        modifier = Modifier.padding(innerPanelPadding),
                        onFetchClicked = { fetchEventFromFastApi() },
                        onAddEvent = { event -> EventRepository.promoteToActive(event) },
                        onDeleteEvent = { event -> EventRepository.removeInactive(event) }
                    )
                }
            }
        }
    }

    private fun fetchEventFromFastApi() {
        lifecycleScope.launch(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("http://10.32.220.33:8000/refresh")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 600000
                connection.readTimeout = 600000

                val responseCode = connection.responseCode
                val responseStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val responseString = responseStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                Log.d("FastAPI_Response", responseString)

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw IllegalStateException("Backend returned HTTP $responseCode: $responseString")
                }

                val eventArray = JSONArray(responseString)
                val fetchedEvents = buildList {
                    for (index in 0 until eventArray.length()) {
                        val eventJson = eventArray.optJSONObject(index) ?: continue
                        val title = eventJson.optString("title").takeIf { it.isNotBlank() } ?: continue
                        val start = eventJson.optString("start")
                        val end = eventJson.optString("end")
                        val description = eventJson.optString("description", "")
                        val postUrl = eventJson.optString("image_url", eventJson.optString("post_url", ""))
                        add(
                            Event(
                                name = title,
                                date = parseAmericanDate(start),
                                startTime = parseAmericanTime(start),
                                endTime = parseAmericanTime(end),
                                location = eventJson.optString("location", "Unknown"),
                                description = description,
                                postUrl = postUrl,
                                isActive = false,
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    receiveEvents(fetchedEvents)
                }
            } catch (e: Exception) {
                Log.e("FastAPI_Error", "Could not load events from backend", e)
            } finally {
                connection?.disconnect()
            }
        }
    }
}

fun receiveEvents(events: List<Event>) {
    events.forEach { event ->
        val key = "${event.name}_${event.date}"
        val alreadyPresent = EventRepository.inactiveEvents.any { "${it.name}_${it.date}" == key }
        val alreadyActive = EventRepository.activeEvents.any { "${it.name}_${it.date}" == key }
        val alreadyDismissed = EventRepository.dismissedEvents.contains(key)

        if (!alreadyPresent && !alreadyActive && !alreadyDismissed) {
            val newId = (EventRepository.inactiveEvents.maxOfOrNull { it.id } ?: 100) + 1
            EventRepository.inactiveEvents.add(event.copy(id = newId))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InactiveEventsContent(
    modifier: Modifier = Modifier,
    onFetchClicked: () -> Unit = {},
    onAddEvent: (Event) -> Unit = {},
    onDeleteEvent: (Event) -> Unit = {}
) {
    var isButtonEnabled by remember { mutableStateOf(true) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    Box(modifier = modifier.fillMaxSize().background(InstagramLightBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Instagram Top Header Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Inactive Events (${EventRepository.inactiveEvents.size})",
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
                            activity?.finish()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = InstagramTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isButtonEnabled) {
                                isButtonEnabled = false
                                onFetchClicked()
                                coroutineScope.launch {
                                    delay(10000L) // 10 second cooldown
                                    isButtonEnabled = true
                                }
                            }
                        },
                        enabled = isButtonEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Fetch New Posts",
                            tint = if (isButtonEnabled) InstagramBlue else InstagramTextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = InstagramLightBackground,
                    titleContentColor = InstagramTextPrimary
                )
            )

            HorizontalDivider(color = InstagramDivider, thickness = 1.dp)

            if (EventRepository.inactiveEvents.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Inactive Events",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = InstagramTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap the refresh icon in the top bar to fetch new posts from your connected Instagram accounts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InstagramTextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (isButtonEnabled) {
                                isButtonEnabled = false
                                onFetchClicked()
                                coroutineScope.launch {
                                    delay(10000L)
                                    isButtonEnabled = true
                                }
                            }
                        },
                        enabled = isButtonEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = InstagramBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isButtonEnabled) "Fetch New Posts" else "Please wait...",
                            color = InstagramWhite
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(EventRepository.inactiveEvents, key = { it.id }) { eventItem ->
                        InactiveEventItem(
                            event = eventItem,
                            onAdd = { onAddEvent(eventItem) },
                            onDelete = { onDeleteEvent(eventItem) },
                            onClick = { selectedEvent = eventItem }
                        )
                    }
                }
            }
        }

        if (selectedEvent != null) {
            EventDetailDialog(
                event = selectedEvent!!,
                onDismiss = { selectedEvent = null }
            )
        }
    }
}

@Composable
fun InactiveEventItem(
    event: Event,
    onAdd: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Clean single-surface row container (Light mode surface)
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
                text = "${event.date} at ${event.time}",
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

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = InstagramBlue),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = InstagramWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add to Active",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = InstagramWhite
                )
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
