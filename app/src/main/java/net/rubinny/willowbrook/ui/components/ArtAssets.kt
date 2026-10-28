package net.rubinny.willowbrook.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
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

/** Full-screen scene: AI painting under a dark scrim (keeps text contrast), or a calm gradient placeholder. */
@Composable
fun SceneBackground(scene: String?, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = LocalWillowColors.current
    val artId = rememberArtId(scene?.let { "scene_$it" })
    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        if (artId != 0) {
            Image(
                painter = painterResource(artId),
                contentDescription = null, // decorative; the scene is described in text
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(Modifier.fillMaxSize().background(colors.background.copy(alpha = 0.72f)))
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

/** An object picture: AI art "obj_<id>" when available, otherwise the placeholder emoji. Decorative (labelled by parent). */
@Composable
fun ItemPicture(itemId: String, emoji: String, size: Dp) {
    val artId = rememberArtId("obj_$itemId")
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
