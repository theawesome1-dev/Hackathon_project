package com.example.hackathon_project.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.example.hackathon_project.MyScreenContent

private val DarkColorScheme = darkColorScheme(
    primary = moss,
    secondary = lightBrown,
    tertiary = chocolate,
    background = darkBrown,
    surface = moss,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = beige,
    onSurface = beige
)

private val LightColorScheme = lightColorScheme(
    primary = moss,
    secondary = lightBrown,
    tertiary = chocolate,
    background = moss,
    surface = beige,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = darkBrown,
    onSurface = darkBrown
)

private val Instagram = lightColorScheme(
    primary = InstagramBlue,
    secondary = InstagramBlack,
    tertiary = InstagramBlack,
    background = InstagramBlack,
    surface = InstagramGrey,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = InstagramWhite,
    onSurface = InstagramWhite
)




//val InstagramBlack = Color(0xFF18181B)
//val InstagramBlue = Color(0xFF3479E9)
//val InstagramWhite = Color(0xFFFFFFFF)

@Composable
fun Hackathon_projectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor = false by default so your custom color scheme (moss, beige, brown) is used
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> Instagram
        else -> Instagram
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun GreetingPreview() {
    Hackathon_projectTheme {
        MyScreenContent()
    }
}
