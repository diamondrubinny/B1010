package net.rubinny.willowbrook.ui.screens.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.data.progress.CaseProgress
import net.rubinny.willowbrook.data.progress.ProgressRepository
import net.rubinny.willowbrook.data.settings.Settings
import net.rubinny.willowbrook.data.settings.SettingsRepository
import net.rubinny.willowbrook.game.engine.DifficultyAdapter
import net.rubinny.willowbrook.game.engine.HiddenObjectEngine
import net.rubinny.willowbrook.game.engine.HintLadder
import net.rubinny.willowbrook.game.engine.HintLevel
import net.rubinny.willowbrook.game.engine.MemoryMatchEngine
import net.rubinny.willowbrook.game.model.CaseContent
import net.rubinny.willowbrook.game.model.Chapter
import net.rubinny.willowbrook.game.model.Item
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
        val hintLevel: HintLevel = HintLevel.NONE,
        /** The cell/card the hint points at (glow at HIGHLIGHT, focus at GUIDE). */
        val hintIndex: Int? = null,
        /** What Marmalade says in the companion bar: feedback or the current hint. */
        val companionMessage: String? = null,
    ) : PlayerUiState {
        val chapter: Chapter get() = case.chapters[chapterIndex]
        val step: Step get() = chapter.steps[stepIndex]
        val activityComplete: Boolean
            get() = hiddenObject?.complete ?: memory?.complete ?: true
    }
}

/**
 * Drives one play session: recap → chapter intro → steps → chapter complete.
 * Progress is saved after every step (never mid-activity), so a session can stop at any time.
 *
 * @param replayChapter when not null, plays that chapter again without changing saved progress.
 */
