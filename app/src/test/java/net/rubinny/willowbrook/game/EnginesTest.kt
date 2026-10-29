package net.rubinny.willowbrook.game

import net.rubinny.willowbrook.data.settings.Difficulty
import net.rubinny.willowbrook.game.engine.DifficultyAdapter
import net.rubinny.willowbrook.game.engine.EffectiveDifficulty
import net.rubinny.willowbrook.game.engine.HiddenObjectEngine
import net.rubinny.willowbrook.game.engine.HintLadder
import net.rubinny.willowbrook.game.engine.HintLevel
import net.rubinny.willowbrook.game.engine.MemoryMatchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HintLadderTest {
    private fun level(sec: Int, help: Int = 0, wrong: Int = 0) = HintLadder.level(sec * 1000L, 20, help, wrong)

    @Test fun `time climbs the ladder at 1x 2x 3x and 4 and a half x the delay`() {
        assertEquals(HintLevel.NONE, level(19))
        assertEquals(HintLevel.REMIND, level(20))
        assertEquals(HintLevel.VERBAL, level(40))
        assertEquals(HintLevel.HIGHLIGHT, level(60))
        assertEquals(HintLevel.GUIDE, level(90))
    }

    @Test fun `help me jumps straight to a useful hint, then climbs`() {
        assertEquals(HintLevel.VERBAL, level(0, help = 1))
        assertEquals(HintLevel.HIGHLIGHT, level(0, help = 2))
        assertEquals(HintLevel.GUIDE, level(0, help = 3))
        assertEquals(HintLevel.GUIDE, level(0, help = 10))
    }

    @Test fun `wrong choices bring help sooner, never punishment`() {
        assertEquals(HintLevel.NONE, level(0, wrong = 1))
        assertEquals(HintLevel.VERBAL, level(0, wrong = 2))
        assertEquals(HintLevel.HIGHLIGHT, level(0, wrong = 3))
    }

    @Test fun `highest signal wins`() {
        assertEquals(HintLevel.GUIDE, level(95, help = 1, wrong = 2))
    }
}

class DifficultyAdapterTest {
    @Test fun `auto starts gentle`() {
        assertEquals(EffectiveDifficulty.GENTLE, DifficultyAdapter.resolve(Difficulty.AUTO, emptyList()))
    }

    @Test fun `auto moves up only after three activities with at most a reminder`() {
        assertEquals(EffectiveDifficulty.GENTLE, DifficultyAdapter.resolve(Difficulty.AUTO, listOf(0, 1)))
        assertEquals(EffectiveDifficulty.STANDARD, DifficultyAdapter.resolve(Difficulty.AUTO, listOf(4, 0, 1, 0)))
        assertEquals(EffectiveDifficulty.GENTLE, DifficultyAdapter.resolve(Difficulty.AUTO, listOf(0, 0, 0, 2)))
    }

    @Test fun `manual settings are respected`() {
        assertEquals(EffectiveDifficulty.GENTLE, DifficultyAdapter.resolve(Difficulty.GENTLE, listOf(0, 0, 0)))
        assertEquals(EffectiveDifficulty.STANDARD, DifficultyAdapter.resolve(Difficulty.STANDARD, listOf(4, 4, 4)))
    }

    @Test fun `gentle limits choices`() {
        assertEquals(3, DifficultyAdapter.hiddenObjectDecoys(EffectiveDifficulty.GENTLE, 5))
        assertEquals(5, DifficultyAdapter.hiddenObjectDecoys(EffectiveDifficulty.STANDARD, 5))
        assertEquals(2, DifficultyAdapter.memoryPairs(EffectiveDifficulty.GENTLE, 4))
        assertEquals(4, DifficultyAdapter.memoryPairs(EffectiveDifficulty.STANDARD, 4))
    }
}

class HiddenObjectEngineTest {
    private fun engine() = HiddenObjectEngine("step1", listOf("a", "b"), listOf("x", "y", "z"), decoyCount = 2)

    @Test fun `layout is the same every time for the same step`() {
        assertEquals(engine().cells, engine().cells)
        assertEquals(4, engine().cells.size) // 2 targets + 2 of 3 decoys
        assertTrue(engine().cells.containsAll(listOf("a", "b")))
    }

    @Test fun `finding all targets completes, wrong and repeated picks are harmless`() {
        val e = engine()
        val decoy = e.cells.indexOfFirst { it !in e.targets }
        assertTrue(e.select(decoy) is HiddenObjectEngine.Result.NotTarget)
        assertEquals("a", e.nextTarget)
        val a = e.select(e.cellOf("a"))
        assertEquals(HiddenObjectEngine.Result.Found("a", complete = false), a)
        assertTrue(e.select(e.cellOf("a")) is HiddenObjectEngine.Result.AlreadyFound)
        assertEquals("b", e.nextTarget)
        assertEquals(HiddenObjectEngine.Result.Found("b", complete = true), e.select(e.cellOf("b")))
        assertTrue(e.isComplete)
        assertNull(e.nextTarget)
    }
}

