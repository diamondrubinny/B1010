package net.rubinny.willowbrook.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val LocalWillowColors = staticCompositionLocalOf { StandardColors }

/** When true, focus animations (scale) are disabled. */
val LocalReduceMotion = staticCompositionLocalOf { false }

@Composable
fun WillowbrookTheme(
    textScale: Float = 1f,
    highContrast: Boolean = false,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (highContrast) HighContrastColors else StandardColors

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.textOnFocus,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
        ),
    ) {
        CompositionLocalProvider(
            LocalWillowColors provides colors,
            LocalReduceMotion provides reduceMotion,
            LocalTextScale provides textScale,
            content = content,
        )
    }
}
