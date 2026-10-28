package net.rubinny.willowbrook.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import net.rubinny.willowbrook.R

/** Atkinson Hyperlegible (SIL OFL 1.1) — designed by the Braille Institute for low-vision readers. */
val Atkinson = FontFamily(
    Font(R.font.atkinson_regular, FontWeight.Normal),
    Font(R.font.atkinson_bold, FontWeight.Bold),
)

/** Caregiver text-size multiplier (1.0 – 1.6). */
val LocalTextScale = staticCompositionLocalOf { 1f }

/**
 * 10-foot UI sizes. Body never goes below 28sp.
 *
 * The caregiver text scale is applied here, linearly. We deliberately do NOT use Density.fontScale for it:
 * Android 14+ applies non-linear font scaling, so large TV text (30sp+) would grow only ~5% at "160%".
 * The system font-size setting still applies on top through normal sp conversion.
 */
object WillowType {
    val display: TextStyle @Composable @ReadOnlyComposable get() = style(56.sp, 66.sp, FontWeight.Bold)
    val title: TextStyle @Composable @ReadOnlyComposable get() = style(40.sp, 50.sp, FontWeight.Bold)
    val button: TextStyle @Composable @ReadOnlyComposable get() = style(34.sp, 42.sp, FontWeight.Bold)
    val body: TextStyle @Composable @ReadOnlyComposable get() = style(30.sp, 42.sp, FontWeight.Normal)
    val label: TextStyle @Composable @ReadOnlyComposable get() = style(28.sp, 36.sp, FontWeight.Normal)

    @Composable
    @ReadOnlyComposable
    private fun style(size: TextUnit, lineHeight: TextUnit, weight: FontWeight): TextStyle {
        val scale = LocalTextScale.current
        return TextStyle(fontFamily = Atkinson, fontWeight = weight, fontSize = size * scale, lineHeight = lineHeight * scale)
    }
}
