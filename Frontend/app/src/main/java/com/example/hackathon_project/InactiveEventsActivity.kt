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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.hackathon_project.ui.theme.Hackathon_projectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

val inactiveEventsList = mutableStateListOf<Event>()

class InactiveEventsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        fetchEventFromFastApi()
        setContent {
            Hackathon_projectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPanelPadding ->
                    InactiveEventsContent(
                        modifier = Modifier.padding(innerPanelPadding),
                        onFetchClicked = { fetchEventFromFastApi() }
                    )
                }
            }
        }
    }

    private fun fetchEventFromFastApi() {
        lifecycleScope.launch(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("http://26.29.51.138:8000/refresh")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 180_000

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
                        add(
                            Event(
                                name = title,
                                date = if (start.length >= 10) start.substring(0, 10) else "TBD",
                                time = if (start.length >= 16) start.substring(11, 16) else "TBD",
                                location = eventJson.optString("location", "Unknown"),
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
        val alreadyPresent = inactiveEventsList.any {
            it.name == event.name && it.date == event.date
        }
        if (!alreadyPresent) {
            val newId = (inactiveEventsList.maxOfOrNull { it.id } ?: 100) + 1
            inactiveEventsList.add(event.copy(id = newId))
        }
    }
}

@Composable
fun InactiveEventsContent(modifier: Modifier = Modifier, onFetchClicked: () -> Unit = {}) {
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Inactive Events",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    Button(
                        onClick = onFetchClicked,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text("Refresh Events from Backend")
                    }
                }
            }

            if (inactiveEventsList.isEmpty()) {
                item {
                    Text(
                        text = "No inactive events found. Tap refresh to load from backend.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(inactiveEventsList, key = { it.id }) { eventItem ->
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
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun InactiveEventsPreview() {
    Hackathon_projectTheme {
        InactiveEventsContent()
    }
}