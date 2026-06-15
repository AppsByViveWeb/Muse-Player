package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantPrimary,
    secondary = ElegantSecondary,
    tertiary = ElegantPrimary,
    background = ElegantBackground,
    surface = ElegantSurface,
    onPrimary = ElegantBackground,
    onSecondary = ElegantText,
    onTertiary = ElegantText,
    onBackground = ElegantText,
    onSurface = ElegantText,
    surfaceVariant = ElegantSecondary,
    onSurfaceVariant = ElegantMutedText
  )

private val LightColorScheme = DarkColorScheme // Elegant Dark app is dark-first/only or defaults completely to this magnificent dark style.

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark-first for Elegant Dark feel
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve exact theme aesthetics
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
