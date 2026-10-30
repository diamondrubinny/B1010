package net.rubinny.willowbrook.ui.screens.settings

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import net.rubinny.willowbrook.BuildConfig
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.ReadableBlock
import net.rubinny.willowbrook.ui.components.ScreenScaffold
import net.rubinny.willowbrook.ui.components.requestFocusSafely

/**
 * In-app summary of the privacy notice and accessibility statement.
 * DRAFT TEXT — must be reviewed by a lawyer / accessibility consultant before public release
 * (see docs/PRIVACY_POLICY.md and docs/ACCESSIBILITY_STATEMENT.md).
 */
@Composable
fun AboutScreen(onClose: () -> Unit) {
    val focus = remember { FocusRequester() }
    val width = Modifier.widthIn(max = 860.dp)

    ScreenScaffold(title = "About Willowbrook Mysteries", subtitle = "Version ${BuildConfig.VERSION_NAME}") {
        BigButton(text = "Back", onClick = onClose, minWidth = 240.dp, modifier = Modifier.focusRequester(focus))
        ReadableBlock(
            heading = "What this is",
            body = "A calm mystery game designed with people living with dementia in mind. " +
                "It is an activity for enjoyment and conversation. It is not a medical device " +
                "and does not diagnose, treat or prevent any condition.",
            modifier = width,
        )
        ReadableBlock(
            heading = "Your privacy",
            body = "The game works without the internet and has no permission to use it. " +
                "Player names, progress and settings stay on this TV only. " +
                "Nothing is sent to us or to anyone else, and there are no adverts, trackers or cookies.",
            modifier = width,
        )
        ReadableBlock(
            heading = "Deleting information",
            body = "In Settings (hold the Menu button on the remote for 3 seconds, or OK for 5 seconds) you can remove a single player or delete all data. " +
                "Uninstalling the game also removes everything.",
            modifier = width,
        )
        ReadableBlock(
            heading = "Accessibility",
            body = "We aim for WCAG 2.1 level AA and stronger contrast for body text. " +
                "Everything works with the arrow keys and OK. Text size, contrast, motion, " +
                "speaking speed and hints can be adjusted. The game works with TalkBack. " +
                "Known limitations are listed in our accessibility statement.",
            modifier = width,
        )
        ReadableBlock(
            heading = "Contact",
            body = "To report an accessibility problem or ask about privacy, " +
                "please contact the app provider (contact details to be added before release).",
            modifier = width,
        )
        ReadableBlock(
            heading = "Credits",
            body = "Voices: computer-generated (synthetic) voices made with Piper from public-domain recordings " +
                "(LibriVox and LJ Speech). They do not imitate any real person. " +
                "Pictures, music and sounds: original work made for this game. " +
                "Font: Atkinson Hyperlegible by the Braille Institute, SIL Open Font License 1.1.",
            modifier = width,
        )
    }
    LaunchedEffect(Unit) { focus.requestFocusSafely() }
}
