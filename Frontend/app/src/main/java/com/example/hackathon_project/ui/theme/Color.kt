package com.example.hackathon_project.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Authentic Instagram Light Palette
val InstagramBlack = Color(0xFF000000)
val InstagramWhite = Color(0xFFFFFFFF)
val InstagramLightBackground = Color(0xFFFAFAFA)
val InstagramSurface = Color(0xFFEFEFF2) // Slightly darker off-white surface so event boxes stand out
val InstagramSurfaceElevated = Color(0xFFE4E4E8)
val InstagramCardBorder = Color(0xFFD8D8DE)

val InstagramBlue = Color(0xFF0095F6)
val InstagramBluePressed = Color(0xFF1877F2)
val InstagramRed = Color(0xFFED4956)
val InstagramGreen = Color(0xFF34C759)

val InstagramTextPrimary = Color(0xFF0F0F12)
val InstagramTextSecondary = Color(0xFF55575C)
val InstagramTextMuted = Color(0xFF787880)
val InstagramDivider = Color(0xFFD8D8DE)

val InstagramPink = Color(0xFFE1306C)
val InstagramPurple = Color(0xFF833AB4)
val InstagramOrange = Color(0xFFF77737)
val InstagramYellow = Color(0xFFFCAF45)

// Legacy compatibility mapping
val moss = InstagramSurface
val darkBrown = InstagramLightBackground
val chocolate = InstagramCardBorder
val lightBrown = InstagramTextSecondary
val beige = InstagramTextPrimary
val brownGreen = InstagramBlue

// Vibrant Instagram Pink Gradient without any black at the bottom
val InstagramPinkGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF833AB4), // Instagram Purple
        Color(0xFFDD2A7B), // Instagram Pink / Magenta
        Color(0xFFF77737), // Instagram Coral / Orange
        Color(0xFFFCAF45)  // Instagram Warm Gold / Yellow
    )
)
