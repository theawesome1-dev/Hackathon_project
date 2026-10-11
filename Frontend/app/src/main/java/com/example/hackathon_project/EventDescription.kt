package com.example.hackathon_project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hackathon_project.ui.theme.Hackathon_projectTheme
import com.example.hackathon_project.ui.theme.InstagramLightBackground
import com.example.hackathon_project.ui.theme.InstagramSurface
import com.example.hackathon_project.ui.theme.InstagramTextPrimary
import com.example.hackathon_project.ui.theme.InstagramTextSecondary

@Composable
fun Event(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(InstagramLightBackground),
        color = InstagramLightBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Example Event",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = InstagramTextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InstagramSurface, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Organizer Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = InstagramTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Name: Event Coordinator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InstagramTextSecondary
                )
                Text(
                    text = "Email: events@example.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InstagramTextSecondary
                )
                Text(
                    text = "Role: Organizer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InstagramTextSecondary
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun EventsPreview() {
    Hackathon_projectTheme {
        Event()
    }
}
