package com.example.hackathon_project

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hackathon_project.ui.theme.InstagramBlue
import com.example.hackathon_project.ui.theme.InstagramRed
import com.example.hackathon_project.ui.theme.InstagramSurface
import com.example.hackathon_project.ui.theme.InstagramSurfaceElevated
import com.example.hackathon_project.ui.theme.InstagramTextMuted
import com.example.hackathon_project.ui.theme.InstagramTextPrimary
import com.example.hackathon_project.ui.theme.InstagramTextSecondary
import com.example.hackathon_project.ui.theme.InstagramWhite

@Composable
fun EventDetailDialog(
    event: Event,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InstagramSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = InstagramTextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Date & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date",
                        tint = InstagramTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (event.end.isNotBlank()) "${event.date} from ${event.start} to ${event.end}" else "${event.date} at ${event.start}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = InstagramTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = InstagramTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InstagramTextSecondary
                    )
                }

                if (event.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DESCRIPTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = InstagramTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InstagramSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = InstagramTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Export to Calendar Action Button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, event.name)
                            putExtra(
                                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                parseDateTimeToMillis(event.date, event.start)
                            )
                            if (event.end.isNotBlank()) {
                                putExtra(
                                    CalendarContract.EXTRA_EVENT_END_TIME,
                                    parseDateTimeToMillis(event.date, event.end)
                                )
                            }
                            putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InstagramSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Export to Calendar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = InstagramTextPrimary
                    )
                }

                if (event.postUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.postUrl))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = InstagramBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Open Original Post",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = InstagramWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open",
                                tint = InstagramWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = InstagramRed)
                    ) {
                        Text("Delete", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.labelLarge,
                        color = InstagramTextMuted
                    )
                }
            }
        }
    )
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
