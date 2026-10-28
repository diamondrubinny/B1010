package net.rubinny.willowbrook.ui.screens.play

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import net.rubinny.willowbrook.R
import net.rubinny.willowbrook.game.model.Item
import net.rubinny.willowbrook.ui.components.ItemPicture
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalReduceMotion
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

/**
 * Soft, slow glow for hints (one pulse every 2.4 s — far below the 3 flashes/second seizure threshold).
 * With "Less movement" it is a steady glow.
 */
@Composable
private fun glowAlpha(active: Boolean): Float {
    if (!active) return 0f
    if (LocalReduceMotion.current) return 0.45f
    val transition = rememberInfiniteTransition(label = "hintGlow")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1_200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hintGlowAlpha",
    )
    return alpha
}

/** Shared look for a selectable picture tile. Found/matched tiles stay focusable so focus is never lost. */
@Composable
private fun Tile(
    onClick: () -> Unit,
    glowing: Boolean,
    done: Boolean,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = LocalWillowColors.current
    val shape = RoundedCornerShape(Dimens.cornerRadius)
    val glow = glowAlpha(glowing)
    // The glow lives in the border only, so text contrast on the tile never drops.
    val base = if (done && !glowing) colors.surface.copy(alpha = 0.55f) else colors.surface
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = accessibilityLabel },
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = base,
            contentColor = colors.textPrimary,
            focusedContainerColor = colors.surfaceFocused,
            focusedContentColor = colors.textOnFocus,
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                BorderStroke(
                    if (glowing) Dimens.focusBorder else 2.dp,
                    if (glowing) colors.accent.copy(alpha = 0.4f + glow) else colors.textSecondary.copy(alpha = 0.4f),
                ),
                shape = shape,
            ),
            focusedBorder = Border(BorderStroke(Dimens.focusBorder, colors.focusRing), shape = shape),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (LocalReduceMotion.current) 1f else 1.05f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 124.dp)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) { content() }
    }
}

@Composable
fun HiddenObjectCell(item: Item, found: Boolean, glowing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Tile(
        onClick = onClick,
        glowing = glowing,
        done = found,
        accessibilityLabel = item.label + if (found) ", found" else "",
        modifier = modifier,
    ) {
        ItemPicture(item.id, item.emoji, size = 56.dp)
        Text(text = if (found) "✓ ${item.label}" else item.label, style = WillowType.label, textAlign = TextAlign.Center)
    }
}

@Composable
fun MemoryCard(position: Int, card: MemoryCardUi, glowing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val showFace = card.faceUp || card.matched
    Tile(
        onClick = onClick,
        glowing = glowing,
        done = card.matched,
        accessibilityLabel = when {
            card.matched -> "Card $position, ${card.item.label}, paired"
            card.faceUp -> "Card $position, ${card.item.label}"
            else -> "Card $position, face down"
        },
        modifier = modifier,
    ) {
        if (showFace) {
            ItemPicture(card.item.id, card.item.emoji, size = 56.dp)
            Text(text = if (card.matched) "✓ ${card.item.label}" else card.item.label, style = WillowType.label, textAlign = TextAlign.Center)
        } else {
            // Card back: the lantern motif, same for every card.
            Icon(painter = painterResource(R.drawable.ic_lantern), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(72.dp))
        }
    }
}
