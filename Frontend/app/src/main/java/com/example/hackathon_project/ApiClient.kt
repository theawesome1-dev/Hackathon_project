package com.example.hackathon_project

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    // Change this to your computer's IP or cloud server URL
    private const val BASE_URL = "http://10.32.220.33:8000"

    // 1. Fetch Event Data and convert it directly into an Event object
    suspend fun fetchLatestEvent(endpoint: String = "/data"): Event? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL$endpoint")
                Log.d("ApiClient", "Fetching from: $url")
                
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 300000
                connection.readTimeout = 300000

                val responseCode = connection.responseCode
                Log.d("ApiClient", "Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("ApiClient", "Response JSON: $responseString")
                    
                    val json = JSONObject(responseString)

                    val title = json.optString("title", "Untitled")
                    val start = json.optString("start", "")
                    val location = json.optString("location", "Unknown")

                    val date = if (start.length >= 10) start.substring(0, 10) else "TBD"
                    val time = if (start.length >= 16) start.substring(11, 16) else "TBD"

                    connection.disconnect()
                    
                    return@withContext Event(
                        id = System.currentTimeMillis(),
                        name = title,
                        date = date,
                        time = time,
                        location = location,
                        isActive = false
                    )
                } else {
                    Log.e("ApiClient", "Error response code: $responseCode")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("ApiClient", "Exception during fetch", e)
            }
            null
        }
    }

    // 2. Send a List of Account Names to Backend (POST /update)
    suspend fun sendAccounts(accountNames: List<String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/update")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val jsonArrayString = accountNames.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
                val jsonInputString = "{\"accounts\": $jsonArrayString}"

                connection.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                Log.d("ApiClient", "Send Accounts Response Code: $responseCode")
                
                val success = responseCode == HttpURLConnection.HTTP_OK
                connection.disconnect()
                return@withContext success
            } catch (e: Exception) {
                Log.e("ApiClient", "Exception sending accounts", e)
                false
            }
        }
    }
}
