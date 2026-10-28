package net.rubinny.willowbrook.ui

import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import kotlinx.coroutines.runBlocking
import net.rubinny.willowbrook.MainActivity
import net.rubinny.willowbrook.WillowbrookApp
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * End-to-end journey driven ONLY by remote-control keys (D-pad + OK + Back), as on a real TV.
 * Also records screenshots at 1920x1080 into app/build/screenshots for visual review.
 */
@OptIn(ExperimentalRoborazziApi::class, ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-television-xhdpi")
class TvJourneyTest {

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun freshInstall() {
        val app = rule.activity.application as WillowbrookApp
        runBlocking { app.container.deleteAllData() }
        rule.waitForIdle()
    }

    /**
     * Sends a key exactly as a TV remote does (source = D-pad). Like Android, keys go to the top window:
     * the Activity normally, or an open dialog (which Compose renders in its own window).
     */
    private fun press(keyCode: Int) {
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

    private fun ok() = press(KeyEvent.KEYCODE_DPAD_CENTER)
    private fun down() = press(KeyEvent.KEYCODE_DPAD_DOWN)
    private fun right() = press(KeyEvent.KEYCODE_DPAD_RIGHT)
    private fun back() = press(KeyEvent.KEYCODE_BACK)

    private fun shot(name: String) = captureScreenRoboImage("build/screenshots/$name.png")

    private fun waitForText(text: String, substring: Boolean = false) {
        rule.waitUntil(10_000) {
            rule.onAllNodes(androidx.compose.ui.test.hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun firstRun_createPlayer_home_backAsksBeforeExit() {
        // 1. First run: one obvious action, already focused.
        waitForText("Welcome to Willowbrook")
        rule.onNodeWithText("Set up a player").assertIsFocused()
        shot("01_welcome")

        // 2. Create a player (caregiver types a name with the TV keyboard).
        ok()
        waitForText("New player")
        rule.onNode(hasSetTextAction()).performTextInput("Ruth")
        rule.waitForIdle()
        shot("02_new_player")
        rule.onNodeWithText("Save player").let { node ->
            // From the name field, Down reaches "Save player" (primary action, left).
            down()
            node.assertIsFocused()
        }
        ok()

        // 3. Player picker: the new player is focused, OK starts.
        waitForText("Who is playing today?")
        rule.onNodeWithText("Ruth").assertIsFocused()
        shot("03_who_is_playing")
        ok()

        // 4. Home: "Continue the case" is focused first.
        waitForText("Hello, Ruth")
        rule.onNodeWithText("Continue the case").assertIsFocused()
        shot("04_home")

        // 5. Continue -> chapter placeholder -> Back returns home.
        ok()
        waitForText("The Lantern of Willowbrook")
        shot("05_case_placeholder")
        back()
        waitForText("Hello, Ruth")

        // 6. Back on Home does NOT exit: it asks, with "Keep playing" pre-selected.
        back()
        waitForText("Would you like to rest now?")
        shot("06_exit_confirm")
        rule.onNodeWithText("Keep playing").assertIsFocused()
        ok() // keep playing
        waitForText("Hello, Ruth")
        assertTrue("activity must still be running", !rule.activity.isFinishing)
    }

    @Test fun caregiverPin_createThenSettings_largeText_highContrast() {
        waitForText("Welcome to Willowbrook")
        down() // -> Caregiver settings
        rule.onNodeWithText("Caregiver settings").assertIsFocused()
        ok()

        // Create PIN with the remote's number keys, then confirm.
        waitForText("Choose a 4-digit caregiver PIN")
        shot("07_pin_create")
        listOf(KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_7).forEach(::press)
        waitForText("Type the same PIN again")
        listOf(KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_7).forEach(::press)

        // Settings open with "Done" focused.
        waitForText("Reading and seeing")
        rule.onNodeWithText("Done").assertIsFocused()
        shot("08_settings")

        // Text size: Down to the row, Right to "More", OK twice -> 140%.
        down(); right()
        ok(); ok()
        waitForText("140%")
        // High contrast toggle is the next row.
        down()
        ok()
        val repo = (rule.activity.application as WillowbrookApp).container.settingsRepository
        rule.waitUntil(10_000) { runBlocking { repo.current().highContrast } }
        rule.waitForIdle()
        shot("09_settings_large_high_contrast")

        // Done -> back to the welcome screen in the new look.
        back()
        waitForText("Welcome to Willowbrook")
        shot("10_welcome_large_high_contrast")
    }
}
