package net.rubinny.willowbrook.ui.screens.play

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import net.rubinny.willowbrook.game.model.SpellSymbol
import net.rubinny.willowbrook.ui.components.ArtPicture
import net.rubinny.willowbrook.ui.theme.LocalReduceMotion
import net.rubinny.willowbrook.ui.theme.LocalWillowColors

/** Ring colour of each bead. The symbol inside always carries the meaning too (never colour alone). */
fun beadColor(symbol: SpellSymbol): Color = when (symbol) {
    SpellSymbol.STAR -> Color(0xFFF2C14E)
    SpellSymbol.MOON -> Color(0xFF7FB2E5)
    SpellSymbol.LEAF -> Color(0xFF7CB35A)
    SpellSymbol.FLOWER -> Color(0xFFE8608A)
}

/** Enough focus targets for the longest possible string (13 + 1 beads, so 15 gaps) with room to spare. */
const val MAX_BEAD_GAPS = 20

/** One light bead: a cream pearl with a coloured ring and its symbol. Decorative (the gaps are described). */
@Composable
fun BeadView(symbol: SpellSymbol, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color(0xFFFFF8EC), CircleShape)
            .border(size * 0.09f, beadColor(symbol), CircleShape)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) { ArtPicture("sym_${symbol.id}", symbol.emoji, size * 0.62f) }
}

/**
 * The string of beads. The player moves along the places *between* beads with the arrow keys; the focused
 * place shows the bead in hand, ready to drop in with OK. The hinted place glows.
 */
@Composable
fun BeadString(
    ui: BeadsUi,
    glowingGap: Int?,
    gapFocus: List<FocusRequester>,
    onPlace: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalWillowColors.current
    val reduceMotion = LocalReduceMotion.current
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val n = ui.chain.size
        val gapWidth = 16.dp
        val openGap = 60.dp
        // Fit the whole string on one line: beads shrink a little on a long string (never below 36dp).
        val bead = min(52.dp, ((maxWidth - gapWidth * (n + 1) - openGap) / n.coerceAtLeast(1))).coerceAtLeast(36.dp)

        // The string itself, behind the beads.
        Canvas(Modifier.matchParentSize()) {
            val y = size.height / 2
            drawLine(colors.accent.copy(alpha = 0.7f), Offset(0f, y), Offset(size.width, y), strokeWidth = 4.dp.toPx())
        }

        val sparkle = remember { Animatable(0f) }
        LaunchedEffect(ui.sparkles) {
            if (ui.sparkles > 0 && !reduceMotion) {
                sparkle.snapTo(0f)
                sparkle.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            for (i in 0..n) {
                key("gap", i) {
                    BeadGap(
                        index = i,
                        ui = ui,
                        glowing = glowingGap == i,
                        closedWidth = gapWidth,
                        openWidth = openGap,
                        beadSize = bead,
                        sparkle = if (ui.sparkleGap == i) sparkle.value else 0f,
                        onClick = { onPlace(i) },
                        modifier = Modifier.focusRequester(gapFocus[i]),
                    )
                }
                if (i < n) key(ui.chain[i].id) { BeadView(ui.chain[i].symbol, bead) }
            }
        }
    }
}

@Composable
private fun BeadGap(
    index: Int,
    ui: BeadsUi,
    glowing: Boolean,
    closedWidth: Dp,
    openWidth: Dp,
    beadSize: Dp,
    sparkle: Float,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val colors = LocalWillowColors.current
    val reduceMotion = LocalReduceMotion.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val width by animateDpAsState(
        if (focused || glowing) openWidth else closedWidth,
        if (reduceMotion) tween(0) else tween(250),
        label = "gapWidth",
    )
    val glow = glowAlphaForHint(glowing)
    val description = describeGap(index, ui)
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier
            .width(width)
            .height(openWidth + 16.dp)
            .semantics { contentDescription = description },
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = colors.surfaceFocused.copy(alpha = 0.3f),
        ),
        border = ClickableSurfaceDefaults.border(
            border = if (glowing) Border(BorderStroke(5.dp, colors.accent.copy(alpha = 0.4f + glow)), shape = CircleShape) else Border.None,
            focusedBorder = Border(BorderStroke(5.dp, colors.focusRing), shape = CircleShape),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.matchParentSize()) {
            if (sparkle in 0.01f..0.99f) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(
                        Brush.radialGradient(listOf(colors.accent.copy(alpha = 0.8f * (1f - sparkle)), Color.Transparent)),
                        radius = size.minDimension * (0.6f + 1.4f * sparkle),
                    )
                }
            }
            // The bead in hand, waiting to be dropped here.
            if (focused) BeadView(ui.hand, min(beadSize, openWidth - 8.dp), Modifier.alpha(0.9f))
        }
    }
}

/** "Between the moon and the star", "At the start, before the leaf" — what TalkBack says on a place. */
private fun describeGap(index: Int, ui: BeadsUi): String {
    val chain = ui.chain
    fun name(i: Int) = "the " + chain[i].symbol.label.lowercase()
    val where = when {
        chain.isEmpty() -> "The string"
        index == 0 -> "At the start, before ${name(0)}"
        index == chain.size -> "At the end, after ${name(chain.lastIndex)}"
        else -> "Between ${name(index - 1)} and ${name(index)}"
    }
    return "$where. Put the ${ui.hand.label.lowercase()} here"
}
