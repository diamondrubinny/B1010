package net.rubinny.willowbrook.ui.screens.caseplaceholder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.ReadableBlock
import net.rubinny.willowbrook.ui.components.ScreenScaffold
import net.rubinny.willowbrook.ui.components.requestFocusSafely

/**
 * STAGE 1 PLACEHOLDER — replaced in stage 2 by the chapter player (content engine + activities).
 * Kept so navigation, back handling and focus can be tested end-to-end now.
 */
@Composable
fun CasePlaceholderScreen(onBackHome: () -> Unit) {
    val focus = remember { FocusRequester() }
    ScreenScaffold(title = "The Lantern of Willowbrook") {
        ReadableBlock(
            heading = null,
            body = "The autumn fair is tonight, but the magic lantern has gone dark. " +
                "Three little light charms have rolled away somewhere in the village. " +
                "Marmalade the cat is ready to help you look for them.",
        )
        ReadableBlock(heading = null, body = "This chapter is being prepared. Please check back soon.")
        BigButton(text = "Back home", onClick = onBackHome, modifier = Modifier.focusRequester(focus))
    }
    LaunchedEffect(Unit) { focus.requestFocusSafely() }
}
