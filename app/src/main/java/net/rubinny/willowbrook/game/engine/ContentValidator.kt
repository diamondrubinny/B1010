package net.rubinny.willowbrook.game.engine

import net.rubinny.willowbrook.game.model.CaseContent
import net.rubinny.willowbrook.game.model.SpellSymbol
import net.rubinny.willowbrook.game.model.Step

/**
 * Guards both technical correctness and the clinical content rules from docs/PLAN.md:
 * no theft/crime/danger themes, short texts, few choices, every activity solvable.
 * Runs in unit tests against the shipped content and at load time in debug builds.
 */
object ContentValidator {
    const val SUPPORTED_SCHEMA = 1
    const val MAX_TEXT_LENGTH = 220
    const val MAX_TARGETS = 3
    const val MAX_HIDDEN_ITEMS = 8
    const val MIN_PAIRS = 2
    const val MAX_PAIRS = 4
    const val MIN_OPTIONS = 2
    const val MAX_OPTIONS = 3
    const val MIN_CATEGORIES = 2
    const val MAX_CATEGORIES = 3
    const val MIN_SORT_ITEMS = 2
    const val MAX_SORT_ITEMS = 6
    const val MIN_SPELL = 2
    const val MAX_SPELL = 4

    /**
     * Words that suggest theft, crime, danger or loss. Suspicion that "someone took my things"
     * is a common dementia symptom, so stories are about misplaced things, never stolen ones.
     */
    val BANNED_WORDS = setOf(
        "steal", "stole", "stolen", "theft", "thief", "thieves", "rob", "robbed", "robbery", "burglar",
        "crime", "criminal", "police", "arrest", "guilty", "suspect", "culprit", "villain",
        "dead", "death", "die", "died", "kill", "killed", "murder", "blood", "poison",
        "scary", "terrifying", "monster", "ghost", "witch", "curse", "cursed", "danger", "dangerous",
        "lost forever", "never come back", "missing person",
    )

    fun validate(content: CaseContent): List<String> {
        val errors = mutableListOf<String>()
        fun err(msg: String) = errors.add(msg)

        if (content.schemaVersion != SUPPORTED_SCHEMA) err("Unsupported schemaVersion ${content.schemaVersion}")
        if (content.chapters.isEmpty()) err("Case has no chapters")

        val itemIds = content.items.map { it.id }
        itemIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys.forEach { err("Duplicate item id '$it'") }
        content.items.forEach {
            if (!it.id.matches(Regex("[a-z][a-z0-9_]*"))) err("Item id '${it.id}' must be lowercase snake_case")
            if (it.label.isBlank() || it.emoji.isBlank()) err("Item '${it.id}' needs a label and an emoji")
        }

        val chapterIds = content.chapters.map { it.id }
        chapterIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys.forEach { err("Duplicate chapter id '$it'") }
        val stepIds = content.chapters.flatMap { c -> c.steps.map { it.id } }
        stepIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys.forEach { err("Duplicate step id '$it'") }

        val texts = mutableListOf(content.title, content.premise)
        content.items.forEach { texts += it.label }

        content.chapters.forEach { chapter ->
            if (chapter.steps.isEmpty()) err("Chapter '${chapter.id}' has no steps")
            if (chapter.steps.lastOrNull() !is Step.Reward) err("Chapter '${chapter.id}' must end with a reward")
            if (!chapter.scene.matches(Regex("[a-z][a-z0-9_]*"))) err("Chapter '${chapter.id}' scene key must be snake_case")
            texts += listOf(chapter.title, chapter.location, chapter.goal, chapter.recap)

            chapter.steps.forEach { step ->
                when (step) {
                    is Step.Narration -> texts += listOfNotNull(step.text, step.speaker)
                    is Step.Reward -> texts += step.text
                    is Step.HiddenObject -> {
                        texts += step.goal
                        texts += step.hints
                        if (step.targets.isEmpty()) err("${step.id}: needs at least one target")
                        if (step.targets.size > MAX_TARGETS) err("${step.id}: at most $MAX_TARGETS targets")
                        if (step.targets.size + step.decoys.size > MAX_HIDDEN_ITEMS) {
                            err("${step.id}: at most $MAX_HIDDEN_ITEMS items on screen")
                        }
                        if (step.hints.size != step.targets.size) err("${step.id}: needs exactly one hint per target")
                        (step.targets + step.decoys).forEach { if (it !in itemIds) err("${step.id}: unknown item '$it'") }
                        if ((step.targets + step.decoys).toSet().size != step.targets.size + step.decoys.size) {
                            err("${step.id}: an item appears twice")
                        }
                    }
                    is Step.Choice -> {
                        texts += listOf(step.question, step.hint)
                        if (step.options.size !in MIN_OPTIONS..MAX_OPTIONS) err("${step.id}: needs $MIN_OPTIONS-$MAX_OPTIONS options")
                        if (step.options.toSet().size != step.options.size) err("${step.id}: duplicate option")
                        if (step.answer !in step.options) err("${step.id}: the answer must be one of the options")
                        step.options.forEach { if (it !in itemIds) err("${step.id}: unknown item '$it'") }
                    }
                    is Step.Sorting -> {
                        texts += step.goal
                        step.categories.forEach { texts += it.label }
                        val categoryIds = step.categories.map { it.id }
                        if (step.categories.size !in MIN_CATEGORIES..MAX_CATEGORIES) {
                            err("${step.id}: needs $MIN_CATEGORIES-$MAX_CATEGORIES categories")
                        }
                        if (categoryIds.toSet().size != categoryIds.size) err("${step.id}: duplicate category")
                        if (step.items.size !in MIN_SORT_ITEMS..MAX_SORT_ITEMS) err("${step.id}: needs $MIN_SORT_ITEMS-$MAX_SORT_ITEMS items")
                        if (step.items.map { it.item }.toSet().size != step.items.size) err("${step.id}: an item appears twice")
                        step.items.forEach {
                            if (it.item !in itemIds) err("${step.id}: unknown item '${it.item}'")
                            if (it.category !in categoryIds) err("${step.id}: unknown category '${it.category}'")
                        }
                        categoryIds.forEach { c -> if (step.items.none { it.category == c }) err("${step.id}: category '$c' is empty") }
                    }
                    is Step.Spell -> {
                        texts += step.goal
                        if (step.sequence.size !in MIN_SPELL..MAX_SPELL) err("${step.id}: spell needs $MIN_SPELL-$MAX_SPELL symbols")
                        step.sequence.forEach { if (SpellSymbol.byId(it) == null) err("${step.id}: unknown symbol '$it'") }
                    }
                    is Step.MemoryMatch -> {
                        texts += step.goal
                        if (step.pairs.size !in MIN_PAIRS..MAX_PAIRS) err("${step.id}: needs $MIN_PAIRS-$MAX_PAIRS pairs")
                        if (step.pairs.toSet().size != step.pairs.size) err("${step.id}: duplicate pair item")
                        step.pairs.forEach { if (it !in itemIds) err("${step.id}: unknown item '$it'") }
                    }
                }
            }
        }

        texts.forEach { text ->
            if (text.isBlank()) err("Empty text found")
            if (text.length > MAX_TEXT_LENGTH) err("Text too long (${text.length} > $MAX_TEXT_LENGTH): \"${text.take(40)}…\"")
            val lower = text.lowercase()
            BANNED_WORDS.forEach { word ->
                if (Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(lower)) err("Banned word '$word' in: \"${text.take(60)}\"")
            }
        }
        return errors
    }
}
