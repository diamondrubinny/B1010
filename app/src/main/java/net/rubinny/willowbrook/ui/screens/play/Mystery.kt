package net.rubinny.willowbrook.ui.screens.play

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.rubinny.willowbrook.game.model.Item
import net.rubinny.willowbrook.ui.components.ItemPicture
import net.rubinny.willowbrook.ui.theme.LocalReduceMotion
import net.rubinny.willowbrook.ui.theme.LocalWillowColors

/**
 * A clue under a magnifying glass: a soft golden glow that breathes slowly (one breath every 3 seconds;
 * steady with "Less movement on screen"), the clue in the lens and the handle below. Decorative: the clue's
 * name is always written next to it.
 */
@Composable
fun ClueGlass(item: Item, size: Dp, modifier: Modifier = Modifier) {
    val colors = LocalWillowColors.current
    val glow = if (LocalReduceMotion.current) {
        0.55f
    } else {
        val t = rememberInfiniteTransition(label = "clueGlow")
        val v by t.animateFloat(0.35f, 0.75f, infiniteRepeatable(tween(1_500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "clueGlowAlpha")
        v
    }
    Box(modifier.size(size).clearAndSetSemantics { }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = this.size.minDimension * 0.4f
            val c = Offset(this.size.width * 0.45f, this.size.height * 0.45f)
            drawCircle(Brush.radialGradient(listOf(colors.accent.copy(alpha = glow), Color.Transparent), center = c, radius = r * 1.4f), r * 1.4f, c)
            drawCircle(Color(0xFFFFF8EC).copy(alpha = 0.92f), r, c)
            drawLine(
                colors.accent,
                start = Offset(c.x + r * 0.72f, c.y + r * 0.72f),
                end = Offset(this.size.width * 0.95f, this.size.height * 0.95f),
                strokeWidth = r * 0.22f,
                cap = StrokeCap.Round,
            )
            drawCircle(colors.accent, r, c, style = Stroke(width = r * 0.12f))
        }
        Box(Modifier.size(size * 0.9f), contentAlignment = Alignment.Center) {
            Box(Modifier.size(size * 0.5f).align(Alignment.Center), contentAlignment = Alignment.Center) {
                ItemPicture(item.id, item.emoji, size * 0.5f)
            }
        }
    }
}

/** The clues shown with the "Who was it?" question: pictures in a row, each named for TalkBack. */
@Composable
fun ClueStrip(clues: List<Item>, modifier: Modifier = Modifier, pictureSize: Dp = 64.dp) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        clues.forEach { clue ->
            Box(Modifier.size(pictureSize).semantics { contentDescription = "Clue: ${clue.label}" }, contentAlignment = Alignment.Center) {
                ItemPicture(clue.id, clue.emoji, pictureSize)
            }
        }
    }
}
