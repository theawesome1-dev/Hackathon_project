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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.hackathon_project.ui.theme.Hackathon_projectTheme
import com.example.hackathon_project.ui.theme.InstagramAppGradient
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
                    containerColor = Color.Transparent
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
    val textColor = Color.White

    Box(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(InstagramAppGradient),
            color = Color.Transparent,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(12.dp))
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Inactive Events (${EventRepository.inactiveEvents.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    },
                    navigationIcon = {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(context, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                context.startActivity(intent)
                                activity?.finish()
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Back to Home", color = textColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = textColor
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
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
                        enabled = isButtonEnabled,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(if (isButtonEnabled) "Fetch New Posts" else "Please wait...")
                    }
                }

                if (EventRepository.inactiveEvents.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = "No inactive events found. Tap 'Fetch New Posts' to load from backend.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.9f),
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(EventRepository.inactiveEvents, key = { it.id }) { eventItem ->
                            InactiveEventCard(
                                event = eventItem,
                                onAdd = { onAddEvent(eventItem) },
                                onDelete = { onDeleteEvent(eventItem) },
                                onClick = { selectedEvent = eventItem }
                            )
                        }
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
fun InactiveEventCard(
    event: Event,
    onAdd: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                    text = "📍 ${event.location} (Inactive)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAdd) {
                    Text("Add")
                }
                TextButton(onClick = onDelete) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
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
