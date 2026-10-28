package net.rubinny.willowbrook.game.engine

import net.rubinny.willowbrook.data.settings.Difficulty

/** The concrete difficulty used for one activity (AUTO is resolved into one of these). */
enum class EffectiveDifficulty { GENTLE, STANDARD }

object DifficultyAdapter {
    private const val WINDOW = 3

    /**
     * AUTO starts gentle (errorless start). It moves to STANDARD only after the last [WINDOW]
     * activities needed no hint beyond a reminder, and drops back as soon as more help was needed.
     * Changes are silent — the player is never told the game got "easier".
     */
    fun resolve(setting: Difficulty, recentHintLevels: List<Int>): EffectiveDifficulty = when (setting) {
        Difficulty.GENTLE -> EffectiveDifficulty.GENTLE
        Difficulty.STANDARD -> EffectiveDifficulty.STANDARD
        Difficulty.AUTO -> {
            val recent = recentHintLevels.takeLast(WINDOW)
            if (recent.size == WINDOW && recent.all { it <= HintLevel.REMIND.ordinal }) {
                EffectiveDifficulty.STANDARD
            } else {
                EffectiveDifficulty.GENTLE
            }
        }
    }

    /** Hidden object: gentle shows all targets but at most 3 decoys. */
    fun hiddenObjectDecoys(difficulty: EffectiveDifficulty, available: Int): Int = when (difficulty) {
        EffectiveDifficulty.GENTLE -> minOf(3, available)
        EffectiveDifficulty.STANDARD -> available
    }

    /** Memory match: gentle = 2 pairs (4 cards), standard = up to 4 pairs (8 cards). */
    fun memoryPairs(difficulty: EffectiveDifficulty, available: Int): Int = when (difficulty) {
        EffectiveDifficulty.GENTLE -> minOf(2, available)
        EffectiveDifficulty.STANDARD -> minOf(4, available)
    }
}