class MemoryMatchEngineTest {
    @Test fun `matching pairs completes the game`() {
        val e = MemoryMatchEngine("m1", listOf("apple", "lemon"))
        assertEquals(4, e.cards.size)
        repeat(2) {
            val first = e.suggestedIndex()!!
            assertTrue(e.flip(first) is MemoryMatchEngine.Result.FirstFlipped)
            val partner = e.suggestedIndex()!!
            assertEquals(e.cards[first].item, e.cards[partner].item)
            assertTrue(e.flip(partner) is MemoryMatchEngine.Result.Matched)
        }
        assertTrue(e.isComplete)
    }

    @Test fun `mismatch stays visible and blocks input until hidden`() {
        val e = MemoryMatchEngine("m1", listOf("apple", "lemon"))
        val first = 0
        val wrong = e.cards.indices.first { e.cards[it].item != e.cards[first].item }
        e.flip(first)
        assertTrue(e.flip(wrong) is MemoryMatchEngine.Result.Mismatched)
        assertTrue(e.cards[first].faceUp && e.cards[wrong].faceUp)
        val other = e.cards.indices.first { it != first && it != wrong }
        assertEquals(MemoryMatchEngine.Result.Ignored, e.flip(other))
        e.hideMismatch()
        assertFalse(e.cards[first].faceUp || e.cards[wrong].faceUp)
        assertTrue(e.flip(other) is MemoryMatchEngine.Result.FirstFlipped)
    }

    @Test fun `flipping a face-up or matched card is ignored`() {
        val e = MemoryMatchEngine("m1", listOf("apple", "lemon"))
        e.flip(0)
        assertEquals(MemoryMatchEngine.Result.Ignored, e.flip(0))
    }
}

class ChoiceEngineTest {
    @Test fun `wrong answers are remembered softly and the right one completes`() {
        val e = net.rubinny.willowbrook.game.engine.ChoiceEngine("q1", listOf("pear", "lemon", "cherries"), "pear")
        assertEquals(e.options, net.rubinny.willowbrook.game.engine.ChoiceEngine("q1", listOf("pear", "lemon", "cherries"), "pear").options)
        val wrong = e.options.indexOfFirst { it != "pear" }
        assertTrue(e.select(wrong) is net.rubinny.willowbrook.game.engine.ChoiceEngine.Result.NotThisOne)
        assertEquals(setOf(e.options[wrong]), e.triedOptions)
        assertFalse(e.isComplete)
        assertEquals(net.rubinny.willowbrook.game.engine.ChoiceEngine.Result.Correct, e.select(e.answerIndex))
        assertTrue(e.isComplete)
        assertEquals(net.rubinny.willowbrook.game.engine.ChoiceEngine.Result.Ignored, e.select(0))
    }
}

class SortingEngineTest {
    private val items = listOf(
        net.rubinny.willowbrook.game.model.SortItem("tulip", "garden"),
        net.rubinny.willowbrook.game.model.SortItem("teacup", "kitchen"),
        net.rubinny.willowbrook.game.model.SortItem("ladybird", "garden"),
        net.rubinny.willowbrook.game.model.SortItem("cheese", "kitchen"),
        net.rubinny.willowbrook.game.model.SortItem("leaf", "garden"),
        net.rubinny.willowbrook.game.model.SortItem("bread", "kitchen"),
    )

    @Test fun `gentle pick keeps both categories`() {
        val picked = net.rubinny.willowbrook.game.engine.SortingEngine.pick(items, listOf("garden", "kitchen"), 4)
        assertEquals(4, picked.size)
        assertEquals(2, picked.count { it.category == "garden" })
        assertEquals(2, picked.count { it.category == "kitchen" })
    }

    @Test fun `wrong bin keeps the same item, right bin moves on until complete`() {
        val e = net.rubinny.willowbrook.game.engine.SortingEngine("s1", items, listOf("garden", "kitchen"))
        val first = e.current!!
        val wrongBin = if (first.category == "garden") 1 else 0
        assertTrue(e.place(wrongBin) is net.rubinny.willowbrook.game.engine.SortingEngine.Result.Wrong)
        assertEquals(first, e.current)
        while (!e.isComplete) {
            assertTrue(e.place(e.correctCategoryIndex!!) is net.rubinny.willowbrook.game.engine.SortingEngine.Result.Correct)
        }
        assertEquals(items.size, e.placedItems.size)
    }
}

class SpellEngineTest {
    @Test fun `correct sequence completes, a mistake starts over without failing`() {
        val e = net.rubinny.willowbrook.game.engine.SpellEngine(listOf("flower", "leaf", "star"))
        assertTrue(e.input("flower") is net.rubinny.willowbrook.game.engine.SpellEngine.Result.Correct)
        assertTrue(e.input("moon") is net.rubinny.willowbrook.game.engine.SpellEngine.Result.ShowAgain)
        assertEquals(0, e.position)
        listOf("flower", "leaf").forEach { e.input(it) }
        val last = e.input("star")
        assertEquals(net.rubinny.willowbrook.game.engine.SpellEngine.Result.Correct("star", complete = true), last)
        assertTrue(e.isComplete)
        assertEquals(net.rubinny.willowbrook.game.engine.SpellEngine.Result.Ignored, e.input("star"))
    }
}
