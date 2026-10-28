package net.rubinny.willowbrook.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Palette tuned for older eyes on a TV at 3 m: warm, low-glare dark background,
 * text contrast >= 7:1 (WCAG AAA) for body copy. Ratios are documented in docs/DESIGN_SYSTEM.md.
 */
@Immutable
data class WillowColors(
    val background: Color,
    val surface: Color,
    val surfaceFocused: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textOnFocus: Color,
    val accent: Color,
    val focusRing: Color,
    val danger: Color,
    val success: Color,
)

val StandardColors = WillowColors(
    background = Color(0xFF16202A),     // deep slate
    surface = Color(0xFF243444),        // card
    surfaceFocused = Color(0xFFF2C14E), // lantern gold fill when focused
    textPrimary = Color(0xFFFFF8EC),    // warm white  — 15.6:1 on background, 12.1:1 on surface
    textSecondary = Color(0xFFD9D2C3),  // parchment   — 10.9:1 on background, 8.5:1 on surface
    textOnFocus = Color(0xFF16202A),    // dark text on gold — 9.8:1
    accent = Color(0xFFF2C14E),
    focusRing = Color(0xFFFFFFFF),
    danger = Color(0xFFFFB4A8),         // soft coral, never alarming red
    success = Color(0xFFB8E0B0),
)

val HighContrastColors = WillowColors(
    background = Color(0xFF000000),
    surface = Color(0xFF1A1A1A),
    surfaceFocused = Color(0xFFFFFF00),
    textPrimary = Color(0xFFFFFFFF),    // 21:1
    textSecondary = Color(0xFFFFFFFF),
    textOnFocus = Color(0xFF000000),    // 19.6:1 on yellow
    accent = Color(0xFFFFFF00),
    focusRing = Color(0xFFFFFFFF),
    danger = Color(0xFFFFB4A8),
    success = Color(0xFFB8FFB0),
)
