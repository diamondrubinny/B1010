package net.rubinny.willowbrook.game.engine

/** Increasing levels of help. Every activity can always be finished by following the hints. */
enum class HintLevel {
    NONE,
    /** Marmalade gently restates the goal. */
    REMIND,
    /** A step-specific verbal clue ("It's near something warm"). */
    VERBAL,
    /** The answer glows softly. */
    HIGHLIGHT,
    /** Focus moves to the answer: pressing OK succeeds. */
    GUIDE,
}

/**
 * Pure function of elapsed time since the last progress, "Help me" presses and wrong choices.
 * Time thresholds are multiples of the caregiver's hint delay: 1x, 2x, 3x, 4.5x.
 */
object HintLadder {
    fun level(
        elapsedMillisSinceProgress: Long,
        hintDelaySec: Int,
        helpPresses: Int,
        wrongChoices: Int,
    ): HintLevel {
        val delay = hintDelaySec.coerceAtLeast(1) * 1000L
        val byTime = when {
            elapsedMillisSinceProgress >= delay * 9 / 2 -> HintLevel.GUIDE
            elapsedMillisSinceProgress >= delay * 3 -> HintLevel.HIGHLIGHT
            elapsedMillisSinceProgress >= delay * 2 -> HintLevel.VERBAL
            elapsedMillisSinceProgress >= delay -> HintLevel.REMIND
            else -> HintLevel.NONE
        }
        // Each "Help me" press climbs one rung, starting at VERBAL (a reminder alone is rarely what they asked for).
        val byHelp = if (helpPresses <= 0) HintLevel.NONE else levelAt(HintLevel.VERBAL.ordinal + helpPresses - 1)
        // Wrong choices are never punished: they simply bring help sooner.
        val byWrong = when {
            wrongChoices >= 3 -> HintLevel.HIGHLIGHT
            wrongChoices >= 2 -> HintLevel.VERBAL
            else -> HintLevel.NONE
        }
        return maxOf(byTime, byHelp, byWrong)
    }

    private fun levelAt(ordinal: Int): HintLevel = HintLevel.entries[ordinal.coerceIn(0, HintLevel.entries.lastIndex)]
}
