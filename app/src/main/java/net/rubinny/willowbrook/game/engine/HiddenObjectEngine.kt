package net.rubinny.willowbrook.game.engine

import kotlin.random.Random

/**
 * Rules for "find the objects". Layout is shuffled with a seed derived from the step id,
 * so the same scene always looks the same — consistency supports memory.
 */
class HiddenObjectEngine(
    stepId: String,
    targets: List<String>,
    decoys: List<String>,
    decoyCount: Int,
) {
    val targets: List<String> = targets
    val cells: List<String> = (targets + decoys.take(decoyCount.coerceIn(0, decoys.size)))
        .shuffled(Random(stepId.hashCode()))

    private val found = linkedSetOf<String>()
    val foundItems: Set<String> get() = found
    val isComplete: Boolean get() = found.size == targets.size

    /** The next target to look for, in content order (used by hints). */
    val nextTarget: String? get() = targets.firstOrNull { it !in found }

    sealed interface Result {
        data class Found(val item: String, val complete: Boolean) : Result
        data class AlreadyFound(val item: String) : Result
        /** Not a target. Feedback is kind: "That's a lovely teapot. We're looking for …" */
        data class NotTarget(val item: String) : Result
    }

    fun select(cellIndex: Int): Result {
        val item = cells[cellIndex]
        return when {
            item in found -> Result.AlreadyFound(item)
            item in targets -> {
                found += item
                Result.Found(item, isComplete)
            }
            else -> Result.NotTarget(item)
        }
    }

    fun cellOf(item: String): Int = cells.indexOf(item)
}
