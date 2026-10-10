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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

// Shared mutable state list so receiveEvent can add items and Compose UI reacts to updates
val inactiveEventsList = mutableStateListOf(
    Event(id = 101, name = "Past Planning Meeting", date = "Feb 10", time = "10:00 AM", location = "Room 202", isActive = false),
    Event(id = 102, name = "Old Workshop", date = "Feb 15", time = "02:00 PM", location = "Lab 5", isActive = false),
)

class InactiveEventsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
            try {
                // Call FastAPI /refresh endpoint (10.0.2.2 for Android Emulator)
                val url = URL("http://26.29.51.138:8000/refresh")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000



                // Inside fetchEventFromFastApi():
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("FastAPI_Response", responseString) // <-- Prints to Logcat tab in Android Studio

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseString)

                    val title = json.optString("title", "Untitled")
                    val start = json.optString("start", "")
                    val location = json.optString("location", "Unknown")

                    val date = if (start.length >= 10) start.substring(0, 10) else "TBD"
                    val time = if (start.length >= 16) start.substring(11, 16) else "TBD"

                    withContext(Dispatchers.Main) {
                        receiveEvent(name = title, date = date, time = time, location = location)
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun receiveEvent(name: String, date: String, time: String, location: String) {
    val newId = (inactiveEventsList.maxOfOrNull { it.id } ?: 100) + 1
    inactiveEventsList.add(Event(id = newId, name = name, date = date, time = time, location = location, isActive = false))
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
                        Text("Fetch Latest Event from Backend")
                    }
                }
            }
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

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun InactiveEventsPreview() {
    Hackathon_projectTheme {
        InactiveEventsContent()
    }
}