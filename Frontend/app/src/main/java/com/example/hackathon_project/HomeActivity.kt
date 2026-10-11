package com.example.hackathon_project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hackathon_project.ui.theme.Hackathon_projectTheme
import com.example.hackathon_project.ui.theme.InstagramDivider
import com.example.hackathon_project.ui.theme.InstagramPinkGradient
import com.example.hackathon_project.ui.theme.InstagramSurface
import com.example.hackathon_project.ui.theme.InstagramTextMuted
import com.example.hackathon_project.ui.theme.InstagramTextPrimary
import com.example.hackathon_project.ui.theme.InstagramTextSecondary
import com.example.hackathon_project.ui.theme.InstagramWhite
import java.io.BufferedReader
import java.io.InputStreamReader

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hackathon_projectTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent
                ) { innerPadding ->
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
        if (output.isNotBlank()) {
            Toast.makeText(context, "Output: $output", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun HomeScreenContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        EventRepository.ensureLoaded(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(InstagramPinkGradient)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Header Section over gradient (White Text for High Contrast)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Event Hub",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = InstagramWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Instagram Post Scraper & Event Manager",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InstagramWhite.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Stats Summary Row (White Card)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InstagramSurface, RoundedCornerShape(12.dp))
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricItem(
                        count = "${EventRepository.activeEvents.size}",
                        label = "Active Events"
                    )
                    MetricItem(
                        count = "${EventRepository.inactiveEvents.size}",
                        label = "Pending Review"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "MANAGE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = InstagramWhite.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Settings/Action Row List (White Card)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InstagramSurface, RoundedCornerShape(14.dp))
                ) {
                    NavigationRow(
                        icon = Icons.Default.DateRange,
                        title = "Active Events",
                        subtitle = "View scheduled upcoming events",
                        onClick = {
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(
                        color = InstagramDivider,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    NavigationRow(
                        icon = Icons.Default.Info,
                        title = "Inactive Events",
                        subtitle = "Review and approve scraped posts",
                        onClick = {
                            runShellScript(context, "echo 'Opening Inactive Events'")
                            val intent = Intent(context, InactiveEventsActivity::class.java)
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(
                        color = InstagramDivider,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    NavigationRow(
                        icon = Icons.Default.AccountCircle,
                        title = "Connected Accounts",
                        subtitle = "Configure Instagram handles for scraping",
                        onClick = {
                            runShellScript(context, "echo 'Opening Accounts'")
                            val intent = Intent(context, AccountsActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Version 1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = InstagramWhite.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    count: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = InstagramTextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = InstagramTextSecondary
        )
    }
}

@Composable
fun NavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = InstagramTextPrimary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = InstagramTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InstagramTextSecondary
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Navigate",
            tint = InstagramTextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun HomeActivityPreview() {
    Hackathon_projectTheme {
        HomeScreenContent()
    }
}
