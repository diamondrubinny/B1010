package net.rubinny.willowbrook.game.engine

import kotlin.random.Random

/**
 * Classic pairs game, made gentle: no move counter, no timer. After a mismatch the two cards
 * stay visible until [hideMismatch] is called (the UI waits a generous moment first).
 */
class MemoryMatchEngine(stepId: String, pairItems: List<String>) {

    data class Card(val item: String, val faceUp: Boolean = false, val matched: Boolean = false)

    private val _cards: MutableList<Card> =
        (pairItems + pairItems).shuffled(Random(stepId.hashCode())).map { Card(it) }.toMutableList()
    val cards: List<Card> get() = _cards.toList()

    /** Index of the single face-up, unmatched card waiting for its partner. */
    var pendingIndex: Int? = null
        private set

    /** Two face-up cards that did not match; input is paused until hidden. */
    var mismatch: Pair<Int, Int>? = null
        private set

    val isComplete: Boolean get() = _cards.all { it.matched }

    sealed interface Result {
        data object Ignored : Result
        data class FirstFlipped(val index: Int) : Result
        data class Matched(val first: Int, val second: Int, val complete: Boolean) : Result
        data class Mismatched(val first: Int, val second: Int) : Result
    }

    fun flip(index: Int): Result {
        if (mismatch != null) return Result.Ignored
        val card = _cards[index]
        if (card.faceUp || card.matched) return Result.Ignored
        _cards[index] = card.copy(faceUp = true)

        val first = pendingIndex ?: run {
            pendingIndex = index
            return Result.FirstFlipped(index)
        }
        pendingIndex = null
        return if (_cards[first].item == card.item) {
            _cards[first] = _cards[first].copy(matched = true)
            _cards[index] = _cards[index].copy(matched = true)
            Result.Matched(first, index, isComplete)
        } else {
            mismatch = first to index
            Result.Mismatched(first, index)
        }
    }

    fun hideMismatch() {
        val (a, b) = mismatch ?: return
        _cards[a] = _cards[a].copy(faceUp = false)
        _cards[b] = _cards[b].copy(faceUp = false)
        mismatch = null
    }

    /**
     * The card the player should pick next for hints: the partner of the pending card,
     * otherwise the first card of any unmatched pair.
     */
    fun suggestedIndex(): Int? {
        pendingIndex?.let { p ->
            return _cards.indices.firstOrNull { it != p && _cards[it].item == _cards[p].item && !_cards[it].matched }
        }
        return _cards.indices.firstOrNull { !_cards[it].matched }
    }
}
