package net.rubinny.willowbrook.game

import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.data.progress.CaseProgress
import net.rubinny.willowbrook.game.engine.ContentValidator
import net.rubinny.willowbrook.game.engine.Notebook
import net.rubinny.willowbrook.game.model.ChoiceKind
import net.rubinny.willowbrook.game.model.Step
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NotebookTest {
    private val case = ContentRepository.parseAndValidate(File("src/main/assets/cases/lantern.json").readText())
    private fun found(p: CaseProgress) = Notebook.entries(case, p).count { it.found }
    private fun indexOf(chapter: Int, id: String) = case.chapters[chapter].steps.indexOfFirst { it.id == id }

    @Test fun `the case has four clues, one in each chapter before the finale`() {
        val entries = Notebook.entries(case, CaseProgress("lantern"))
        assertEquals(listOf(0, 1, 2, 3), entries.map { it.chapterIndex })
        assertEquals(listOf("clue_pawprints", "clue_bitten_apple", "clue_leaf_trail", "clue_prickle"), entries.map { it.clue.clue })
    }

    @Test fun `clues count as found once seen, and stay found`() {
        assertEquals(0, found(CaseProgress("lantern")))
        assertEquals(0, found(CaseProgress("lantern", chapterIndex = 0, stepIndex = indexOf(0, "ch0_clue") - 1)))
        assertEquals(1, found(CaseProgress("lantern", chapterIndex = 0, stepIndex = indexOf(0, "ch0_clue"))))
        assertEquals(1, found(CaseProgress("lantern", chapterIndex = 1, completedChapters = listOf("ch0_cottage"))))
        val all = case.chapters.map { it.id }
        assertEquals(4, found(CaseProgress("lantern", chapterIndex = 4, completedChapters = all)))
    }

    @Test fun `the mystery question shows every clue and its answer is the hedgehog`() {
        val q = case.chapters.flatMap { it.steps }.filterIsInstance<Step.Choice>().single { it.kind == ChoiceKind.MYSTERY }
        assertEquals("hedgehog", q.answer)
        assertEquals(Notebook.entries(case, CaseProgress("lantern")).map { it.clue.clue }, q.clues)
    }

    @Test fun `clues speak of a little animal outdoors, never of someone in the house`() {
        val clues = case.chapters.flatMap { it.steps }.filterIsInstance<Step.Clue>()
        clues.forEach { c ->
            val t = c.text.lowercase()
            assertTrue(c.id, listOf("someone", "last night", "intruder", "stranger", "in the house").none { it in t })
        }
        assertTrue(clues.count { "little animal" in it.text } >= 3)
    }

    @Test fun `a mystery question without its clues is rejected`() {
        val bad = case.copy(
            chapters = case.chapters.map { ch ->
                ch.copy(steps = ch.steps.map { if (it is Step.Choice && it.kind == ChoiceKind.MYSTERY) it.copy(clues = emptyList()) else it })
            },
        )
        assertTrue(ContentValidator.validate(bad).any { "shows its clues" in it })
        val unknown = case.copy(
            chapters = case.chapters.map { ch -> ch.copy(steps = ch.steps.map { if (it is Step.Clue) it.copy(clue = "nothing") else it }) },
        )
        assertTrue(ContentValidator.validate(unknown).any { "unknown clue" in it })
    }
}
