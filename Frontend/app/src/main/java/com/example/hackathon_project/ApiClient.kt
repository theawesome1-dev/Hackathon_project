package com.example.hackathon_project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    // Change this to your computer's IP or cloud server URL
    private const val BASE_URL = "http://10.32.220.33:8000"

    // 1. Fetch Event Data (GET /data or /refresh)
    suspend fun fetchLatestEvent(endpoint: String = "/data"): Event? {
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
                    val end = json.optString("end", "")
                    val location = json.optString("location", "Unknown")
                    val description = json.optString("description", "")
                    val postUrl = json.optString("image_url", json.optString("post_url", ""))

                    connection.disconnect()
                    return@withContext Event(
                        id = System.currentTimeMillis(),
                        name = title,
                        date = parseAmericanDate(start),
                        startTime = parseAmericanTime(start),
                        endTime = parseAmericanTime(end),
                        location = location,
                        description = description,
                        postUrl = postUrl,
                        isActive = false
                    )
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }
    }

    // Overload for sending a single account name string
    suspend fun sendAccounts(accountName: String): Boolean = sendAccounts(listOf(accountName))
    suspend fun sendAccount(accountName: String): Boolean = sendAccounts(listOf(accountName))

    // 2. Send Account Names to Backend (POST /update)
    suspend fun sendAccounts(accountNames: List<String>): Boolean {
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

                val jsonArrayString = accountNames.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
                val jsonInputString = "{\"accounts\": $jsonArrayString}"

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
