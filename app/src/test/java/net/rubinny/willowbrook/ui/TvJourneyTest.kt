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
import androidx.compose.ui.test.onAllNodesWithText
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
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-television-xhdpi")
class TvJourneyTest : TvUiTestBase() {

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

        // 4. Home: the main action ("Start the case" for a new player) is focused first.
        waitForText("Hello, Ruth")
        rule.onNodeWithText("Start the case").assertIsFocused()
        shot("04_home")

        // 5. Start -> chapter intro. Back asks first; "Go home" (one step right) returns home.
        ok()
        waitForText("Getting started")
        shot("05_chapter_intro")
        back()
        waitForText("Would you like a break?")
        right()
        ok()
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

    /** No settings button: a short press does nothing special; holding Menu asks first, with "Back to the game" ready. */
    @Test fun hiddenSettings_holdMenu_thenLargeText_highContrast() {
        waitForText("Welcome to Willowbrook")
        assertTrue(rule.onAllNodesWithText("Caregiver settings").fetchSemanticsNodes().isEmpty())
        assertTrue(rule.onAllNodesWithText("Settings").fetchSemanticsNodes().isEmpty())

        // A short press of Menu does nothing.
        press(KeyEvent.KEYCODE_MENU)
        assertTrue(rule.onAllNodesWithText("Open settings").fetchSemanticsNodes().isEmpty())

        // Held by accident: the safe choice is ready, OK returns to the game.
        hold(KeyEvent.KEYCODE_MENU, 3_200)
        waitForText("These settings are for the person who set up the game.")
        rule.onNodeWithText("Back to the game").assertIsFocused()
        shot("07_secret_settings_ask")
        ok()
        waitForText("Set up a player")
        rule.onNodeWithText("Set up a player").assertIsFocused()

        // Held on purpose: Right to "Open settings", OK.
        hold(KeyEvent.KEYCODE_MENU, 3_200)
        waitForText("Open settings")
        right()
        ok()

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
        assertTrue(focusedDescription(), focusedDescription().startsWith("Extra-strong contrast"))
        ok()
        val repo = (rule.activity.application as WillowbrookApp).container.settingsRepository
        // The save resumes on the (simulated) main thread, so let it run while waiting.
        rule.waitUntil(10_000) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            runBlocking { repo.current().highContrast }
        }
        rule.waitForIdle()
        shot("09_settings_large_high_contrast")

        // Done -> back to the welcome screen in the new look.
        back()
        waitForText("Welcome to Willowbrook")
        shot("10_welcome_large_high_contrast")
    }
}
