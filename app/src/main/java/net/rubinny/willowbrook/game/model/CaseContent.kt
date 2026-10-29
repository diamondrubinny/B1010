package net.rubinny.willowbrook.game.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Story content, loaded from assets/cases/<id>.json. Read-only at runtime.
 * Content rules (tone, banned themes, limits) are enforced by ContentValidator.
 */
@Serializable
data class CaseContent(
    val schemaVersion: Int,
    val id: String,
    val title: String,
    /** One sentence reminding the player of the overall goal ("What are we doing?"). */
    val premise: String,
    val items: List<Item>,
    val chapters: List<Chapter>,
) {
    fun item(id: String): Item? = items.firstOrNull { it.id == id }
}

/** A findable or matchable object. [emoji] is the placeholder until AI art "obj_<id>" exists. */
@Serializable
data class Item(
    val id: String,
    val label: String,
    val emoji: String,
)

@Serializable
data class Chapter(
    val id: String,
    val title: String,
    val location: String,
    /** Scene background key: drawable "scene_<scene>" if present, otherwise a calm gradient. */
    val scene: String,
    /** Short goal for this chapter, shown by "What are we doing?". */
    val goal: String,
    /** Shown when resuming: "Last time, …". */
    val recap: String,
    val steps: List<Step>,
)

@Serializable
sealed interface Step {
    val id: String

    /** Story text with an optional speaker (null = narrator). */
    @Serializable
    @SerialName("narration")
    data class Narration(
        override val id: String,
        val speaker: String? = null,
        /** Character art key: drawable "char_<portrait>" if present, otherwise [portraitEmoji]. */
        val portrait: String? = null,
        val portraitEmoji: String? = null,
        val text: String,
    ) : Step

    @Serializable
    @SerialName("hiddenObject")
    data class HiddenObject(
        override val id: String,
        val goal: String,
        val targets: List<String>,
        val decoys: List<String>,
        /** Step-specific verbal hints, one per target, in the same order as [targets]. */
        val hints: List<String>,
    ) : Step

    @Serializable
    @SerialName("memoryMatch")
    data class MemoryMatch(
        override val id: String,
        val goal: String,
        /** Item ids; each appears as a pair of cards. Difficulty decides how many are used. */
        val pairs: List<String>,
    ) : Step

    /** A gentle question with picture answers: recalling something said earlier, or a riddle. */
    @Serializable
    @SerialName("choice")
    data class Choice(
        override val id: String,
        val kind: ChoiceKind,
        val question: String,
        /** Item ids, 2–3. Gentle difficulty keeps the answer plus the first other option. */
        val options: List<String>,
        val answer: String,
        val hint: String,
    ) : Step

    /** Put each item where it belongs, one at a time. */
    @Serializable
    @SerialName("sorting")
    data class Sorting(
        override val id: String,
        val goal: String,
        val categories: List<Category>,
        val items: List<SortItem>,
    ) : Step

    /** Watch a short sequence of magic symbols, then repeat it. Symbols come from [SpellSymbols]. */
    @Serializable
    @SerialName("spell")
    data class Spell(
        override val id: String,
        val goal: String,
        val sequence: List<String>,
    ) : Step

    @Serializable
    @SerialName("reward")
    data class Reward(
        override val id: String,
        val text: String,
        val charm: Boolean = false,
    ) : Step
}

@Serializable
enum class ChoiceKind {
    @SerialName("recall") RECALL,
    @SerialName("riddle") RIDDLE,
}

@Serializable
data class Category(val id: String, val label: String, val emoji: String)

@Serializable
data class SortItem(val item: String, val category: String)

/** The four kind-magic symbols. Each has its own musical note, so the sequence can be remembered by ear too. */
enum class SpellSymbol(val id: String, val label: String, val emoji: String) {
    STAR("star", "Star", "⭐"),
    MOON("moon", "Moon", "🌙"),
    LEAF("leaf", "Leaf", "🍃"),
    FLOWER("flower", "Flower", "🌸");

    companion object {
        fun byId(id: String): SpellSymbol? = entries.firstOrNull { it.id == id }
    }
}
