package net.rubinny.willowbrook.game.engine

/**
 * Repeat-the-sequence. The UI first shows the sequence; while showing, input is ignored.
 * A mistake never fails the spell: the sequence is simply shown again and the player starts over.
 */
class SpellEngine(val sequence: List<String>) {
    var position: Int = 0
        private set
    var isComplete: Boolean = false
        private set

    val expected: String? get() = sequence.getOrNull(position)

    sealed interface Result {
        data class Correct(val symbol: String, val complete: Boolean) : Result
        data class ShowAgain(val symbol: String) : Result
        data object Ignored : Result
    }

    fun input(symbol: String): Result {
        if (isComplete) return Result.Ignored
        return if (symbol == sequence[position]) {
            position++
            if (position == sequence.size) isComplete = true
            Result.Correct(symbol, isComplete)
        } else {
            position = 0
            Result.ShowAgain(symbol)
        }
    }

    fun restart() {
        if (!isComplete) position = 0
    }
}
