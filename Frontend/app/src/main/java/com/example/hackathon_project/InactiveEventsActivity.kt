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
import androidx.compose.runtime.LaunchedEffect
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
                    containerColor = InstagramLightBackground
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

    LaunchedEffect(Unit) {
        registerNewEventCallback { event ->
            selectedEvent = event
        }
    }

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
fun InactiveEventItem(
    event: Event,
    onAdd: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Card(
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