class ChapterPlayerViewModel(
    private val profileId: String,
    private val replayChapter: Int?,
    private val content: ContentRepository,
    private val progressRepo: ProgressRepository,
    private val settingsRepo: SettingsRepository,
    private val caseId: String = ContentRepository.DEFAULT_CASE,
    private val clock: () -> Long = System::currentTimeMillis,
    private val mismatchRevealMillis: Long = 2_500L,
) : ViewModel() {

    private val _state = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var progress = CaseProgress(caseId)
    private var settings = Settings()

    // Per-activity state
    private var hiddenEngine: HiddenObjectEngine? = null
    private var memoryEngine: MemoryMatchEngine? = null
    private var lastProgressAt = 0L
    private var helpPresses = 0
    private var wrongChoices = 0
    private var maxHintLevel = HintLevel.NONE
    private var feedback: String? = null
    private var hintJob: Job? = null

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        try {
            val case = content.load(caseId)
            settings = settingsRepo.current()
            progress = progressRepo.get(profileId, caseId)

            if (replayChapter != null) {
                val index = replayChapter.coerceIn(0, case.chapters.lastIndex)
                _state.value = PlayerUiState.Playing(case, index, 0, Phase.CHAPTER_INTRO, replay = true)
                return
            }
            if (progress.chapterIndex >= case.chapters.size) {
                _state.value = PlayerUiState.Playing(case, case.chapters.lastIndex, 0, Phase.CASE_FINISHED, replay = false)
                return
            }
            val chapterIndex = progress.chapterIndex
            val stepIndex = progress.stepIndex.coerceIn(0, case.chapters[chapterIndex].steps.lastIndex)
            val recap = when {
                stepIndex > 0 -> case.chapters[chapterIndex].recap
                chapterIndex > 0 -> "You finished \"${case.chapters[chapterIndex - 1].title}\"."
                else -> null
            }
            _state.value = PlayerUiState.Playing(
                case = case,
                chapterIndex = chapterIndex,
                stepIndex = stepIndex,
                phase = when {
                    recap != null -> Phase.RECAP
                    else -> Phase.CHAPTER_INTRO
                },
                replay = false,
                recapText = recap,
            )
        } catch (e: Exception) {
            _state.value = PlayerUiState.Error("Sorry, this part of the story could not be opened.")
        }
    }

    private fun playing(): PlayerUiState.Playing? = _state.value as? PlayerUiState.Playing

    /** "Carry on" on the recap screen. Mid-chapter resumes at the saved step; otherwise shows the chapter intro. */
    fun onRecapContinue() {
        val s = playing() ?: return
        if (s.stepIndex > 0) enterStep(s, s.stepIndex) else _state.value = s.copy(phase = Phase.CHAPTER_INTRO)
    }

    fun onChapterIntroContinue() {
        val s = playing() ?: return
        enterStep(s, s.stepIndex)
    }

    /** Continue button on narration/reward, or after an activity is complete. */
    fun onStepContinue() {
        val s = playing() ?: return
        if (s.phase != Phase.STEP || !s.activityComplete) return
        val chapter = s.chapter
        if (s.step is Step.HiddenObject || s.step is Step.MemoryMatch) {
            progress = progress.copy(recentHintLevels = progress.recentHintLevels + maxHintLevel.ordinal)
        }
        val next = s.stepIndex + 1
        if (next < chapter.steps.size) {
            save(progress.copy(chapterIndex = s.chapterIndex, stepIndex = next))
            enterStep(s, next)
        } else {
            save(
                progress.copy(
                    chapterIndex = s.chapterIndex + 1,
                    stepIndex = 0,
                    completedChapters = (progress.completedChapters + chapter.id).distinct(),
                ),
            )
            stopHints()
            _state.value = s.copy(phase = Phase.CHAPTER_COMPLETE, hiddenObject = null, memory = null, companionMessage = null)
        }
    }

    /** "Next chapter" on the chapter-complete screen. Returns false when there is no next chapter. */
    fun onNextChapter(): Boolean {
        val s = playing() ?: return false
        val next = s.chapterIndex + 1
        if (s.replay || next >= s.case.chapters.size) return false
        _state.value = s.copy(chapterIndex = next, stepIndex = 0, phase = Phase.CHAPTER_INTRO, recapText = null)
        return true
    }

    private fun save(next: CaseProgress) {
        progress = next
        val s = playing()
        if (s?.replay == true) return // replays never change saved progress
        viewModelScope.launch { progressRepo.save(profileId, next) }
    }

    private fun enterStep(s: PlayerUiState.Playing, stepIndex: Int) {
        stopHints()
        feedback = null
        helpPresses = 0
        wrongChoices = 0
        maxHintLevel = HintLevel.NONE
        hiddenEngine = null
        memoryEngine = null
        val step = s.chapter.steps[stepIndex]
        val difficulty = DifficultyAdapter.resolve(settings.difficulty, progress.recentHintLevels)
        var next = s.copy(
            stepIndex = stepIndex,
            phase = Phase.STEP,
            hiddenObject = null,
            memory = null,
            hintLevel = HintLevel.NONE,
            hintIndex = null,
            companionMessage = null,
        )
        when (step) {
            is Step.HiddenObject -> {
                hiddenEngine = HiddenObjectEngine(
                    step.id, step.targets, step.decoys,
                    DifficultyAdapter.hiddenObjectDecoys(difficulty, step.decoys.size),
                )
                next = next.copy(hiddenObject = hiddenUi(s.case))
            }
            is Step.MemoryMatch -> {
                val count = DifficultyAdapter.memoryPairs(difficulty, step.pairs.size)
                memoryEngine = MemoryMatchEngine(step.id, step.pairs.take(count))
                next = next.copy(memory = memoryUi(s.case))
            }
            is Step.Narration, is Step.Reward -> Unit
        }
        _state.value = next
        if (step is Step.HiddenObject || step is Step.MemoryMatch) {
            startHints()
            refresh()
        }
    }

    // ---- Hidden object ----

    fun onHiddenCellSelected(index: Int) {
        val s = playing() ?: return
        val engine = hiddenEngine ?: return
        if (engine.isComplete) return
        val case = s.case
        feedback = when (val r = engine.select(index)) {
            is HiddenObjectEngine.Result.Found -> {
                markProgress()
                val next = engine.nextTarget?.let { label(case, it).lowercase() }
                if (r.complete) {
                    "Wonderful! You found everything."
                } else {
                    "Yes! You found the ${label(case, r.item).lowercase()}. Now let's find the $next."
                }
            }
            is HiddenObjectEngine.Result.AlreadyFound -> "You already found the ${label(case, r.item).lowercase()}."
            is HiddenObjectEngine.Result.NotTarget -> {
                wrongChoices++
                val wanted = engine.nextTarget?.let { label(case, it).lowercase() }
                "That's a lovely ${label(case, r.item).lowercase()}. We're looking for the $wanted."
            }
        }
        if (engine.isComplete) stopHints()
        refresh()
    }

    // ---- Memory match ----

    fun onMemoryCardSelected(index: Int) {
        val engine = memoryEngine ?: return
        when (val r = engine.flip(index)) {
            MemoryMatchEngine.Result.Ignored -> return
            is MemoryMatchEngine.Result.FirstFlipped -> feedback = "Now find its twin."
            is MemoryMatchEngine.Result.Matched -> {
                markProgress()
                feedback = if (r.complete) "Every card has its twin. Well done!" else "A pair! Well done."
                if (r.complete) stopHints()
            }
            is MemoryMatchEngine.Result.Mismatched -> {
                wrongChoices++
                feedback = "Not quite a pair. Let's turn them back and look again."
                viewModelScope.launch {
                    delay(mismatchRevealMillis)
                    engine.hideMismatch()
                    refresh()
                }
            }
        }
        refresh()
    }

    // ---- Hints ----

    fun onHelpMe() {
        val s = playing() ?: return
        if (s.phase != Phase.STEP || s.activityComplete) return
        helpPresses++
        feedback = null
        refresh()
    }

    private fun markProgress() {
        lastProgressAt = clock()
        helpPresses = 0
        wrongChoices = 0
    }

    private fun startHints() {
        lastProgressAt = clock()
        hintJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                refresh()
            }
        }
    }

    private fun stopHints() {
        hintJob?.cancel()
        hintJob = null
    }

    /** Recomputes the activity UI and hint state from the engines. */
    private fun refresh() {
        val s = playing() ?: return
        val case = s.case
        val step = s.step
        val complete = hiddenEngine?.isComplete ?: memoryEngine?.isComplete ?: true
        val level = if (complete) {
            HintLevel.NONE
        } else {
            HintLadder.level(clock() - lastProgressAt, settings.hintDelaySec, helpPresses, wrongChoices)
        }
        if (level > maxHintLevel) maxHintLevel = level
        // A new, higher hint replaces older feedback so the newest help is what is on screen.
        if (level > s.hintLevel) feedback = null

        val hintIndex: Int?
        val hintText: String?
        // What Marmalade says when there is no news: what is still to do (an always-visible memory aid).
        val idleText: String? = when {
            step is Step.HiddenObject && hiddenEngine != null -> {
                val remaining = hiddenEngine!!.targets.filterNot { it in hiddenEngine!!.foundItems }
                if (remaining.isEmpty()) null
                else "Please find " + naturalList(remaining.map { "the " + label(case, it).lowercase() }) + "."
            }
            step is Step.MemoryMatch -> step.goal
            else -> null
        }
        when {
            step is Step.HiddenObject && hiddenEngine != null -> {
                val engine = hiddenEngine!!
                val target = engine.nextTarget
                val targetLabel = target?.let { label(case, it).lowercase() }
                hintIndex = target?.let(engine::cellOf)
                hintText = when (level) {
                    HintLevel.NONE -> null
                    HintLevel.REMIND -> "Remember, we're looking for the $targetLabel."
                    HintLevel.VERBAL -> step.hints.getOrNull(step.targets.indexOf(target))
                    HintLevel.HIGHLIGHT -> "I think it's the one that is glowing."
                    HintLevel.GUIDE -> "Here it is! Press OK."
                }
            }
            step is Step.MemoryMatch && memoryEngine != null -> {
                val engine = memoryEngine!!
                hintIndex = engine.suggestedIndex()
                val pendingLabel = engine.pendingIndex?.let { label(case, engine.cards[it].item).lowercase() }
                hintText = when (level) {
                    HintLevel.NONE -> null
                    HintLevel.REMIND -> "We're looking for two cards that are the same."
                    HintLevel.VERBAL -> if (pendingLabel != null) {
                        "Where did you see another $pendingLabel?"
                    } else {
                        "Turn over a card to see what is on it."
                    }
                    HintLevel.HIGHLIGHT -> "Try the card that is glowing."
                    HintLevel.GUIDE -> "Here it is! Press OK."
                }
            }
            else -> {
                hintIndex = null
                hintText = null
            }
        }

        _state.value = s.copy(
            hiddenObject = hiddenEngine?.let { hiddenUi(case) },
            memory = memoryEngine?.let { memoryUi(case) },
            hintLevel = level,
            hintIndex = if (level >= HintLevel.HIGHLIGHT) hintIndex else null,
            companionMessage = feedback ?: hintText ?: idleText,
        )
    }

    private fun hiddenUi(case: CaseContent): HiddenObjectUi {
        val e = hiddenEngine!!
        return HiddenObjectUi(
            cells = e.cells.map { item(case, it) },
            found = e.foundItems.toSet(),
            targets = e.targets.map { item(case, it) },
            complete = e.isComplete,
        )
    }

    private fun memoryUi(case: CaseContent): MemoryUi {
        val e = memoryEngine!!
        return MemoryUi(
            cards = e.cards.map { MemoryCardUi(item(case, it.item), it.faceUp, it.matched) },
            locked = e.mismatch != null,
            complete = e.isComplete,
        )
    }

    /** "a", "a and b", "a, b and c" — plain English for lists. */
    private fun naturalList(parts: List<String>): String = when (parts.size) {
        0 -> ""
        1 -> parts[0]
        else -> parts.dropLast(1).joinToString(", ") + " and " + parts.last()
    }

    private fun item(case: CaseContent, id: String): Item = case.item(id) ?: Item(id, id, "❔")

    private fun label(case: CaseContent, id: String): String = item(case, id).label

    override fun onCleared() {
        stopHints()
    }
}
