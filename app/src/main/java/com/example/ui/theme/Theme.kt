package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val GhostDarkColorScheme = darkColorScheme(
    primary = GhostBlue,
    onPrimary = GhostTextPrimary,
    primaryContainer = GhostBlueContainer,
    onPrimaryContainer = GhostCyan,
    secondary = GhostRed,
    onSecondary = GhostTextPrimary,
    secondaryContainer = GhostRedContainer,
    onSecondaryContainer = GhostRedGlow,
    tertiary = XboxGreen,
    onTertiary = GhostVoid,
    tertiaryContainer = XboxGreenDark,
    onTertiaryContainer = XboxGreen,
    background = GhostBackground,
    onBackground = GhostTextPrimary,
    surface = GhostSurface,
    onSurface = GhostTextPrimary,
    surfaceVariant = GhostSurfaceElevated,
    onSurfaceVariant = GhostTextSecondary,
    surfaceContainerLowest = GhostVoid,
    surfaceContainerLow = GhostBackground,
    surfaceContainer = GhostSurface,
    surfaceContainerHigh = GhostSurfaceElevated,
    surfaceContainerHighest = GhostSurfaceBright,
    outline = GhostBorder,
    outlineVariant = GhostBorder.copy(alpha = 0.55f),
    error = GhostRed,
    onError = GhostTextPrimary,
    errorContainer = GhostRedContainer,
    onErrorContainer = GhostRedGlow
)

private val GhostShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun GhostCloudTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GhostDarkColorScheme,
        typography = GhostTypography,
        shapes = GhostShapes,
        content = content
    )
}
