package com.example.hackathon_project.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val InstagramLightColorScheme = lightColorScheme(
    primary = InstagramBlue,
    onPrimary = Color.White,
    primaryContainer = InstagramSurfaceElevated,
    onPrimaryContainer = InstagramTextPrimary,
    secondary = InstagramPink,
    onSecondary = Color.White,
    tertiary = InstagramOrange,
    onTertiary = Color.White,
    background = InstagramLightBackground,
    onBackground = InstagramTextPrimary,
    surface = InstagramSurface,
    onSurface = InstagramTextPrimary,
    surfaceVariant = InstagramSurfaceElevated,
    onSurfaceVariant = InstagramTextSecondary,
    outline = InstagramCardBorder,
    outlineVariant = InstagramDivider,
    error = InstagramRed,
    onError = Color.White
)

@Composable
fun Hackathon_projectTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = InstagramLightColorScheme,
        typography = Typography,
        content = content
    )
}
