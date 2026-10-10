package com.example.hackathon_project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hackathon_project.ui.theme.Hackathon_projectTheme
import java.io.BufferedReader
import java.io.InputStreamReader

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hackathon_projectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreenContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Optional helper function to execute shell scripts/commands on the Android device
fun runShellScript(context: Context, scriptCommand: String) {
    try {
        val process = Runtime.getRuntime().exec(scriptCommand)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val output = reader.readText()
        process.waitFor()
        Toast.makeText(context, "Shell Output: $output", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Script executed: $scriptCommand", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun HomeScreenContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Event App",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your central hub for all hackathon events.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Button 1: View Active Events
            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = "View Active Events",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Button 2: Inactive Events
            Button(
                onClick = {
                    // Executes optional shell script command if needed:
                    runShellScript(context, "echo 'Opening Inactive Events'")

                    // Opens Inactive Events Activity page
                    val intent = Intent(context, InactiveEventsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = "Inactive Events",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Button 3: Accounts
            OutlinedButton(
                onClick = {
                    // Executes optional shell script command if needed:
                    runShellScript(context, "echo 'Opening Accounts'")

                    // Opens Accounts Activity page
                    val intent = Intent(context, AccountsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = "Accounts",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun HomeActivityPreview() {
    Hackathon_projectTheme {
        HomeScreenContent()
    }
}
