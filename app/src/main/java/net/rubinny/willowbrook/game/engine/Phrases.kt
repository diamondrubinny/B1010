package net.rubinny.willowbrook.game.engine

import net.rubinny.willowbrook.game.model.CaseContent
import net.rubinny.willowbrook.game.model.ChoiceKind
import net.rubinny.willowbrook.game.model.SpellSymbol
import net.rubinny.willowbrook.game.model.Step
import java.security.MessageDigest

/** How to say an item in a sentence: "the honey pot", "an autumn leaf", or a name like "Mrs. Bramble". */
data class Noun(private val label: String, private val properName: Boolean = false, val plural: Boolean = false) {
    private val lower = if (properName) label else label.lowercase()
    /** Labels like "The sea" already carry their article: never say "the the sea". */
    private val hasArticle = !properName && lower.startsWith("the ")
    val the: String get() = if (properName || hasArticle) lower else "the $lower"
    val a: String
        get() = when {
            properName || hasArticle -> lower
            plural -> "some $lower"
            else -> (if (lower.first() in "aeiou") "an " else "a ") + lower
        }
    val bare: String get() = if (hasArticle) lower.removePrefix("the ") else lower

    companion object {
        fun of(item: net.rubinny.willowbrook.game.model.Item) = Noun(item.label, item.properName, item.plural)
    }
}

/** A sorting destination in a sentence: "in the kitchen", "on the bookshelf". */
data class Place(private val label: String, private val preposition: String) {
    override fun toString() = "$preposition the ${label.lowercase()}"

    companion object {
        fun of(c: net.rubinny.willowbrook.game.model.Category) = Place(c.label, c.preposition)
    }
}

/**
 * Every sentence Marmalade can say during play, in one place. Item names are passed already lower-cased.
 * [PhraseCatalog] enumerates all of them for a case, so each one can be recorded in the same voice.
 */
object Phrases {
    // Hidden object
    fun foundAll() = "Wonderful! You found everything."
    fun found(item: Noun, next: Noun) = "Yes! You found ${item.the}. Now let's find ${next.the}."
    fun alreadyFound(item: Noun) = "You already found ${item.the}."
    fun lovely(item: Noun, wanted: Noun) =
        (if (item.plural) "Those are lovely ${item.bare}." else "That's a lovely ${item.bare}.") + " We're looking for ${wanted.the}."
    fun rememberLooking(target: Noun) = "Remember, we're looking for ${target.the}."
    fun pleaseFind(items: List<Noun>) = "Please find " + naturalList(items.map { it.the }) + "."

    // Shared hints
    fun glowing() = "I think it's the one that is glowing."
    fun hereItIs() = "Here it is! Press OK."

    // Memory
    fun findTwin() = "Now find its twin."
    fun allPairs() = "Every card has its twin. Well done!"
    fun aPair() = "A pair! Well done."
    fun notAPair() = "Not quite a pair. Let's turn them back and look again."
    fun lookingForPairs() = "We're looking for two cards that are the same."
    fun whereAnother(item: Noun) = "Where did you see another ${item.bare}?"
    fun turnACard() = "Turn over a card to see what is on it."
    fun tryGlowingCard() = "Try the card that is glowing."

    // Choice
    fun correct(kind: ChoiceKind, answer: Noun) =
        if (kind == ChoiceKind.RECALL) "Yes! It was ${answer.the}. What a good memory!" else "Yes! It's ${answer.the}. Well solved!"
    fun notThis(item: Noun) = "Not ${item.the}. Let's think again."
    fun takeYourTime() = "Take your time. Choose the picture you think is right."

    // Sorting
    fun allSorted() = "All sorted. Thank you!"
    fun sortedInto(item: Noun, place: Place) = "Yes, ${item.the} goes $place."
    fun wouldWeKeep(item: Noun, place: Place) = "I wonder, would we keep ${item.a} $place? Let's try again."
    fun whereBelongs(item: Noun) = "Where does ${item.the} belong?"
    fun thinkWhere(item: Noun) = "Think about where you would usually find ${item.a}."
    fun goesWhereGlowing() = "I think it goes where it's glowing."

    // Spell
    fun showing(symbol: SpellSymbol) = "Here is the ${symbol.label.lowercase()}."
    fun symbolYes(symbol: SpellSymbol) = "Yes, the ${symbol.label.lowercase()}!"
    fun spellWorked() = "The spell worked! Well done."
    fun watchAgain() = "Let's watch the magic once more."
    fun nowYouTry() = "Now you try. Press the symbols in the same order."
    fun watchTheMagic() = "Watch the magic…"
    fun sameOrder() = "Press the symbols in the same order as the magic."
    fun nextIs(symbol: SpellSymbol) = "Next is the ${symbol.label.lowercase()}."
    fun tryGlowingSymbol() = "Try the symbol that is glowing."

    /** "a", "a and b", "a, b and c" — plain English for lists. */
    fun naturalList(parts: List<String>): String = when (parts.size) {
        0 -> ""
        1 -> parts[0]
        else -> parts.dropLast(1).joinToString(", ") + " and " + parts.last()
    }

    /** Stable id for a recorded play-time phrase: assets/voice/<id>.ogg. */
    fun idFor(text: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(text.toByteArray(Charsets.UTF_8))
        return "say_" + digest.take(6).joinToString("") { "%02x".format(it) }
    }
}

/** All phrases Marmalade can speak during play for a case (feedback and hints), for recording. */
object PhraseCatalog {
    fun all(case: CaseContent): List<String> {
        val out = linkedSetOf<String>()
        fun label(id: String) = case.item(id)?.let(Noun::of) ?: Noun(id)
        with(Phrases) {
            out += listOf(foundAll(), glowing(), hereItIs(), findTwin(), allPairs(), aPair(), notAPair(), lookingForPairs(),
                turnACard(), tryGlowingCard(), allSorted(), goesWhereGlowing(), spellWorked(), watchAgain(), nowYouTry(),
                sameOrder(), tryGlowingSymbol())
            SpellSymbol.entries.forEach { out += listOf(showing(it), symbolYes(it), nextIs(it)) }
            case.chapters.flatMap { it.steps }.forEach { step ->
                when (step) {
                    is Step.HiddenObject -> {
                        val all = step.targets + step.decoys
                        step.targets.forEach { t ->
                            out += rememberLooking(label(t))
                            out += alreadyFound(label(t))
                            // "Found X, now find Y": Y is the first target (in content order) still missing.
                            step.targets.forEach { other -> if (other != t) out += found(label(t), label(other)) }
                            all.forEach { item -> if (item !in step.targets) out += lovely(label(item), label(t)) }
                        }
                    }
                    is Step.MemoryMatch -> step.pairs.forEach { out += whereAnother(label(it)) }
                    is Step.Choice -> {
                        out += correct(step.kind, label(step.answer))
                        step.options.filter { it != step.answer }.forEach { out += notThis(label(it)) }
                    }
                    is Step.Sorting -> step.items.forEach { si ->
                        val item = label(si.item)
                        out += whereBelongs(item)
                        out += thinkWhere(item)
                        step.categories.forEach { c ->
                            if (c.id == si.category) out += sortedInto(item, Place.of(c)) else out += wouldWeKeep(item, Place.of(c))
                        }
                    }
                    else -> Unit
                }
            }
        }
        return out.toList()
    }
}
