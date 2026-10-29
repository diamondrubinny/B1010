package net.rubinny.willowbrook.ui.screens.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.rubinny.willowbrook.audio.SoundCue
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.data.progress.CaseProgress
import net.rubinny.willowbrook.data.progress.ProgressRepository
import net.rubinny.willowbrook.data.settings.Settings
import net.rubinny.willowbrook.data.settings.SettingsRepository
import net.rubinny.willowbrook.game.engine.ChoiceEngine
import net.rubinny.willowbrook.game.engine.DifficultyAdapter
import net.rubinny.willowbrook.game.engine.HiddenObjectEngine
import net.rubinny.willowbrook.game.engine.HintLadder
import net.rubinny.willowbrook.game.engine.HintLevel
import net.rubinny.willowbrook.game.engine.MemoryMatchEngine
import net.rubinny.willowbrook.game.engine.Noun
import net.rubinny.willowbrook.game.engine.Phrases
import net.rubinny.willowbrook.game.engine.Place
import net.rubinny.willowbrook.game.engine.SortingEngine
import net.rubinny.willowbrook.game.engine.SpellEngine
import net.rubinny.willowbrook.game.engine.SpokenLine
import net.rubinny.willowbrook.game.engine.VoiceLines
import net.rubinny.willowbrook.game.model.CaseContent
import net.rubinny.willowbrook.game.model.ChoiceKind
import net.rubinny.willowbrook.game.model.Item
import net.rubinny.willowbrook.game.model.SpellSymbol
import net.rubinny.willowbrook.game.model.Step

