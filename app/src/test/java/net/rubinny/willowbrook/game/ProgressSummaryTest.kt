package net.rubinny.willowbrook.game

import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.data.progress.CaseProgress
import net.rubinny.willowbrook.data.settings.Difficulty
import net.rubinny.willowbrook.game.engine.EffectiveDifficulty
import net.rubinny.willowbrook.game.engine.HelpNeeded
import net.rubinny.willowbrook.game.engine.HelpTrend
import net.rubinny.willowbrook.game.engine.ProgressSummary
import net.rubinny.willowbrook.ui.screens.settings.describe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale
import java.util.TimeZone

class ProgressSummaryTest {
    private val case = ContentRepository.parseAndValidate(File("src/main/assets/cases/lantern.json").readText())

    private fun summary(levels: List<Int>, chapter: Int = 1, done: List<String> = listOf("ch0_cottage")) =
        ProgressSummary.summarize(
            case,
            CaseProgress("lantern", chapterIndex = chapter, completedChapters = done, recentHintLevels = levels, updatedAt = 1_700_000_000_000),
            Difficulty.AUTO,
        )

    @Test fun `new player has no help rating and starts gentle`() {
        val s = ProgressSummary.summarize(case, CaseProgress("lantern"), Difficulty.AUTO)
        assertEquals(0, s.chaptersDone)
        assertEquals(case.chapters.size, s.totalChapters)
        assertNull(s.helpNeeded)
        assertNull(s.lastPlayedAt)
        assertEquals(EffectiveDifficulty.GENTLE, s.difficulty)
        assertTrue(describe(s).contains("Not played yet"))
    }

    @Test fun `help is described in plain words`() {
        assertEquals(HelpNeeded.LITTLE, summary(listOf(0, 1, 0)).helpNeeded)
        assertEquals(HelpNeeded.SOME, summary(listOf(2, 2, 1)).helpNeeded)
        assertEquals(HelpNeeded.LOTS, summary(listOf(4, 3, 4)).helpNeeded)
        assertNull(summary(listOf(4, 4)).helpNeeded) // too few to say anything
    }

    @Test fun `trend compares the last five puzzles with the five before`() {
        assertEquals(HelpTrend.MORE, summary(List(5) { 0 } + List(5) { 3 }).trend)
        assertEquals(HelpTrend.LESS, summary(List(5) { 4 } + List(5) { 1 }).trend)
        assertEquals(HelpTrend.SAME, summary(List(10) { 2 }).trend)
        assertNull(summary(List(9) { 2 }).trend)
    }

    @Test fun `solved case and description`() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        val s = summary(listOf(0, 0, 0), chapter = case.chapters.size, done = case.chapters.map { it.id })
        assertNull(s.nextChapterTitle)
        val text = describe(s, Locale.UK)
        assertTrue(text, text.contains("Chapters finished: ${case.chapters.size} of ${case.chapters.size}"))
        assertTrue(text, text.contains("whole case is solved"))
        assertTrue(text, text.contains("14 November 2023"))
        assertTrue(text, text.contains("mostly without help"))
    }
}
