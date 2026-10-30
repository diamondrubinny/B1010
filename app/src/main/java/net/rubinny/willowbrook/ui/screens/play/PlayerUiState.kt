package net.rubinny.willowbrook.ui.screens.play

import net.rubinny.willowbrook.audio.SoundCue
import net.rubinny.willowbrook.game.engine.HintLevel
import net.rubinny.willowbrook.game.engine.SpokenLine
import net.rubinny.willowbrook.game.model.CaseContent
import net.rubinny.willowbrook.game.model.Category
import net.rubinny.willowbrook.game.model.Chapter
import net.rubinny.willowbrook.game.model.ChoiceKind
import net.rubinny.willowbrook.game.model.Item
import net.rubinny.willowbrook.game.model.SpellSymbol
import net.rubinny.willowbrook.game.model.Step

enum class Phase { RECAP, CHAPTER_INTRO, STEP, CHAPTER_COMPLETE, CASE_FINISHED }

data class HiddenObjectUi(
    val cells: List<Item>,
    val found: Set<String>,
    val targets: List<Item>,
    val complete: Boolean,
)

data class MemoryCardUi(val item: Item, val faceUp: Boolean, val matched: Boolean)

data class MemoryUi(val cards: List<MemoryCardUi>, val locked: Boolean, val complete: Boolean)

data class ChoiceUi(
    val kind: ChoiceKind,
    val question: String,
    val options: List<Item>,
    /** Options already tried that were not the answer: dimmed but still selectable (focus is never lost). */
    val tried: Set<String>,
    val answer: String,
    val complete: Boolean,
    /** A mystery question shows its clues, so nothing has to be remembered by heart. */
    val clues: List<Item> = emptyList(),
)

data class SortingBinUi(val category: Category, val placed: List<Item>)

data class SortingUi(
    val current: Item?,
    val bins: List<SortingBinUi>,
    val placedCount: Int,
    val total: Int,
    val complete: Boolean,
)

data class SpellUi(
    val symbols: List<SpellSymbol>,
    val length: Int,
    /** How many symbols the player has repeated correctly so far. */
    val position: Int,
    /** While the magic is being shown, the index (in [symbols]) of the symbol lit up right now. */
    val showingSymbol: Int?,
    val showing: Boolean,
    val complete: Boolean,
)

data class BeadUi(val id: Int, val symbol: SpellSymbol)

data class BeadsUi(
    val chain: List<BeadUi>,
    /** The bead the player is holding: always a symbol already on the string. */
    val hand: SpellSymbol,
    /** How many beads have turned into light so far. */
    val lit: Int,
    val complete: Boolean,
    /** Gap where beads last turned into light, and a counter that changes once per match (for the sparkle). */
    val sparkleGap: Int?,
    val sparkles: Int,
)

sealed interface PlayerUiState {
    data object Loading : PlayerUiState
    data class Error(val message: String) : PlayerUiState
    data class Playing(
        val case: CaseContent,
        val chapterIndex: Int,
        val stepIndex: Int,
        val phase: Phase,
        val replay: Boolean,
        val recapText: String? = null,
        val hiddenObject: HiddenObjectUi? = null,
        val memory: MemoryUi? = null,
        val choice: ChoiceUi? = null,
        val sorting: SortingUi? = null,
        val spell: SpellUi? = null,
        val beads: BeadsUi? = null,
        val hintLevel: HintLevel = HintLevel.NONE,
        /** The tile/card/option/bin/symbol the hint points at (glow at HIGHLIGHT, focus at GUIDE). */
        val hintIndex: Int? = null,
        /** What Marmalade says in the companion bar: feedback, the current hint, or what is left to do. */
        val companionMessage: String? = null,
        /** Narration is on: show "Listen again". */
        val canListen: Boolean = false,
        /** False only when the caregiver turned captions off AND narration is on. */
        val showStoryText: Boolean = true,
    ) : PlayerUiState {
        val chapter: Chapter get() = case.chapters[chapterIndex]
        val step: Step get() = chapter.steps[stepIndex]
        val activityComplete: Boolean
            get() = hiddenObject?.complete ?: memory?.complete ?: choice?.complete
                ?: sorting?.complete ?: spell?.complete ?: beads?.complete ?: true
    }
}

/** One-off events for the screen: speech and sounds. */
sealed interface PlayerEffect {
    /**
     * [companion] = a Marmalade bar message (feedback/hint). TalkBack already announces the bar
     * (live region), so the screen skips these when TalkBack is on to avoid hearing everything twice.
     */
    data class Speak(val line: SpokenLine, val rate: Float, val companion: Boolean = false) : PlayerEffect
    data class Sound(val cue: SoundCue, val volume: Float) : PlayerEffect
}
