package net.rubinny.willowbrook.ui

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import androidx.compose.ui.test.hasContentDescription
import net.rubinny.willowbrook.data.profiles.Avatar
import net.rubinny.willowbrook.data.profiles.ProfileRepository
import net.rubinny.willowbrook.data.progress.CaseProgress
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

        // Chapter 0: intro → 3 narrations → find glasses → narration → reward
        waitForText("Getting started")
        shot("11_chapter_intro")
        ok()
        waitForText("Welcome to Willowbrook, a quiet village", substring = true)
        rule.onNodeWithText("Continue").assertIsFocused()
        ok()
        // Dr. Rubinny, the guide, introduces himself.
        waitForText("I am Dr. Rubinny, and I will be your guide", substring = true)
        waitForText("Dr. Rubinny")
        shot("11b_guide_rubinny")
        ok()
        waitForText("Hello! I'm Marmalade", substring = true)
        shot("12_narration_marmalade")
        ok()

        waitForText("Found")
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
        waitForText("Our first stop is the bakery.", substring = true)
        ok()
        waitForText("Good morning, detective!", substring = true)
        ok()
        waitForText("Found")
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
        waitForText("a little question before we look", substring = true)
        ok()

        // New in stage 3: a memory question about something said at the start of the chapter.
        waitForText("Memory question")
        shot("19a_recall_question")
        askForFullHelp()
        ok()
        waitForText("What a good memory", substring = true)
        ok() // Continue (focused)
        waitForText("Something is twinkling", substring = true)
        ok()
        waitForText("You found the first light charm!", substring = true)
        shot("19_reward_charm")
        ok()
        waitForText("Well done, detective!")
        rule.onNodeWithText("Next: The Garden").assertIsFocused()
        shot("20_chapter_complete")
        right() // "Rest now"
        ok()

        // Home reflects progress; case files list both chapters to play again.
        waitForText("Continue the case")
        waitForText("The Garden")
        shot("21_home_after")
        focusButton("Case files")
        ok()
        waitForText("✓ Chapter 1: The Bakery")
        shot("22_case_files")
    }

    /** Chapter 2: hidden object, sorting, the spell (watch then repeat) and a riddle — remote only. */
    @Test fun playChapter2_sortingSpellRiddle() {
        runBlocking {
            val profile = (app.container.profileRepository.add("Ruth", Avatar.SAGE) as ProfileRepository.AddResult.Added).profile
            app.container.progressRepository.save(
                profile.id,
                CaseProgress("lantern", chapterIndex = 2, completedChapters = listOf("ch0_cottage", "ch1_bakery")),
            )
        }
        waitForText("Who is playing today?")
        ok()
        waitForText("Continue the case")
        ok()
        waitForText("Welcome back!")
        ok()
        waitForText("Chapter 2")
        shot("30_chapter2_intro")
        ok()
        waitForText("Mr. Hollis's garden is bright", substring = true)
        ok()
        waitForText("Here we are in the garden.", substring = true)
        ok()
        waitForText("Hello, detective!", substring = true)
        ok()
        waitForText("Found")
        repeat(3) { askForFullHelp(); ok() }
        waitForText("Wonderful! You found everything.")
        ok()
        waitForText("my basket is all muddled", substring = true)
        ok()

        // Sorting: one item at a time.
        waitForText("Where does it belong?")
        shot("31_sorting")
        repeat(4) { round ->
            focusButton("Help me")
            ok(); ok()
            if (round == 0) shot("32_sorting_glow")
            ok()
            ok()
        }
        waitForText("All sorted. Thank you!")
        shot("33_sorting_done")
        ok()
        waitForText("I know a kind spell", substring = true)
        ok()

        // Spell: the magic is shown (each symbol lights up with its own note), then the player repeats it.
        waitForText("A kind spell")
        passTime(3_100)
        rule.waitUntil(5_000) {
            rule.onAllNodes(hasContentDescription("shining now", substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
        shot("34_spell_showing")
        passTime(4_000)
        waitForText("Now you try. Press the symbols in the same order.")
        shot("35_spell_your_turn")
        repeat(2) { askForFullHelp(); ok() }
        waitForText("The spell worked! Well done.")
        shot("36_spell_done")
        ok()
        waitForText("The flowers are opening!", substring = true)
        ok()
        waitForText("here is a riddle", substring = true)
        ok()

        // Riddle with pictures.
        waitForText("A riddle")
        shot("37_riddle")
        askForFullHelp()
        ok()
        waitForText("Well solved!", substring = true)
        ok()
        waitForText("You found the second light charm!", substring = true)
        ok()
        waitForText("Well done, detective!")
        rule.onNodeWithText("Next: The Library").assertIsFocused()
    }

    /** Chapters 3–4 to the end of the case, then "Let's chat" and the caregiver summary. */
    @Test fun playChapters3and4_chatAndSummary() {
        runBlocking {
            val profile = (app.container.profileRepository.add("Ruth", Avatar.SAGE) as ProfileRepository.AddResult.Added).profile
            app.container.progressRepository.save(
                profile.id,
                CaseProgress("lantern", chapterIndex = 3, completedChapters = listOf("ch0_cottage", "ch1_bakery", "ch2_garden")),
            )
        }
        waitForText("Who is playing today?")
        ok()
        waitForText("Continue the case")
        ok()
        waitForText("Welcome back!")
        ok()
        waitForText("Chapter 3")
        ok()

        // Chapter 3: the library.
        waitForText("The village library is quiet", substring = true)
        ok()
        waitForText("our last stop before the fair", substring = true)
        ok()
        waitForText("a lovely book about the sea", substring = true)
        ok()
        waitForText("Found")
        shot("40_library_hidden")
        repeat(3) { askForFullHelp(); ok() }
        waitForText("Wonderful! You found everything.")
        ok()
        waitForText("People leave all sorts of things here", substring = true)
        ok()
        waitForText("Where does it belong?")
        shot("41_library_sorting")
        repeat(4) { askForFullHelp(); ok() }
        waitForText("All sorted. Thank you!")
        ok()
        waitForText("Something glints", substring = true)
        ok()
        waitForText("Pairs found")
        repeat(2) { askForFullHelp(); ok(); ok() }
        waitForText("Every card has its twin. Well done!")
        ok()
        waitForText("a little question for a good detective", substring = true)
        ok()
        waitForText("What was Miss Penrose's book about?")
        askForFullHelp()
        ok()
        waitForText("What a good memory", substring = true)
        ok()
        waitForText("third light charm, shining", substring = true)
        ok()
        waitForText("You found the third light charm!", substring = true)
        ok()

        // Chapter complete: "Let's chat" conversation starters.
        waitForText("Well done, detective!")
        focusButton("💬 Let's chat")
        ok()
        waitForText("What kind of books or stories do you enjoy?")
        rule.onNodeWithText("Another question").assertIsFocused()
        shot("42_lets_chat")
        ok()
        waitForText("Did you have a favourite place to sit and read?")
        right()
        ok() // Done
        waitForText("Next: The Lantern Lights Up")
        focusButton("Next: The Lantern Lights Up")
        ok()

        // Chapter 4: the finale.
        waitForText("Chapter 4")
        shot("43_finale_intro")
        ok()
        waitForText("The village square is ready", substring = true)
        ok()
        waitForText("let's thank the friends", substring = true)
        ok()
        waitForText("Who was baking a pear tart?")
        shot("44_who_question")
        askForFullHelp()
        ok()
        waitForText("What a good memory", substring = true)
        ok()
        waitForText("Who was raking leaves in the garden?")
        askForFullHelp()
        ok()
        waitForText("What a good memory", substring = true)
        ok()
        // Lantern Beads: put three matching beads together. Help ×3 moves focus to the best place; OK drops it.
        waitForText("all muddled on their string", substring = true)
        ok()
        playBeadsWithHelp(shotName = "44a_beads")
        waitForText("the kindest spell of all", substring = true)
        ok()
        waitForText("A kind spell")
        passTime(7_500)
        waitForText("Now you try. Press the symbols in the same order.")
        repeat(2) { askForFullHelp(); ok() }
        waitForText("The spell worked! Well done.")
        ok()
        waitForText("the great lantern lights up", substring = true)
        ok()
        waitForText("I am very proud of you.", substring = true)
        shot("44b_guide_proud")
        ok()
        waitForText("Willowbrook is lucky to have you", substring = true)
        ok()
        waitForText("You solved the mystery of the lantern!", substring = true)
        ok()
        waitForText("You solved the whole case!")
        shot("45_case_solved")
        rule.onNodeWithText("Back home").assertIsFocused()
        ok()

        // Home, then the progress summary in the hidden settings: holding OK (5 s) asks first and does
        // not press the button that has focus.
        waitForText("Visit the case again")
        hold(android.view.KeyEvent.KEYCODE_DPAD_CENTER, 5_200)
        waitForText("These settings are for the person who set up the game.")
        right()
        ok() // Open settings
        waitForText("Reading and seeing")
        focusButton("Progress summary")
        ok()
        waitForText("Chapters finished: 5 of 5", substring = true)
        waitForText("not a medical assessment", substring = true)
        shot("46_progress_summary")
    }

    /** Plays a beads round with remote keys only, the guided way, until every bead shines. */
    private fun playBeadsWithHelp(shotName: String) {
        waitForText("Beads left")
        waitForText("Your bead")
        shot(shotName)
        var moves = 0
        while (rule.onAllNodesWithText("Every bead is shining. Well done!").fetchSemanticsNodes().isEmpty()) {
            check(moves++ < 30) { "beads took too many moves" }
            askForFullHelp()
            if (moves == 1) shot(shotName + "_guided")
            ok()
        }
        shot(shotName + "_done")
        rule.onNodeWithText("Continue").assertIsFocused()
        ok()
    }

    /** "Lantern Beads" from the home screen: a calm standalone game that never changes story progress. */
    @Test fun lanternBeadsFromHome() {
        runBlocking { app.container.profileRepository.add("Ruth", Avatar.SAGE) }
        waitForText("Who is playing today?")
        ok()
        waitForText("Start the case")
        focusButton("✨ Lantern Beads")
        shot("50_home_beads_button")
        ok()
        waitForText("Lantern Beads")
        ok() // Play again / Begin
        waitForText("Welcome to Lantern Beads", substring = true)
        ok()
        playBeadsWithHelp(shotName = "51_beads")
        waitForText("Every bead is shining. What a lovely glow!", substring = true)
        ok()
        waitForText("Well done, detective!")
        shot("52_beads_complete")
    }

    @Test fun backDuringActivity_asksAndKeepsPlaying() {
        runBlocking { app.container.profileRepository.add("Ruth", Avatar.SAGE) }
        waitForText("Who is playing today?")
        ok()
        waitForText("Start the case")
        ok()
        waitForText("Getting started")
        ok(); ok(); ok(); ok()
        waitForText("Found")
        back()
        waitForText("Would you like a break?")
        rule.onNodeWithText("Keep playing").assertIsFocused()
        shot("23_break_dialog")
        ok()
        waitForText("Found")
    }
}
