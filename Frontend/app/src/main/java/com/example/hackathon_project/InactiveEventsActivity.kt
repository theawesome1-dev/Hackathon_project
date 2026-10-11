package com.example.hackathon_project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
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
import android.provider.CalendarContract

class InactiveEventsActivity : ComponentActivity() {
    private var onNewEventCallback: ((Event) -> Unit)? = null

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
                        onDeleteEvent = { event -> EventRepository.removeInactive(event) },
                        registerNewEventCallback = { callback -> onNewEventCallback = callback }
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
                connection.readTimeout = 300000

                val responseCode = connection.responseCode
                val responseStream = if (responseCode in 200..1000) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val responseString = responseStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                Log.d("FastAPI_Response", responseString)

                if (responseCode !in 200..299) {
                    throw IllegalStateException("Backend returned HTTP $responseCode: $responseString")
                }

                val fetchedEvents = mutableListOf<Event>()
                val trimmed = responseString.trim()

                if (trimmed.startsWith("[")) {
                    val eventArray = JSONArray(trimmed)
                    for (index in 0 until eventArray.length()) {
                        val eventJson = eventArray.optJSONObject(index) ?: continue
                        parseEventJson(eventJson)?.let { fetchedEvents.add(it) }
                    }
                } else if (trimmed.startsWith("{")) {
                    val jsonObject = org.json.JSONObject(trimmed)
                    if (jsonObject.has("events") || jsonObject.has("data")) {
                        val arrayKey = if (jsonObject.has("events")) "events" else "data"
                        val eventArray = jsonObject.optJSONArray(arrayKey)
                        if (eventArray != null) {
                            for (index in 0 until eventArray.length()) {
                                val eventJson = eventArray.optJSONObject(index) ?: continue
                                parseEventJson(eventJson)?.let { fetchedEvents.add(it) }
                            }
                        }
                    } else {
                        parseEventJson(jsonObject)?.let { fetchedEvents.add(it) }
                    }
                }

                withContext(Dispatchers.Main) {
                    val newlyAdded = receiveEvents(fetchedEvents)
                    if (newlyAdded.isNotEmpty()) {
                        val latest = newlyAdded.first()
                        Toast.makeText(this@InactiveEventsActivity, "New post detected: ${latest.name}", Toast.LENGTH_LONG).show()
                        onNewEventCallback?.invoke(latest)
                    } else {
                        Toast.makeText(this@InactiveEventsActivity, "No new posts detected", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("FastAPI_Error", "Connection timed out while loading events from backend", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InactiveEventsActivity, "Request timed out. Server is taking too long to respond.", Toast.LENGTH_LONG).show()
                }
            } catch (e: java.net.SocketException) {
                Log.e("FastAPI_Error", "Socket closed or network error while loading events from backend", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InactiveEventsActivity, "Connection closed by server or network interrupted.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("FastAPI_Error", "Could not load events from backend", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InactiveEventsActivity, "Error connecting to backend: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun parseEventJson(eventJson: org.json.JSONObject): Event? {
        val title = eventJson.optString("title", eventJson.optString("name", "")).takeIf { it.isNotBlank() } ?: return null
        val start = eventJson.optString("start", eventJson.optString("date", ""))
        val end = eventJson.optString("end", "")
        val description = eventJson.optString("description", "")
        val postUrl = eventJson.optString("image_url", eventJson.optString("post_url", ""))
        val location = eventJson.optString("location", "Unknown")

        val date = if (start.length >= 10) start.substring(0, 10) else if (start.isNotBlank()) start else "TBD"
        val startTime = if (start.length >= 16) start.substring(11, 16) else if (start.isNotBlank()) start else "TBD"
        val endTime = if (end.length >= 16) end.substring(11, 16) else end

        return Event(
            name = title,
            date = date,
            start = startTime,
            end = endTime,
            location = location,
            description = description,
            postUrl = postUrl,
            isActive = false,
        )
    }
}

fun receiveEvents(events: List<Event>): List<Event> {
    val newlyAdded = mutableListOf<Event>()
    events.forEach { event ->
        val key = "${event.name}_${event.date}"
        val alreadyPresent = EventRepository.inactiveEvents.any { "${it.name}_${it.date}" == key }
        val alreadyActive = EventRepository.activeEvents.any { "${it.name}_${it.date}" == key }
        val alreadyDismissed = EventRepository.dismissedEvents.contains(key)

        if (!alreadyPresent && !alreadyActive && !alreadyDismissed) {
            val newId = (EventRepository.inactiveEvents.maxOfOrNull { it.id } ?: 100) + 1
            val newEvent = event.copy(id = newId)
            EventRepository.inactiveEvents.add(newEvent)
            newlyAdded.add(newEvent)
        }
    }
    return newlyAdded
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InactiveEventsContent(
    modifier: Modifier = Modifier,
    onFetchClicked: () -> Unit = {},
    onAddEvent: (Event) -> Unit = {},
    onDeleteEvent: (Event) -> Unit = {},
    registerNewEventCallback: ((Event) -> Unit) -> Unit = {}
) {
    var isButtonEnabled by remember { mutableStateOf(true) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val textColor = Color.White

    LaunchedEffect(Unit) {
        registerNewEventCallback { event ->
            selectedEvent = event
        }
    }

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
                onDismiss = { selectedEvent = null },
                onDelete = {
                    EventRepository.removeInactive(selectedEvent!!)
                    selectedEvent = null
                }
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
    val context = LocalContext.current

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
                    text = "${event.date} from ${event.start} to ${event.end}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "📍 ${event.location} (Inactive)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = onAdd) {
                    Text("Add")
                }
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, event.name)
                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, parseDateTimeToMillis(event.date, event.start))
                        if (event.end.isNotBlank()) {
                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, parseDateTimeToMillis(event.date, event.end))
                        }
                        putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                        putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                    }
                    context.startActivity(intent)
                }) {
                    Text(text = "Calendar")
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

private fun parseDateTimeToMillis(dateStr: String, timeStr: String): Long {
    try {
        val formats = listOf(
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()),
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()),
            java.text.SimpleDateFormat("MMM dd yyyy HH:mm", java.util.Locale.getDefault()),
            java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        )
        val combined = "$dateStr $timeStr".trim()
        for (format in formats) {
            try {
                format.isLenient = true
                val date = format.parse(combined)
                if (date != null) return date.time
            } catch (e: Exception) {
                // try next
            }
            try {
                format.isLenient = true
                val date = format.parse(dateStr.trim())
                if (date != null) return date.time
            } catch (e: Exception) {
                // try next
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return System.currentTimeMillis()
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun InactiveEventsPreview() {
    Hackathon_projectTheme {
        InactiveEventsContent()
    }
}
