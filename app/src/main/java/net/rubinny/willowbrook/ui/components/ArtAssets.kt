package net.rubinny.willowbrook.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import net.rubinny.willowbrook.game.stage.SceneFxCatalog
import net.rubinny.willowbrook.ui.stage.LivingScene
import net.rubinny.willowbrook.ui.stage.rememberSceneLayers
import net.rubinny.willowbrook.ui.theme.LocalReduceMotion
import net.rubinny.willowbrook.ui.theme.LocalWillowColors

/**
 * Finds optional AI artwork by naming convention (docs/ASSET_PIPELINE.md).
 * Returns 0 when the art has not been produced yet, so callers fall back to placeholders.
 */
@SuppressLint("DiscouragedApi") // names come from validated content; resources are kept via res/raw/keep.xml
@Composable
fun rememberArtId(name: String?): Int {
    val packageName = LocalContext.current.packageName
    val resources = LocalResources.current
    return remember(name, resources) {
        if (name.isNullOrBlank() || !name.matches(Regex("[a-z][a-z0-9_]*"))) 0
        else resources.getIdentifier(name, "drawable", packageName)
    }
}

/**
 * Full-screen scene under a scrim (WillowColors.sceneScrim). Text never sits directly on it: use [OnScene]. With artwork it is a living scene
 * (ui/stage/LivingScene): layers, warm light and gentle particles, still when "Less movement on screen"
 * is on. Without artwork, a calm gradient placeholder.
 */
@Composable
fun SceneBackground(
    scene: String?,
    modifier: Modifier = Modifier,
    calmCamera: Boolean = false,
    mystery: Boolean = false,
    fixedSeconds: Float? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val dusk by animateFloatAsState(if (mystery) 1f else 0f, tween(1_200), label = "mysteryDusk")
    val colors = LocalWillowColors.current
    val layers = rememberSceneLayers(
        farId = rememberArtId(scene?.let { "scene_$it" }),
        nearId = rememberArtId(scene?.let { "scene_${it}_near" }),
    )
    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        if (layers != null && scene != null) {
            LivingScene(
                far = layers.first,
                near = layers.second,
                fx = SceneFxCatalog.forScene(scene),
                still = LocalReduceMotion.current,
                sceneKey = scene,
                calmCamera = calmCamera,
                fixedSeconds = fixedSeconds,
            )
            Box(Modifier.fillMaxSize().background(colors.background.copy(alpha = colors.sceneScrim)))
            if (dusk > 0f) {
                // A soft vignette: the middle stays as it was, the edges fade toward dusk. Text sits on cards.
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.radialGradient(
                            0.35f to Color.Transparent,
                            1f to colors.background.copy(alpha = 0.6f * dusk),
                        ),
                    ),
                )
            }
        } else {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(colors.background, colors.surface.copy(alpha = 0.55f), colors.background)),
                ),
            )
        }
        content()
    }
}

/** An object picture: artwork "obj_<id>" when available, otherwise the placeholder emoji. Decorative (labelled by parent). */
@Composable
fun ItemPicture(itemId: String, emoji: String, size: Dp) = ArtPicture("obj_$itemId", emoji, size)

/** Any artwork by resource name, falling back to an emoji. Decorative. */
@Composable
fun ArtPicture(artName: String, emoji: String, size: Dp) {
    val artId = rememberArtId(artName)
    if (artId != 0) {
        Image(painterResource(artId), contentDescription = null, modifier = Modifier.size(size))
    } else {
        EmojiGlyph(emoji, (size.value * 0.75f).sp)
    }
}

/** Character portrait: AI art "char_<key>" or emoji placeholder. */
@Composable
fun Portrait(key: String?, emoji: String?, size: Dp = 160.dp) {
    val artId = rememberArtId(key?.let { "char_$it" })
    when {
        artId != 0 -> Image(painterResource(artId), contentDescription = null, modifier = Modifier.size(size))
        emoji != null -> Box(Modifier.size(size), contentAlignment = Alignment.Center) { EmojiGlyph(emoji, (size.value * 0.6f).sp) }
    }
}

@Composable
private fun EmojiGlyph(emoji: String, fontSize: TextUnit) {
    // A picture, not text: sized by the layout, so it is not multiplied by the caregiver text scale.
    Text(
        text = emoji,
        fontSize = fontSize,
        color = Color.Unspecified,
        modifier = Modifier.clearAndSetSemantics { },
    )
}

/** A readable patch for text that would otherwise sit directly on the living scene. */
@Composable
fun OnScene(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = LocalWillowColors.current
    Box(
        modifier = modifier
            .background(colors.background.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        content = content,
    )
}
