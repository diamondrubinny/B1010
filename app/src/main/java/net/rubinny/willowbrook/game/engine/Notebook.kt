package net.rubinny.willowbrook.game.engine

import net.rubinny.willowbrook.data.progress.CaseProgress
import net.rubinny.willowbrook.game.model.CaseContent
import net.rubinny.willowbrook.game.model.Step

/** One page of the detective notebook. */
data class NotebookEntry(val clue: Step.Clue, val chapterIndex: Int, val found: Boolean)

/**
 * The detective notebook: every clue of the case, in story order, and whether the player has seen it.
 * Worked out from saved progress, so nothing new is stored. A clue counts as seen once its chapter is
 * finished, or once the player has reached that step in the current chapter.
 */
object Notebook {
    fun entries(case: CaseContent, progress: CaseProgress): List<NotebookEntry> =
        case.chapters.flatMapIndexed { c, chapter ->
            chapter.steps.mapIndexedNotNull { s, step ->
                (step as? Step.Clue)?.let {
                    val found = chapter.id in progress.completedChapters ||
                        (c == progress.chapterIndex && s <= progress.stepIndex && progress.hasStarted) ||
                        c < progress.chapterIndex
                    NotebookEntry(it, c, found)
                }
            }
        }
}
