package com.example.hackathon_project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    // Change this to your computer's IP or cloud server URL
    private const val BASE_URL = "http://10.32.220.33:8000"
    //10.32.220.33
    //26.29.51.138

    // 1. Fetch Event Data (GET /data or /refresh)
    suspend fun fetchLatestEvent(endpoint: String = "/data"): EventData? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL$endpoint")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 30000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseString)

                    val title = json.optString("title", "Untitled")
                    val start = json.optString("start", "")
                    val location = json.optString("location", "Unknown")

                    val date = if (start.length >= 10) start.substring(0, 10) else "TBD"
                    val time = if (start.length >= 16) start.substring(11, 16) else "TBD"

                    connection.disconnect()
                    return@withContext EventData(title, date, time, location)
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }
    }

    // 2. Send a List of Account Names to Backend (POST /update)
    suspend fun sendAccounts(accountName : String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/update")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                // Build JSON array string from List<String>
                val jsonInputString = "{\"accounts\": $accountName}"

                connection.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val success = connection.responseCode == HttpURLConnection.HTTP_OK
                connection.disconnect()
                return@withContext success
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}

// Simple data holder for network results
data class EventData(
    val title: String,
    val date: String,
    val time: String,
    val location: String
)
