package net.rubinny.willowbrook.ui

import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import kotlinx.coroutines.runBlocking
import net.rubinny.willowbrook.MainActivity
import net.rubinny.willowbrook.WillowbrookApp
import org.junit.Before
import org.junit.Rule

/** Shared helpers that drive the app the way a TV remote does. */
@OptIn(ExperimentalRoborazziApi::class, ExperimentalTestApi::class)
abstract class TvUiTestBase {

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    protected val app: WillowbrookApp get() = rule.activity.application as WillowbrookApp

    @Before fun freshInstall() {
        runBlocking { app.container.deleteAllData() }
        rule.waitForIdle()
    }

    /**
     * Sends a key exactly as a TV remote does (source = D-pad). Like Android, keys go to the top window:
     * the Activity normally, or an open dialog (which Compose renders in its own window).
     */
    protected fun press(keyCode: Int) {
        val roots = rule.onAllNodes(isRoot()).fetchSemanticsNodes()
        if (roots.size > 1) {
            rule.onAllNodes(isRoot())[roots.lastIndex].performKeyInput { pressKey(Key(keyCode)) }
            rule.waitForIdle()
            return
        }
        rule.runOnUiThread {
            val now = SystemClock.uptimeMillis()
            listOf(KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP).forEach { action ->
                rule.activity.dispatchKeyEvent(
                    KeyEvent(now, now, action, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_DPAD),
                )
            }
        }
        rule.waitForIdle()
    }

    protected fun ok() = press(KeyEvent.KEYCODE_DPAD_CENTER)
    protected fun down() = press(KeyEvent.KEYCODE_DPAD_DOWN)
    protected fun right() = press(KeyEvent.KEYCODE_DPAD_RIGHT)
    protected fun back() = press(KeyEvent.KEYCODE_BACK)

    /** Robolectric's clock stands still; this lets timed game events (the spell show) happen. */
    protected fun passTime(millis: Long) {
        var left = millis
        while (left > 0) {
            val step = minOf(200L, left)
            org.robolectric.shadows.ShadowLooper.idleMainLooper(step, java.util.concurrent.TimeUnit.MILLISECONDS)
            rule.waitForIdle()
            left -= step
        }
    }

    protected fun shot(name: String) = captureScreenRoboImage("build/screenshots/$name.png")

    protected fun waitForText(text: String, substring: Boolean = false) {
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty() }
    }

    /** Text of the focused node (merged), for assertions like "focus is on the glasses". */
    protected fun focusedDescription(): String {
        val node = rule.onAllNodes(isFocused()).fetchSemanticsNodes().firstOrNull() ?: return "<nothing focused>"
        val text = node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)?.joinToString(" ")
        val description = node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.ContentDescription)?.joinToString(" ")
        return listOfNotNull(text, description).joinToString(" | ").ifEmpty { node.config.toString() }
    }

    /** Moves focus to an on-screen button (a stand-in for a few arrow presses whose count depends on layout). */
    protected fun focusButton(text: String) {
        rule.onNodeWithText(text).performSemanticsAction(SemanticsActions.RequestFocus)
        rule.waitForIdle()
    }
}
