package net.rubinny.willowbrook.ui.components

import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.delay

/**
 * Requests focus once the node is attached. On slow TV boxes the first frame may not be laid out yet,
 * so we retry briefly instead of crashing with "FocusRequester is not initialized".
 */
suspend fun FocusRequester.requestFocusSafely() {
    repeat(5) {
        try {
            requestFocus()
            return
        } catch (_: IllegalStateException) {
            delay(50)
        }
    }
}
