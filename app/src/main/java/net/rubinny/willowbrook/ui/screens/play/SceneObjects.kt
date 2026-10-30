package net.rubinny.willowbrook.ui.screens.play

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import net.rubinny.willowbrook.game.model.Item
import net.rubinny.willowbrook.game.stage.SceneSpots
import net.rubinny.willowbrook.ui.components.ItemPicture
import net.rubinny.willowbrook.ui.theme.LocalReduceMotion
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

/** Size of one object placed on the scene (picture + label). */
private val OBJECT_WIDTH = 176.dp
private val OBJECT_HEIGHT = 146.dp

/** The play area must hold two rows of objects without overlap; below this the tile grid is used instead. */
val SCENE_OBJECTS_MIN_HEIGHT: Dp = OBJECT_HEIGHT * SceneSpots.MIN_HEIGHT_FACTOR

/**
 * Hidden objects lying on the scene itself (game/stage/SceneSpots), instead of a grid of cards.
 * Focus moves with the arrow keys to the nearest object in that direction (built-in geometric focus).
 */
@Composable
fun SceneObjects(
    scene: String?,
    cells: List<Item>,
    found: Set<String>,
    glowingIndex: Int?,
    focus: List<FocusRequester>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spots = remember(scene) { SceneSpots.forScene(scene) }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = min(OBJECT_WIDTH, maxWidth / 5)
        // Two rows must fit whatever the header leaves: shrink the picture (never the text) if space is short.
        val h = min(OBJECT_HEIGHT, maxHeight / 2)
        val picture = (h - 86.dp).coerceIn(40.dp, 60.dp)
        cells.take(SceneSpots.MAX).forEachIndexed { index, item ->
            val spot = spots[index]
            val x = spot.left(maxWidth.value, w.value).dp
            val y = spot.top(maxHeight.value, h.value).dp
            SceneObject(
                item = item,
                found = item.id in found,
                glowing = glowingIndex == index,
                onClick = { onSelect(index) },
                pictureSize = picture,
                modifier = Modifier
                    .offset(x, y)
                    .size(w, h)
                    .focusRequester(focus[index]),
            )
        }
    }
}

/** One object on the scene: picture with a soft shadow, a readable label, and a magnifying glass when focused. */
@Composable
fun SceneObject(
    item: Item,
    found: Boolean,
    glowing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pictureSize: Dp = 60.dp,
) {
    val colors = LocalWillowColors.current
    val reduceMotion = LocalReduceMotion.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(28.dp)
    val glow = glowAlphaForHint(glowing)

    // A soft ring that widens once when the object is found (none with "Less movement on screen").
    val burst = remember { Animatable(if (found) 1f else 0f) }
    LaunchedEffect(found) {
        if (found && !reduceMotion && burst.value == 0f) burst.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier.semantics { contentDescription = item.label + if (found) ", found" else "" },
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = colors.surfaceFocused.copy(alpha = 0.22f),
            pressedContainerColor = colors.surfaceFocused.copy(alpha = 0.35f),
        ),
        border = ClickableSurfaceDefaults.border(
            border = if (glowing) Border(BorderStroke(6.dp, colors.accent.copy(alpha = 0.4f + glow)), shape = shape) else Border.None,
            focusedBorder = Border(BorderStroke(6.dp, colors.focusRing), shape = shape),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (reduceMotion) 1f else 1.06f),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            ) {
                Box(
                    modifier = Modifier
                        .size(pictureSize + 4.dp)
                        .drawBehind {
                            // Contact shadow so the object looks like it rests on the scene.
                            drawOval(
                                Color.Black.copy(alpha = 0.35f),
                                topLeft = Offset(size.width * 0.12f, size.height * 0.82f),
                                size = Size(size.width * 0.76f, size.height * 0.2f),
                            )
                            if (burst.value in 0.01f..0.99f) {
                                drawCircle(
                                    colors.accent.copy(alpha = 1f - burst.value),
                                    radius = size.minDimension * (0.45f + 0.5f * burst.value),
                                    style = Stroke(width = 6.dp.toPx()),
                                )
                            }
                        }
                        .alpha(if (found) 0.5f else 1f),
                    contentAlignment = Alignment.Center,
                ) {
                    ItemPicture(item.id, item.emoji, size = pictureSize)
                }
                Text(
                    text = item.label,
                    style = WillowType.label,
                    color = if (focused) colors.textOnFocus else colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier
                        .background(if (focused) colors.surfaceFocused else colors.background.copy(alpha = 0.88f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            if (focused) Magnifier(Modifier.align(Alignment.TopEnd).padding(2.dp).size(36.dp))
            if (found) FoundBadge(Modifier.align(Alignment.TopStart).padding(6.dp).size(34.dp))
        }
    }
}

/** A small golden magnifying glass: "you are looking here". Decorative. */
@Composable
private fun Magnifier(modifier: Modifier) {
    val colors = LocalWillowColors.current
    Canvas(modifier.clearAndSetSemantics { }) {
        val r = size.minDimension * 0.3f
        val c = Offset(size.width * 0.42f, size.height * 0.42f)
        drawCircle(colors.background.copy(alpha = 0.6f), r, c)
        drawCircle(colors.accent, r, c, style = Stroke(width = size.minDimension * 0.12f))
        drawLine(
            colors.accent,
            start = Offset(c.x + r * 0.75f, c.y + r * 0.75f),
            end = Offset(size.width * 0.92f, size.height * 0.92f),
            strokeWidth = size.minDimension * 0.16f,
            cap = StrokeCap.Round,
        )
    }
}

/** A tick in a circle: "found". Shape plus colour, so it does not rely on colour alone. Decorative (label says "found"). */
@Composable
private fun FoundBadge(modifier: Modifier) {
    val colors = LocalWillowColors.current
    Canvas(modifier.clearAndSetSemantics { }) {
        val r = size.minDimension / 2
        drawCircle(colors.success, r)
        drawCircle(colors.background, r, style = Stroke(width = r * 0.14f))
        drawLine(colors.background, Offset(r * 0.5f, r * 1.05f), Offset(r * 0.85f, r * 1.4f), strokeWidth = r * 0.22f, cap = StrokeCap.Round)
        drawLine(colors.background, Offset(r * 0.85f, r * 1.4f), Offset(r * 1.5f, r * 0.65f), strokeWidth = r * 0.22f, cap = StrokeCap.Round)
    }
}