/**
 * Drives one play session: recap → chapter intro → steps → chapter complete.
 * Progress is saved after every step (never mid-activity), so a session can stop at any time.
 * Speech and sounds are emitted as [effects]; the screen plays them.
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
    private val spellSymbolMillis: Long = 1_200L,
    private val spellGapMillis: Long = 400L,
    private val spellLeadInMillis: Long = 3_000L,
) : ViewModel() {

    private val _state = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PlayerEffect>(extraBufferCapacity = 32)
    val effects: SharedFlow<PlayerEffect> = _effects.asSharedFlow()

    private var progress = CaseProgress(caseId)
    private var settings = Settings()

    // Per-activity state
    private var hiddenEngine: HiddenObjectEngine? = null
    private var memoryEngine: MemoryMatchEngine? = null
    private var choiceEngine: ChoiceEngine? = null
    private var sortingEngine: SortingEngine? = null
    private var spellEngine: SpellEngine? = null
    private var spellShowing = false
    private var spellShowingSymbol: Int? = null
    private var spellJob: Job? = null
    private var lastProgressAt = 0L
    private var helpPresses = 0
    private var wrongChoices = 0
    private var maxHintLevel = HintLevel.NONE
    private var feedback: String? = null
    private var lastSpokenCompanion: String? = null
    private var hintJob: Job? = null

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        try {
            val case = content.load(caseId)
            settings = settingsRepo.current()
            progress = progressRepo.get(profileId, caseId)
            val base = PlayerUiState.Playing(
                case = case,
                chapterIndex = 0,
                stepIndex = 0,
                phase = Phase.CHAPTER_INTRO,
                replay = false,
                canListen = settings.narrationEnabled,
                showStoryText = settings.captionsEnabled || !settings.narrationEnabled,
            )

            if (replayChapter != null) {
                val index = replayChapter.coerceIn(0, case.chapters.lastIndex)
                showIntro(base.copy(chapterIndex = index, replay = true))
                return
            }
            if (progress.chapterIndex >= case.chapters.size) {
                _state.value = base.copy(chapterIndex = case.chapters.lastIndex, phase = Phase.CASE_FINISHED)
                speak(VoiceLines.caseSolved(case))
                return
            }
            val chapterIndex = progress.chapterIndex
            val stepIndex = progress.stepIndex.coerceIn(0, case.chapters[chapterIndex].steps.lastIndex)
            val recap: SpokenLine? = when {
                stepIndex > 0 -> VoiceLines.recapMidChapter(case.chapters[chapterIndex])
                chapterIndex > 0 -> VoiceLines.recapAfterChapter(case.chapters[chapterIndex - 1])
                else -> null
            }
            val located = base.copy(chapterIndex = chapterIndex, stepIndex = stepIndex)
            if (recap != null) {
                val text = if (stepIndex > 0) case.chapters[chapterIndex].recap else "You finished \"${case.chapters[chapterIndex - 1].title}\"."
                _state.value = located.copy(phase = Phase.RECAP, recapText = text)
                speak(recap)
            } else {
                showIntro(located)
            }
        } catch (e: Exception) {
            _state.value = PlayerUiState.Error("Sorry, this part of the story could not be opened.")
        }
    }

    private fun playing(): PlayerUiState.Playing? = _state.value as? PlayerUiState.Playing

    private fun speak(line: SpokenLine, companion: Boolean = false) {
        if (settings.narrationEnabled) _effects.tryEmit(PlayerEffect.Speak(line, settings.speechRate, companion))
    }

    private fun sound(cue: SoundCue) {
        _effects.tryEmit(PlayerEffect.Sound(cue, settings.musicVolume))
    }

    private fun showIntro(s: PlayerUiState.Playing) {
        _state.value = s.copy(phase = Phase.CHAPTER_INTRO, stepIndex = 0, recapText = null)
        speak(VoiceLines.chapterIntro(s.chapter, s.chapterIndex))
    }

    /** "Carry on" on the recap screen. Mid-chapter resumes at the saved step; otherwise shows the chapter intro. */
    fun onRecapContinue() {
        val s = playing() ?: return
        if (s.stepIndex > 0) enterStep(s, s.stepIndex) else showIntro(s)
    }

    fun onChapterIntroContinue() {
        val s = playing() ?: return
        enterStep(s, s.stepIndex)
    }

    /** Re-reads the current screen's main line. */
    fun onListenAgain() {
        val s = playing() ?: return
        when (s.phase) {
            Phase.CHAPTER_INTRO -> speak(VoiceLines.chapterIntro(s.chapter, s.chapterIndex))
            Phase.STEP -> speak(VoiceLines.stepLine(s.step))
            Phase.CHAPTER_COMPLETE -> speak(VoiceLines.chapterComplete(s.chapter))
            else -> s.recapText?.let { speak(SpokenLine(null, it)) }
        }
    }

    /** "Let's chat": reads a conversation starter aloud. Nothing about the conversation is stored. */
    fun onChat(index: Int) {
        val s = playing() ?: return
        VoiceLines.chat(s.chapter, index)?.let(::speak)
    }

    fun onWhatAreWeDoing() {
        val s = playing() ?: return
        speak(VoiceLines.whatAreWeDoing(s.case, s.chapter))
    }

    /** Continue button on narration/reward, or after an activity is complete. */
    fun onStepContinue() {
        val s = playing() ?: return
        if (s.phase != Phase.STEP || !s.activityComplete) return
        val chapter = s.chapter
        if (s.step.isActivity()) {
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
            stopActivityJobs()
            _state.value = s.copy(
                phase = Phase.CHAPTER_COMPLETE,
                hiddenObject = null, memory = null, choice = null, sorting = null, spell = null,
                companionMessage = null,
            )
            speak(VoiceLines.chapterComplete(chapter))
        }
    }

    /** "Next chapter" on the chapter-complete screen. Returns false when there is no next chapter. */
    fun onNextChapter(): Boolean {
        val s = playing() ?: return false
        val next = s.chapterIndex + 1
        if (s.replay || next >= s.case.chapters.size) return false
        showIntro(s.copy(chapterIndex = next))
        return true
    }

    private fun save(next: CaseProgress) {
        progress = next
        if (playing()?.replay == true) return // replays never change saved progress
        viewModelScope.launch { progressRepo.save(profileId, next) }
    }

    private fun Step.isActivity() = this !is Step.Narration && this !is Step.Reward

    private fun stopActivityJobs() {
        hintJob?.cancel()
        hintJob = null
        spellJob?.cancel()
        spellJob = null
    }

    private fun enterStep(s: PlayerUiState.Playing, stepIndex: Int) {
        stopActivityJobs()
        feedback = null
        lastSpokenCompanion = null
        helpPresses = 0
        wrongChoices = 0
        maxHintLevel = HintLevel.NONE
        hiddenEngine = null
        memoryEngine = null
        choiceEngine = null
        sortingEngine = null
        spellEngine = null
        spellShowing = false
        spellShowingSymbol = null

        val step = s.chapter.steps[stepIndex]
        val difficulty = DifficultyAdapter.resolve(settings.difficulty, progress.recentHintLevels)
        when (step) {
            is Step.HiddenObject -> hiddenEngine = HiddenObjectEngine(
                step.id, step.targets, step.decoys, DifficultyAdapter.hiddenObjectDecoys(difficulty, step.decoys.size),
            )
            is Step.MemoryMatch ->
                memoryEngine = MemoryMatchEngine(step.id, step.pairs.take(DifficultyAdapter.memoryPairs(difficulty, step.pairs.size)))
            is Step.Choice -> {
                val count = DifficultyAdapter.choiceOptions(difficulty, step.options.size)
                val others = step.options.filter { it != step.answer }.take(count - 1)
                choiceEngine = ChoiceEngine(step.id, step.options.filter { it == step.answer || it in others }, step.answer)
            }
            is Step.Sorting -> {
                val categories = step.categories.map { it.id }
                val items = SortingEngine.pick(step.items, categories, DifficultyAdapter.sortingItems(difficulty, step.items.size))
                sortingEngine = SortingEngine(step.id, items, categories)
            }
            is Step.Spell -> spellEngine = SpellEngine(step.sequence.take(DifficultyAdapter.spellLength(difficulty, step.sequence.size)))
            is Step.Narration -> Unit
            is Step.Reward -> if (step.charm) sound(SoundCue.REWARD) else sound(SoundCue.COMPLETE)
        }
        _state.value = s.copy(
            stepIndex = stepIndex,
            phase = Phase.STEP,
            hintLevel = HintLevel.NONE,
            hintIndex = null,
            companionMessage = null,
        )
        speak(VoiceLines.stepLine(step))
        if (step.isActivity()) {
            startHints()
            refresh()
            if (step is Step.Spell) showSpell(leadIn = spellLeadInMillis)
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
                sound(if (r.complete) SoundCue.COMPLETE else SoundCue.FOUND)
                val next = engine.nextTarget?.let { noun(case, it) }
                if (r.complete || next == null) Phrases.foundAll() else Phrases.found(noun(case, r.item), next)
            }
            is HiddenObjectEngine.Result.AlreadyFound -> Phrases.alreadyFound(noun(case, r.item))
            is HiddenObjectEngine.Result.NotTarget -> {
                wrongChoices++
                val wanted = engine.nextTarget?.let { noun(case, it) } ?: noun(case, r.item)
                Phrases.lovely(noun(case, r.item), wanted)
            }
        }
        if (engine.isComplete) stopActivityJobs()
        refresh()
    }

    // ---- Memory match ----

    fun onMemoryCardSelected(index: Int) {
        val engine = memoryEngine ?: return
        when (val r = engine.flip(index)) {
            MemoryMatchEngine.Result.Ignored -> return
            is MemoryMatchEngine.Result.FirstFlipped -> {
                sound(SoundCue.FLIP)
                feedback = Phrases.findTwin()
            }
            is MemoryMatchEngine.Result.Matched -> {
                markProgress()
                sound(if (r.complete) SoundCue.COMPLETE else SoundCue.FOUND)
                feedback = if (r.complete) Phrases.allPairs() else Phrases.aPair()
                if (r.complete) stopActivityJobs()
            }
            is MemoryMatchEngine.Result.Mismatched -> {
                sound(SoundCue.FLIP)
                wrongChoices++
                feedback = Phrases.notAPair()
                viewModelScope.launch {
                    delay(mismatchRevealMillis)
                    engine.hideMismatch()
                    refresh()
                }
            }
        }
        refresh()
    }

    // ---- Choice (recall / riddle) ----

    fun onChoiceSelected(index: Int) {
        val s = playing() ?: return
        val engine = choiceEngine ?: return
        val step = s.step as? Step.Choice ?: return
        when (val r = engine.select(index)) {
            ChoiceEngine.Result.Ignored -> return
            ChoiceEngine.Result.Correct -> {
                markProgress()
                sound(SoundCue.COMPLETE)
                feedback = Phrases.correct(step.kind, noun(s.case, step.answer))
                stopActivityJobs()
            }
            is ChoiceEngine.Result.NotThisOne -> {
                wrongChoices++
                feedback = Phrases.notThis(noun(s.case, r.item))
            }
        }
        refresh()
    }

    // ---- Sorting ----

    fun onSortingBinSelected(index: Int) {
        val s = playing() ?: return
        val engine = sortingEngine ?: return
        val step = s.step as? Step.Sorting ?: return
        fun place(id: String) = Place.of(step.categories.first { it.id == id })
        when (val r = engine.place(index)) {
            SortingEngine.Result.Ignored -> return
            is SortingEngine.Result.Correct -> {
                markProgress()
                sound(if (r.complete) SoundCue.COMPLETE else SoundCue.FOUND)
                val placedIn = place(step.items.first { it.item == r.item }.category)
                feedback = if (r.complete) Phrases.allSorted() else Phrases.sortedInto(noun(s.case, r.item), placedIn)
                if (r.complete) stopActivityJobs()
            }
            is SortingEngine.Result.Wrong -> {
                wrongChoices++
                feedback = Phrases.wouldWeKeep(noun(s.case, r.item), place(r.chosenCategory))
            }
        }
        refresh()
    }

    // ---- Spell ----

    fun onSpellSymbolSelected(symbolIndex: Int) {
        val engine = spellEngine ?: return
        if (spellShowing || engine.isComplete) return
        val symbol = SpellSymbol.entries[symbolIndex]
        when (val r = engine.input(symbol.id)) {
            SpellEngine.Result.Ignored -> return
            is SpellEngine.Result.Correct -> {
                markProgress()
                SoundCue.noteFor(symbol.id)?.let(::sound)
                feedback = if (r.complete) Phrases.spellWorked() else Phrases.symbolYes(symbol)
                if (r.complete) {
                    sound(SoundCue.COMPLETE)
                    stopActivityJobs()
                }
            }
            is SpellEngine.Result.ShowAgain -> {
                wrongChoices++
                feedback = Phrases.watchAgain()
                refresh()
                showSpell(leadIn = 1_200)
                return
            }
        }
        refresh()
    }

    /** "Show me again": replays the sequence from the start (not counted as a mistake). */
    fun onSpellShowAgain() {
        val engine = spellEngine ?: return
        if (spellShowing || engine.isComplete) return
        engine.restart()
        feedback = null
        showSpell(leadIn = 300)
    }

    private fun showSpell(leadIn: Long) {
        val engine = spellEngine ?: return
        spellJob?.cancel()
        spellShowing = true
        spellShowingSymbol = null
        refresh()
        spellJob = viewModelScope.launch {
            delay(leadIn)
            for (id in engine.sequence) {
                val symbol = SpellSymbol.byId(id) ?: continue
                spellShowingSymbol = symbol.ordinal
                feedback = Phrases.showing(symbol)
                SoundCue.noteFor(id)?.let(::sound)
                refresh()
                delay(spellSymbolMillis)
                spellShowingSymbol = null
                refresh()
                delay(spellGapMillis)
            }
            spellShowing = false
            feedback = Phrases.nowYouTry()
            lastProgressAt = clock() // hint timing starts once the player can act
            refresh()
        }
    }

    // ---- Hints ----

    fun onHelpMe() {
        val s = playing() ?: return
        if (s.phase != Phase.STEP || s.activityComplete || spellShowing) return
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

    /** Recomputes the activity UI and hint state from the engines, and speaks new companion messages. */
    private fun refresh() {
        val s = playing() ?: return
        val case = s.case
        val step = s.step
        val complete = hiddenEngine?.isComplete ?: memoryEngine?.isComplete ?: choiceEngine?.isComplete
            ?: sortingEngine?.isComplete ?: spellEngine?.isComplete ?: true
        val level = if (complete || spellShowing) {
            HintLevel.NONE
        } else {
            HintLadder.level(clock() - lastProgressAt, settings.hintDelaySec, helpPresses, wrongChoices)
        }
        if (level > maxHintLevel) maxHintLevel = level
        // A new, higher hint replaces older feedback so the newest help is what is on screen.
        if (level > s.hintLevel) feedback = null

        val hint: Hint = if (level == HintLevel.NONE) Hint.NONE else hintFor(case, step, level)
        val idle = idleText(case, step)
        val message = feedback ?: hint.text ?: idle

        // Speak feedback and hints once each (the 1-second ticker must not repeat them). Idle text is the
        // step's own goal, which was already spoken when the step started.
        val spoken = feedback ?: hint.text
        if (spoken != null && spoken != lastSpokenCompanion) {
            lastSpokenCompanion = spoken
            // Content hints use their authored id; play-time phrases use a stable id derived from the text.
            speak(if (feedback == null && hint.line != null) hint.line else SpokenLine(Phrases.idFor(spoken), spoken), companion = true)
        }

        _state.value = s.copy(
            hiddenObject = hiddenEngine?.let { hiddenUi(case) },
            memory = memoryEngine?.let { memoryUi(case) },
            choice = choiceEngine?.let { choiceUi(case, step as Step.Choice) },
            sorting = sortingEngine?.let { sortingUi(case, step as Step.Sorting) },
            spell = spellEngine?.let { spellUi(it) },
            hintLevel = level,
            hintIndex = if (level >= HintLevel.HIGHLIGHT) hint.index else null,
            companionMessage = message,
        )
    }

    private data class Hint(val text: String?, val index: Int?, val line: SpokenLine? = null) {
        companion object {
            val NONE = Hint(null, null)
        }
    }

    private fun hintFor(case: CaseContent, step: Step, level: HintLevel): Hint {
        val glow = Phrases.glowing()
        val guide = Phrases.hereItIs()
        return when (step) {
            is Step.HiddenObject -> {
                val engine = hiddenEngine ?: return Hint.NONE
                val target = engine.nextTarget ?: return Hint.NONE
                val targetIndex = step.targets.indexOf(target)
                val verbal = VoiceLines.hint(step, targetIndex)
                val text = when (level) {
                    HintLevel.REMIND -> Phrases.rememberLooking(noun(case, target))
                    HintLevel.VERBAL -> verbal?.text
                    HintLevel.HIGHLIGHT -> glow
                    else -> guide
                }
                Hint(text, engine.cellOf(target), if (level == HintLevel.VERBAL) verbal else null)
            }
            is Step.MemoryMatch -> {
                val engine = memoryEngine ?: return Hint.NONE
                val pending = engine.pendingIndex?.let { noun(case, engine.cards[it].item) }
                val text = when (level) {
                    HintLevel.REMIND -> Phrases.lookingForPairs()
                    HintLevel.VERBAL -> if (pending != null) Phrases.whereAnother(pending) else Phrases.turnACard()
                    HintLevel.HIGHLIGHT -> Phrases.tryGlowingCard()
                    else -> guide
                }
                Hint(text, engine.suggestedIndex())
            }
            is Step.Choice -> {
                val engine = choiceEngine ?: return Hint.NONE
                val text = when (level) {
                    HintLevel.REMIND -> step.question
                    HintLevel.VERBAL -> step.hint
                    HintLevel.HIGHLIGHT -> glow
                    else -> guide
                }
                Hint(text, engine.answerIndex, if (level == HintLevel.VERBAL) VoiceLines.hint(step, 0) else null)
            }
            is Step.Sorting -> {
                val engine = sortingEngine ?: return Hint.NONE
                val item = engine.current?.let { noun(case, it.item) } ?: return Hint.NONE
                val text = when (level) {
                    HintLevel.REMIND -> Phrases.whereBelongs(item)
                    HintLevel.VERBAL -> Phrases.thinkWhere(item)
                    HintLevel.HIGHLIGHT -> Phrases.goesWhereGlowing()
                    else -> guide
                }
                Hint(text, engine.correctCategoryIndex)
            }
            is Step.Spell -> {
                val engine = spellEngine ?: return Hint.NONE
                val next = engine.expected?.let(SpellSymbol::byId) ?: return Hint.NONE
                val text = when (level) {
                    HintLevel.REMIND -> Phrases.sameOrder()
                    HintLevel.VERBAL -> Phrases.nextIs(next)
                    HintLevel.HIGHLIGHT -> Phrases.tryGlowingSymbol()
                    else -> guide
                }
                Hint(text, next.ordinal)
            }
            is Step.Narration, is Step.Reward -> Hint.NONE
        }
    }

    /** What Marmalade says when there is no news: what is still to do (an always-visible memory aid). */
    private fun idleText(case: CaseContent, step: Step): String? = when (step) {
        is Step.HiddenObject -> {
            val engine = hiddenEngine
            val remaining = engine?.targets?.filterNot { it in engine.foundItems }.orEmpty()
            if (remaining.isEmpty()) null else Phrases.pleaseFind(remaining.map { noun(case, it) })
        }
        is Step.MemoryMatch -> step.goal
        is Step.Choice -> Phrases.takeYourTime()
        is Step.Sorting -> sortingEngine?.current?.let { Phrases.whereBelongs(noun(case, it.item)) }
        is Step.Spell -> if (spellShowing) Phrases.watchTheMagic() else Phrases.nowYouTry()
        else -> null
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

    private fun choiceUi(case: CaseContent, step: Step.Choice): ChoiceUi {
        val e = choiceEngine!!
        return ChoiceUi(step.kind, step.question, e.options.map { item(case, it) }, e.triedOptions.toSet(), e.answer, e.isComplete)
    }

    private fun sortingUi(case: CaseContent, step: Step.Sorting): SortingUi {
        val e = sortingEngine!!
        return SortingUi(
            current = e.current?.let { item(case, it.item) },
            bins = step.categories.map { c -> SortingBinUi(c, e.placedItems.filter { it.category == c.id }.map { item(case, it.item) }) },
            placedCount = e.placedItems.size,
            total = e.items.size,
            complete = e.isComplete,
        )
    }

    private fun spellUi(e: SpellEngine) = SpellUi(
        symbols = SpellSymbol.entries,
        length = e.sequence.size,
        position = e.position,
        showingSymbol = spellShowingSymbol,
        showing = spellShowing,
        complete = e.isComplete,
    )

    private fun item(case: CaseContent, id: String): Item = case.item(id) ?: Item(id, id, "❔")

    private fun noun(case: CaseContent, id: String): Noun = Noun.of(item(case, id))

    override fun onCleared() {
        stopActivityJobs()
    }
}
