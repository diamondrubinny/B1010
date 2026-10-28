package net.rubinny.willowbrook.ui

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import net.rubinny.willowbrook.data.profiles.Avatar
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Plays chapters 0 and 1 with remote keys only, using the guided-help path that every player can rely on:
 * "Help me" three times moves focus onto the answer, then OK. Records screenshots for review.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-television-xhdpi")
class PlayJourneyTest : TvUiTestBase() {

    /** Help me ×3 → focus jumps to the answer (GUIDE). */
    private fun askForFullHelp() {
        focusButton("Help me")
        ok(); ok(); ok()
    }

    @Test fun playChapters0and1_withGuidedHelp() {
        runBlocking { app.container.profileRepository.add("Ruth", Avatar.SAGE) }
        waitForText("Who is playing today?")
        ok()
        waitForText("Start the case")
        rule.onNodeWithText("Start the case").assertIsFocused()
        ok()

        // Chapter 0: intro → 2 narrations → find glasses → narration → reward
        waitForText("Getting started")
        shot("11_chapter_intro")
        ok()
        waitForText("Welcome to Willowbrook, a quiet village", substring = true)
        rule.onNodeWithText("Continue").assertIsFocused()
        ok()
        waitForText("Hello! I'm Marmalade", substring = true)
        shot("12_narration_marmalade")
        ok()

        waitForText("Please find")
        shot("13_hidden_object_start")
        focusButton("Help me")
        ok(); ok() // HIGHLIGHT: the answer glows
        waitForText("I think it's the one that is glowing.")
        shot("14_hidden_object_glow")
        ok() // GUIDE: focus moves to the answer
        waitForText("Here it is! Press OK.")
        assertTrue(focusedDescription(), focusedDescription().contains("Reading glasses"))
        ok()
        waitForText("Wonderful! You found everything.")
        rule.onNodeWithText("Continue").assertIsFocused()
        ok()
        waitForText("Well done! Now, some news.", substring = true)
        ok()
        waitForText("You are ready to begin.", substring = true)
        ok()
        waitForText("Well done, detective!")
        rule.onNodeWithText("Next: The Bakery").assertIsFocused()
        ok()

        // Chapter 1: intro → 2 narrations → find 3 objects → narration → memory → narration → reward
        waitForText("Chapter 1")
        ok()
        waitForText("The bakery smells", substring = true)
        ok()
        waitForText("Good morning, detective!", substring = true)
        ok()
        waitForText("Please find")
        repeat(3) {
            askForFullHelp()
            ok()
        }
        waitForText("Wonderful! You found everything.")
        shot("15_hidden_object_complete")
        ok() // Continue (focused)
        waitForText("My recipe cards are all mixed up", substring = true)
        ok()

        waitForText("Find two cards that are the same.")
        shot("16_memory_start")
        // "What are we doing?" works mid-activity and returns focus safely.
        focusButton("What are we doing?")
        ok()
        waitForText("Right now: Help Mrs. Bramble", substring = true)
        shot("17_what_are_we_doing")
        ok()
        repeat(2) { pair ->
            askForFullHelp()
            ok() // flip first card; focus moves to its twin
            if (pair == 0) shot("18_memory_first_card")
            ok() // flip the twin
        }
        waitForText("Every card has its twin. Well done!")
        ok()
        waitForText("Something is twinkling", substring = true)
        ok()
        waitForText("You found the first light charm!", substring = true)
        shot("19_reward_charm")
        ok()
        waitForText("Well done, detective!")
        waitForText("More chapters are on the way.")
        shot("20_chapter_complete_last")
        rule.onNodeWithText("Back home").assertIsFocused()
        ok()

        // Home reflects progress; case files list both chapters to play again.
        waitForText("Visit the case again")
        shot("21_home_after")
        focusButton("Case files")
        ok()
        waitForText("✓ Chapter 1: The Bakery")
        shot("22_case_files")
    }

    @Test fun backDuringActivity_asksAndKeepsPlaying() {
        runBlocking { app.container.profileRepository.add("Ruth", Avatar.SAGE) }
        waitForText("Who is playing today?")
        ok()
        waitForText("Start the case")
        ok()
        waitForText("Getting started")
        ok(); ok(); ok()
        waitForText("Please find")
        back()
        waitForText("Would you like a break?")
        rule.onNodeWithText("Keep playing").assertIsFocused()
        shot("23_break_dialog")
        ok()
        waitForText("Please find")
    }
}
