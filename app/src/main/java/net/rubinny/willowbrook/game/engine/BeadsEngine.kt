package net.rubinny.willowbrook.game.engine

import net.rubinny.willowbrook.game.model.SpellSymbol
import kotlin.random.Random

/**
 * "Lantern Beads": a string of light beads, each a symbol (star, moon, leaf, flower: shape and colour,
 * never colour alone). The player holds one bead and chooses where on the string to put it. Three or more
 * of the same side by side turn into light and leave the string; if the beads that close up behind them
 * also make three, they shine too.
 *
 * Designed for people living with dementia:
 * - There is no wrong move and nothing is ever lost. A bead that makes no match is simply part of the string.
 * - The bead in hand is always a symbol already on the string, preferably one that can make three at once,
 *   so a good move always exists.
 * - The string can never grow past [maxLength]: from then on a bead "rolls to its friends" (goes to the
 *   best place by itself), so the string always gets shorter again.
 * - No timer, no moving chain, no score.
 */
class BeadsEngine(
    start: List<SpellSymbol>,
    private val maxLength: Int,
    seed: Int,
) {
    data class Bead(val id: Int, val symbol: SpellSymbol)

    sealed interface Result {
        data object Ignored : Result

        /**
         * The bead was put at [gap] ([rolled] = it went to its friends by itself). [rounds] are the groups
         * that turned into light, in order (more than one = the string closed up and matched again).
         */
        data class Placed(val gap: Int, val rolled: Boolean, val rounds: List<List<Bead>>, val madePair: Boolean) : Result {
            val cleared get() = rounds.isNotEmpty()
        }
    }

    private val random = Random(seed)
    private var nextId = 0
    private val beads: MutableList<Bead> = start.map { Bead(nextId++, it) }.toMutableList()

    val chain: List<Bead> get() = beads
    var hand: SpellSymbol = start.first()
        private set
    var lit = 0
        private set
    val isComplete get() = beads.isEmpty()

    init {
        require(start.isNotEmpty()) { "A string needs beads" }
        require(maxLength >= start.size) { "maxLength must allow the starting string" }
        hand = nextHand()
    }

    /** Puts the bead in hand at [gap] (0 = before the first bead, chain.size = after the last). */
    fun place(gap: Int): Result {
        if (isComplete || gap !in 0..beads.size) return Result.Ignored
        val rolled = beads.size >= maxLength && score(gap) < CLEARS
        val at = if (rolled) bestGap() else gap
        val bead = Bead(nextId++, hand)
        beads.add(at, bead)
        val (start, end) = runAround(at)
        val madePair = end - start + 1 == 2
        val rounds = mutableListOf<List<Bead>>()
        var from = start
        var to = end
        while (to - from + 1 >= 3) {
            val group = beads.subList(from, to + 1).toList()
            repeat(group.size) { beads.removeAt(from) }
            rounds += group
            lit += group.size
            // The string closes up: do the beads now touching at the join make three?
            if (from == 0 || from >= beads.size || beads[from - 1].symbol != beads[from].symbol) break
            val joined = runAround(from)
            from = joined.first
            to = joined.second
        }
        if (!isComplete) hand = nextHand()
        return Result.Placed(at, rolled, rounds, madePair && rounds.isEmpty())
    }

    /** The most helpful place for the bead in hand (what hints point at): clears the most, else makes a pair. */
    fun bestGap(): Int = (0..beads.size).maxWith(compareBy({ score(it) }, { -it })) // ties: the leftmost

    /** How good it is to put the bead in hand at [gap]: CLEARS + run length, PAIR, or 0. */
    fun score(gap: Int): Int {
        var run = 1
        var i = gap - 1
        while (i >= 0 && beads[i].symbol == hand) { run++; i-- }
        i = gap
        while (i < beads.size && beads[i].symbol == hand) { run++; i++ }
        return when {
            run >= 3 -> CLEARS + run
            run == 2 -> PAIR
            else -> 0
        }
    }

    /** First and last index of the run of equal symbols that includes [index]. */
    private fun runAround(index: Int): Pair<Int, Int> {
        val symbol = beads[index].symbol
        var a = index
        var b = index
        while (a > 0 && beads[a - 1].symbol == symbol) a--
        while (b < beads.lastIndex && beads[b + 1].symbol == symbol) b++
        return a to b
    }

    /** Prefers a symbol that already has a pair on the string (three at once is possible), else any on it. */
    private fun nextHand(): SpellSymbol {
        val onString = beads.map { it.symbol }.distinct()
        val withPair = onString.filter { s -> beads.indices.any { i -> i > 0 && beads[i].symbol == s && beads[i - 1].symbol == s } }
        val pool = withPair.ifEmpty { onString }
        return pool[random.nextInt(pool.size)]
    }

    companion object {
        private const val CLEARS = 100
        private const val PAIR = 10

        /**
         * A starting string of [length] beads using [palette]: runs of one or two equal beads, never three,
         * and always at least one pair so the very first bead can make light.
         */
        fun newString(length: Int, palette: List<SpellSymbol>, seed: Int): List<SpellSymbol> {
            require(palette.size >= 2 && length >= 3)
            val random = Random(seed)
            val out = mutableListOf<SpellSymbol>()
            var previous: SpellSymbol? = null
            while (out.size < length) {
                val symbol = palette.filter { it != previous }.let { it[random.nextInt(it.size)] }
                val run = if (out.size + 2 <= length && (out.isEmpty() || random.nextFloat() < 0.4f)) 2 else 1
                repeat(run) { out += symbol }
                previous = symbol
            }
            return out
        }
    }
}
