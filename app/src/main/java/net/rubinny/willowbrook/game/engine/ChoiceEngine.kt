package net.rubinny.willowbrook.game.engine

import kotlin.random.Random

/**
 * A question with picture answers. A wrong pick only brings help closer; the question stays open
 * until the right answer is chosen. Option order is stable per step.
 */
class ChoiceEngine(stepId: String, options: List<String>, val answer: String) {
    val options: List<String> = options.shuffled(Random(stepId.hashCode()))
    private val tried = mutableSetOf<String>()
    var isComplete: Boolean = false
        private set

    /** Options already chosen that were not the answer (shown softly as "not this one"). */
    val triedOptions: Set<String> get() = tried

    val answerIndex: Int get() = options.indexOf(answer)

    sealed interface Result {
        data object Correct : Result
        data class NotThisOne(val item: String) : Result
        data object Ignored : Result
    }

    fun select(index: Int): Result {
        if (isComplete) return Result.Ignored
        val item = options[index]
        return if (item == answer) {
            isComplete = true
            Result.Correct
        } else {
            tried += item
            Result.NotThisOne(item)
        }
    }
}
