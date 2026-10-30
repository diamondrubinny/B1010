package net.rubinny.willowbrook.ui.components

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent

/**
 * The hidden way into Settings (there is no Settings button for players): hold the remote's Menu key
 * for [MENU_HOLD_MS], or OK for [OK_HOLD_MS]. A short press does what it always does; once a hold has
 * opened the entry, the rest of that key press (including its release) is swallowed, so the button that
 * had focus is not pressed as well.
 */
class LongHoldDetector {
    private var key = -1
    private var fired = false

    /** Returns true when the event must be consumed. [onHold] is called once per qualifying hold. */
    fun onKey(keyCode: Int, action: Int, repeatCount: Int, downTime: Long, eventTime: Long, onHold: () -> Unit): Boolean {
        val threshold = thresholdFor(keyCode) ?: return false
        return when (action) {
            KeyEvent.ACTION_DOWN -> {
                if (repeatCount == 0 || keyCode != key) {
                    key = keyCode
                    fired = false
                }
                if (!fired && eventTime - downTime >= threshold) {
                    fired = true
                    onHold()
                }
                fired
            }
            KeyEvent.ACTION_UP -> {
                val swallow = fired && keyCode == key
                key = -1
                fired = false
                swallow
            }
            else -> false
        }
    }

    companion object {
        const val MENU_HOLD_MS = 3_000L
        const val OK_HOLD_MS = 5_000L

        fun thresholdFor(keyCode: Int): Long? = when (keyCode) {
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_SETTINGS -> MENU_HOLD_MS
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> OK_HOLD_MS
            else -> null
        }
    }
}

/** Listens (on a screen's root) for the long hold that opens the hidden Settings entry. */
@Composable
fun Modifier.secretSettingsHold(onHold: () -> Unit): Modifier {
    val detector = remember { LongHoldDetector() }
    return onPreviewKeyEvent { e ->
        val n = e.nativeKeyEvent
        detector.onKey(n.keyCode, n.action, n.repeatCount, n.downTime, n.eventTime, onHold)
    }
}

/**
 * Asks before opening Settings, with "Back to the game" pre-selected: a player who held a key by
 * accident simply presses OK and carries on.
 */
@Composable
fun SecretSettingsDialog(onOpen: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        title = "Settings",
        message = "These settings are for the person who set up the game.",
        confirmText = "Open settings",
        dismissText = "Back to the game",
        onConfirm = onOpen,
        onDismiss = onDismiss,
    )
}
