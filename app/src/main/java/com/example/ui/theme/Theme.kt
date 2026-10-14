package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EduPrimaryDark,
    onPrimary = EduOnPrimaryDark,
    primaryContainer = EduPrimaryContainerDark,
    onPrimaryContainer = EduOnPrimaryContainerDark,
    secondary = EduSecondaryDark,
    onSecondary = EduOnSecondaryDark,
    secondaryContainer = EduSecondaryContainerDark,
    onSecondaryContainer = EduOnSecondaryContainerDark,
    tertiary = EduTertiaryDark,
    onTertiary = EduOnTertiaryDark,
    tertiaryContainer = EduTertiaryContainerDark,
    onTertiaryContainer = EduOnTertiaryContainerDark,
    background = EduBackgroundDark,
    surface = EduSurfaceDark,
    surfaceVariant = EduSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = EduPrimary,
    onPrimary = EduOnPrimary,
    primaryContainer = EduPrimaryContainer,
    onPrimaryContainer = EduOnPrimaryContainer,
    secondary = EduSecondary,
    onSecondary = EduOnSecondary,
    secondaryContainer = EduSecondaryContainer,
    onSecondaryContainer = EduOnSecondaryContainer,
    tertiary = EduTertiary,
    onTertiary = EduOnTertiary,
    tertiaryContainer = EduTertiaryContainer,
    onTertiaryContainer = EduOnTertiaryContainer,
    background = EduBackgroundLight,
    surface = EduSurfaceLight,
    surfaceVariant = EduSurfaceVariantLight
)

@Composable
fun EduVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false by default so our custom brand palette shines!
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
