package net.rubinny.willowbrook.game.engine

import net.rubinny.willowbrook.data.progress.CaseProgress
import net.rubinny.willowbrook.data.settings.Difficulty
import net.rubinny.willowbrook.game.model.CaseContent

/** How much help was needed recently, in plain words for the caregiver. */
enum class HelpNeeded(val text: String) {
    LITTLE("Mostly without help"),
    SOME("With some help"),
    LOTS("With lots of help"),
}

enum class HelpTrend(val text: String) {
    LESS("needing a little less help lately"),
    SAME("about the same as before"),
    MORE("needing a little more help lately"),
}

/**
 * A caregiver-facing summary of one player's progress. Purely descriptive — it is not a measure
 * of health or memory, and the screen says so.
 */
data class ProgressSummaryData(
    val chaptersDone: Int,
    val totalChapters: Int,
    /** Title of the chapter to play next, or null when the whole case is solved. */
    val nextChapterTitle: String?,
    val lastPlayedAt: Long?,
    val activitiesCounted: Int,
    val helpNeeded: HelpNeeded?,
    val trend: HelpTrend?,
    val difficulty: EffectiveDifficulty,
)

object ProgressSummary {
    const val MIN_ACTIVITIES = 3
    const val WINDOW = 5

    fun summarize(case: CaseContent, progress: CaseProgress, difficultySetting: Difficulty): ProgressSummaryData {
        val levels = progress.recentHintLevels
        val recent = levels.takeLast(WINDOW * 2)
        val help = if (levels.size < MIN_ACTIVITIES) null else classify(recent.average())
        val trend = if (levels.size < WINDOW * 2) {
            null
        } else {
            val before = levels.takeLast(WINDOW * 2).take(WINDOW).average()
            val lately = levels.takeLast(WINDOW).average()
            when {
                lately - before >= 0.75 -> HelpTrend.MORE
                before - lately >= 0.75 -> HelpTrend.LESS
                else -> HelpTrend.SAME
            }
        }
        return ProgressSummaryData(
            chaptersDone = case.chapters.count { it.id in progress.completedChapters },
            totalChapters = case.chapters.size,
            nextChapterTitle = case.chapters.getOrNull(progress.chapterIndex)?.title,
            lastPlayedAt = progress.updatedAt.takeIf { it > 0 },
            activitiesCounted = levels.size,
            helpNeeded = help,
            trend = trend,
            difficulty = DifficultyAdapter.resolve(difficultySetting, levels),
        )
    }

    /** Average hint level: up to a reminder = little help; up to a verbal hint and a half = some; more = lots. */
    private fun classify(average: Double): HelpNeeded = when {
        average <= HintLevel.REMIND.ordinal -> HelpNeeded.LITTLE
        average <= HintLevel.VERBAL.ordinal + 0.5 -> HelpNeeded.SOME
        else -> HelpNeeded.LOTS
    }
}
