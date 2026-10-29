package net.rubinny.willowbrook.game

import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.game.engine.Noun
import net.rubinny.willowbrook.game.engine.PhraseCatalog
import net.rubinny.willowbrook.game.engine.Phrases
import net.rubinny.willowbrook.game.engine.Place
import net.rubinny.willowbrook.game.model.ChoiceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Grammar of everything Marmalade says. Several of these were real bugs found by listening tests. */
class PhrasesTest {
    @Test fun `articles a and an`() {
        assertEquals("Think about where you would usually find an autumn leaf.", Phrases.thinkWhere(Noun("Autumn leaf")))
        assertEquals("Think about where you would usually find a hat.", Phrases.thinkWhere(Noun("Hat")))
    }

    @Test fun `people are named without the and keep capitals`() {
        assertEquals("Not Miss Penrose. Let's think again.", Phrases.notThis(Noun("Miss Penrose", properName = true)))
        assertEquals("Yes! It was Mrs. Bramble. What a good memory!", Phrases.correct(ChoiceKind.RECALL, Noun("Mrs. Bramble", properName = true)))
    }

    @Test fun `labels that already have the are never doubled`() {
        assertEquals("Yes! It was the sea. What a good memory!", Phrases.correct(ChoiceKind.RECALL, Noun("The sea")))
        assertEquals("Not the stars. Let's think again.", Phrases.notThis(Noun("The stars", plural = true)))
    }

    @Test fun `plural things are never called a`() {
        assertEquals("Those are lovely scissors. We're looking for the candle.", Phrases.lovely(Noun("Scissors", plural = true), Noun("Candle")))
        assertEquals("That's a lovely globe. We're looking for the key.", Phrases.lovely(Noun("Globe"), Noun("Key")))
    }

    @Test fun `prepositions follow the place`() {
        assertEquals("Yes, the book goes on the bookshelf.", Phrases.sortedInto(Noun("Book"), Place("Bookshelf", "on")))
        assertEquals("I wonder, would we keep a hat on the bookshelf? Let's try again.", Phrases.wouldWeKeep(Noun("Hat"), Place("Bookshelf", "on")))
    }

    @Test fun `no phrase in the shipped case has doubled words or a before a plural`() {
        val case = ContentRepository.parseAndValidate(File("src/main/assets/cases/lantern.json").readText())
        val plurals = case.items.filter { it.plural }.map { it.label.lowercase().removePrefix("the ") }
        val doubled = Regex("\\b(\\w+) \\1\\b", RegexOption.IGNORE_CASE)
        assertTrue("the check itself must work", doubled.containsMatchIn("Yes! It was the the sea."))
        PhraseCatalog.all(case).forEach { phrase ->
            assertTrue(phrase, !doubled.containsMatchIn(phrase))
            plurals.forEach { p -> assertTrue(phrase, !phrase.contains("a lovely $p") && !phrase.contains(" a $p")) }
        }
    }
}
