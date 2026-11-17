package com.example.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Ghost-Tech Obsidian & Dual-Neon Palette
val GhostVoid = Color(0xFF06080F)
val GhostBackground = Color(0xFF090C16)
val GhostSurface = Color(0xFF101526)
val GhostSurfaceElevated = Color(0xFF171F36)
val GhostSurfaceBright = Color(0xFF1F2A47)
val GhostBorder = Color(0xFF283558)

// Signature Ghost Red -> Ghost Blue Accents (matching bg_ghost_gradient)
val GhostRed = Color(0xFFFF1E3C)
val GhostRedGlow = Color(0xFFFF4D66)
val GhostRedContainer = Color(0xFF3D0C19)

val GhostBlue = Color(0xFF0088FF)
val GhostCyan = Color(0xFF00E5FF)
val GhostBlueContainer = Color(0xFF0B2545)

// Semantic Console Accents
val XboxGreen = Color(0xFF19C358)
val XboxGreenDark = Color(0xFF0D331D)
val PremiumGold = Color(0xFFFFB800)
val PremiumGoldContainer = Color(0xFF3B2B07)

// Xbox Face Button Colors
val XboxButtonA = Color(0xFF19C358)
val XboxButtonB = Color(0xFFFF2E4C)
val XboxButtonX = Color(0xFF0088FF)
val XboxButtonY = Color(0xFFFFC400)

// Text Hierarchy
val GhostTextPrimary = Color(0xFFF3F6FF)
val GhostTextSecondary = Color(0xFF9AA8C7)
val GhostTextMuted = Color(0xFF667599)

val GhostDiagonalBrush = Brush.linearGradient(
    colors = listOf(GhostRed, GhostBlue),
    start = Offset(0f, 0f),
    end = Offset(900f, 900f)
)

val GhostHorizontalBrush = Brush.horizontalGradient(
    colors = listOf(GhostRed, GhostBlue)
)

val GhostSubtleGlowBrush = Brush.linearGradient(
    colors = listOf(
        GhostRed.copy(alpha = 0.22f),
        GhostBlue.copy(alpha = 0.22f)
    )
)
