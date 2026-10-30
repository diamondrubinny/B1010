package net.rubinny.willowbrook.game

import net.rubinny.willowbrook.game.engine.BeadsEngine
import net.rubinny.willowbrook.game.model.SpellSymbol
import net.rubinny.willowbrook.game.model.SpellSymbol.FLOWER
import net.rubinny.willowbrook.game.model.SpellSymbol.LEAF
import net.rubinny.willowbrook.game.model.SpellSymbol.MOON
import net.rubinny.willowbrook.game.model.SpellSymbol.STAR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BeadsEngineTest {
    private fun symbols(e: BeadsEngine) = e.chain.map { it.symbol }

    /** An engine whose hand is [hand] (the hand is chosen by the engine; search seeds for the wanted one). */
    private fun engineWithHand(start: List<SpellSymbol>, hand: SpellSymbol, maxLength: Int = 12): BeadsEngine =
        (0 until 500).asSequence().map { BeadsEngine(start, maxLength, it) }.first { it.hand == hand }

    @Test fun `new strings have no three in a row, always a pair, and the right length`() {
        for (seed in 0 until 300) {
            for ((length, palette) in listOf(8 to listOf(STAR, MOON, FLOWER), 11 to SpellSymbol.entries)) {
                val s = BeadsEngine.newString(length, palette, seed)
                assertEquals(length, s.size)
                assertTrue(s.all { it in palette })
                assertTrue("no triple in $s", s.windowed(3).none { (a, b, c) -> a == b && b == c })
                assertTrue("a pair in $s", s.zipWithNext().any { (a, b) -> a == b })
            }
        }
    }

    @Test fun `the bead in hand is on the string, and can make light at once when a pair exists`() {
        for (seed in 0 until 200) {
            val e = BeadsEngine(BeadsEngine.newString(9, SpellSymbol.entries, seed), 13, seed)
            assertTrue(e.hand in symbols(e))
            assertTrue("seed $seed", e.score(e.bestGap()) > 100)
        }
    }

    @Test fun `three together turn into light, and a closing string can match again`() {
        // STAR STAR | MOON | LEAF LEAF MOON  -> put MOON between the moons? Build a cascade:
        // MOON MOON _ STAR STAR MOON ... placing STAR at gap 2 clears STAR×3, then MOON MOON + MOON joins.
        val e = engineWithHand(listOf(MOON, MOON, STAR, STAR, MOON, LEAF), STAR)
        val r = e.place(2) as BeadsEngine.Result.Placed
        assertEquals(2, r.rounds.size)
        assertEquals(listOf(STAR, STAR, STAR), r.rounds[0].map { it.symbol })
        assertEquals(listOf(MOON, MOON, MOON), r.rounds[1].map { it.symbol })
        assertEquals(listOf(LEAF), symbols(e))
        assertEquals(6, e.lit)
    }

    @Test fun `a bead with no match is simply part of the string - nothing is lost`() {
        val e = engineWithHand(listOf(MOON, MOON, STAR, LEAF, LEAF), MOON)
        val before = e.chain.size
        val r = e.place(4) as BeadsEngine.Result.Placed // between the leaves: no match
        assertFalse(r.cleared)
        assertEquals(before + 1, e.chain.size)
    }

    @Test fun `a full string makes the bead roll to its friends`() {
        val start = listOf(MOON, MOON, STAR, LEAF, STAR, LEAF)
        val e = engineWithHand(start, MOON, maxLength = 6)
        val r = e.place(6) as BeadsEngine.Result.Placed // the end: would not match
        assertTrue(r.rolled)
        assertTrue(r.cleared)
        assertEquals(listOf(STAR, LEAF, STAR, LEAF), symbols(e))
    }

    @Test fun `out-of-range places are ignored`() {
        val e = BeadsEngine(listOf(STAR, STAR, MOON), 6, 1)
        assertEquals(BeadsEngine.Result.Ignored, e.place(-1))
        assertEquals(BeadsEngine.Result.Ignored, e.place(9))
    }

    @Test fun `whatever the player does, the string never grows past the limit`() {
        for (seed in 0 until 200) {
            val max = 13
            val e = BeadsEngine(BeadsEngine.newString(11, SpellSymbol.entries, seed), max, seed)
            val random = Random(seed)
            repeat(400) {
                if (!e.isComplete) e.place(random.nextInt(e.chain.size + 1))
                assertTrue("seed $seed size ${e.chain.size}", e.chain.size <= max + 1)
            }
        }
    }

    @Test fun `following the hints always lights every bead, in few moves`() {
        for (seed in 0 until 300) {
            for ((length, palette, max) in listOf(Triple(8, listOf(STAR, MOON, FLOWER), 11), Triple(11, SpellSymbol.entries, 13))) {
                val e = BeadsEngine(BeadsEngine.newString(length, palette, seed), max, seed)
                var moves = 0
                while (!e.isComplete && moves < 60) {
                    e.place(e.bestGap())
                    moves++
                }
                assertTrue("seed $seed length $length took $moves", e.isComplete)
                assertTrue(moves <= length * 2)
            }
        }
    }
}
