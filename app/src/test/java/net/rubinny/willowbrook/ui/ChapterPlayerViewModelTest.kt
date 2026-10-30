package net.rubinny.willowbrook.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.data.progress.ProgressRepository
import net.rubinny.willowbrook.data.settings.SettingsRepository
import net.rubinny.willowbrook.game.engine.HintLevel
import net.rubinny.willowbrook.game.model.SpellSymbol
import net.rubinny.willowbrook.game.model.Step
import net.rubinny.willowbrook.newDataStore
import net.rubinny.willowbrook.ui.screens.play.ChapterPlayerViewModel
import net.rubinny.willowbrook.ui.screens.play.Phase
import net.rubinny.willowbrook.ui.screens.play.PlayerUiState
import kotlinx.coroutines.launch
import net.rubinny.willowbrook.audio.SoundCue
import net.rubinny.willowbrook.game.engine.SpokenLine
import net.rubinny.willowbrook.ui.screens.play.PlayerEffect
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ChapterPlayerViewModelTest {
    @get:Rule val tmp = TemporaryFolder()

    private val scheduler = TestCoroutineScheduler()
    private var now = 0L
    private val content = ContentRepository { path -> File("src/main/assets/$path").inputStream() }
    private lateinit var progress: ProgressRepository
    private lateinit var settings: SettingsRepository

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher(scheduler))
        val store = tmp.newDataStore()
        progress = ProgressRepository(store)
        settings = SettingsRepository(store)
    }

    /** Every line spoken in any scenario must be in the recording script (so it has a recorded voice). */
    @After fun tearDown() {
        Dispatchers.resetMain()
        val case = runBlocking { content.load("lantern") }
        val scripted = net.rubinny.willowbrook.game.engine.VoiceLines.script(case).map { it.second.id }.toSet()
        val missing = spoken().filter { it.id !in scripted }
        assertTrue("Spoken lines without a recording: $missing", missing.isEmpty())
    }

    private fun vm(replay: Int? = null) = ChapterPlayerViewModel(
        profileId = "p1",
        replayChapter = replay,
        content = content,
        progressRepo = progress,
        settingsRepo = settings,
        clock = { now },
        mismatchRevealMillis = 2_500,
    )

    private suspend fun ChapterPlayerViewModel.await(predicate: (PlayerUiState.Playing) -> Boolean): PlayerUiState.Playing =
        withTimeout(10_000) { state.first { it is PlayerUiState.Playing && predicate(it) } as PlayerUiState.Playing }

    private fun ChapterPlayerViewModel.playing() = state.value as PlayerUiState.Playing

    /** Moves virtual time: the hint ticker runs once per second. */
    private fun advance(millis: Long) {
        now += millis
        scheduler.advanceTimeBy(millis)
        scheduler.runCurrent()
    }

    /** Completes the current step the way a player would, choosing correct answers. */
    private fun ChapterPlayerViewModel.solveCurrentStep() {
        val s = playing()
        when (s.step) {
            is Step.HiddenObject -> {
                while (!playing().hiddenObject!!.complete) {
                    val ui = playing().hiddenObject!!
                    val target = ui.targets.first { it.id !in ui.found }
                    onHiddenCellSelected(ui.cells.indexOfFirst { it.id == target.id })
                }
            }
            is Step.Choice -> onChoiceSelected(playing().choice!!.options.indexOfFirst { it.id == playing().choice!!.answer })
            is Step.Sorting -> while (!playing().sorting!!.complete) {
                val step = playing().step as Step.Sorting
                val current = playing().sorting!!.current!!
                val category = step.items.first { it.item == current.id }.category
                onSortingBinSelected(step.categories.indexOfFirst { it.id == category })
            }
            is Step.Spell -> {
                advance(60_000) // let the magic be shown
                val step = playing().step as Step.Spell
                step.sequence.take(playing().spell!!.length).forEach { id ->
                    onSpellSymbolSelected(SpellSymbol.byId(id)!!.ordinal)
                }
            }
            is Step.MemoryMatch -> {
                while (!playing().memory!!.complete) {
                    val cards = playing().memory!!.cards
                    val first = cards.indexOfFirst { !it.matched }
                    val partner = cards.indices.first { it != first && !cards[it].matched && cards[it].item.id == cards[first].item.id }
                    onMemoryCardSelected(first)
                    onMemoryCardSelected(partner)
                }
            }
            else -> Unit
        }
        onStepContinue()
    }

    @Test fun `new player plays chapter 0 to the end and progress is saved`() = runBlocking {
        val vm = vm()
        val intro = vm.await { it.phase == Phase.CHAPTER_INTRO }
        assertEquals(0, intro.chapterIndex)
        vm.onChapterIntroContinue()
        while (vm.playing().phase == Phase.STEP) vm.solveCurrentStep()
        assertEquals(Phase.CHAPTER_COMPLETE, vm.playing().phase)

        val saved = withTimeout(10_000) { progress.progress("p1", "lantern").first { it.chapterIndex == 1 } }
        assertEquals(listOf("ch0_cottage"), saved.completedChapters)
        assertEquals(0, saved.stepIndex)
        assertEquals(1, saved.recentHintLevels.size) // one activity in chapter 0
    }

    @Test fun `returning player sees a recap, then resumes at the saved step`() = runBlocking {
        startAt("ch1_tidy_kitchen")
        val vm = vm()
        val recap = vm.await { it.phase == Phase.RECAP }
        assertTrue(recap.recapText!!.contains("bakery"))
        vm.onRecapContinue()
        val step = vm.await { it.phase == Phase.STEP }
        assertTrue(step.step is Step.HiddenObject)
        assertEquals(positionOf("ch1_tidy_kitchen").second, step.stepIndex)
    }

    @Test fun `wrong pick gets kind feedback and nothing is lost`() = runBlocking {
        startAt("ch1_tidy_kitchen")
        val vm = vm()
        vm.await { it.phase == Phase.RECAP }
        vm.onRecapContinue()
        val s = vm.await { it.hiddenObject != null }
        val ui = s.hiddenObject!!
        val wrong = ui.cells.indexOfFirst { c -> ui.targets.none { it.id == c.id } }
        vm.onHiddenCellSelected(wrong)
        val msg = vm.playing().companionMessage!!
        assertTrue(msg, msg.startsWith("That's a lovely"))
        assertTrue(msg, msg.contains("honey pot"))
        assertTrue(vm.playing().hiddenObject!!.found.isEmpty())
    }

    @Test fun `hints climb with time, help me and finally guide to the answer`() = runBlocking {
        startAt("ch1_tidy_kitchen")
        val vm = vm()
        vm.await { it.phase == Phase.RECAP }
        vm.onRecapContinue()
        vm.await { it.hiddenObject != null }
        assertEquals(HintLevel.NONE, vm.playing().hintLevel)

        advance(20_000) // default hint delay is 20 s
        assertEquals(HintLevel.REMIND, vm.playing().hintLevel)
        assertTrue(vm.playing().companionMessage!!.contains("honey pot"))

        vm.onHelpMe()
        assertEquals(HintLevel.VERBAL, vm.playing().hintLevel)
        assertEquals("The honey pot is golden and round.", vm.playing().companionMessage)
        assertNull(vm.playing().hintIndex)

        vm.onHelpMe()
        assertEquals(HintLevel.HIGHLIGHT, vm.playing().hintLevel)
        val ui = vm.playing().hiddenObject!!
        assertEquals("honey", ui.cells[vm.playing().hintIndex!!].id)

        vm.onHelpMe()
        assertEquals(HintLevel.GUIDE, vm.playing().hintLevel)
        vm.onHiddenCellSelected(vm.playing().hintIndex!!)
        // Progress resets the ladder for the next object.
        assertEquals(HintLevel.NONE, vm.playing().hintLevel)
        assertEquals(setOf("honey"), vm.playing().hiddenObject!!.found)
    }

    @Test fun `memory mismatch is shown, then turned back after a pause`() = runBlocking {
        startAt("ch1_recipe_pairs")
        val vm = vm()
        vm.await { it.phase == Phase.RECAP }
        vm.onRecapContinue()
        val s = vm.await { it.memory != null }
        assertEquals(4, s.memory!!.cards.size) // AUTO starts gentle: 2 pairs
        val cards = s.memory.cards
        val wrong = cards.indices.first { cards[it].item.id != cards[0].item.id }
        vm.onMemoryCardSelected(0)
        vm.onMemoryCardSelected(wrong)
        assertTrue(vm.playing().memory!!.locked)
        assertTrue(vm.playing().memory!!.cards[0].faceUp)
        advance(2_500)
        assertTrue(!vm.playing().memory!!.locked)
        assertTrue(!vm.playing().memory!!.cards[0].faceUp)
    }

    @Test fun `replay does not change saved progress`() = runBlocking {
        progress.save("p1", progress.get("p1", "lantern").copy(chapterIndex = 2, completedChapters = listOf("ch0_cottage", "ch1_bakery")))
        val before = progress.get("p1", "lantern")
        val vm = vm(replay = 0)
        val intro = vm.await { it.phase == Phase.CHAPTER_INTRO }
        assertTrue(intro.replay)
        vm.onChapterIntroContinue()
        while (vm.playing().phase == Phase.STEP) vm.solveCurrentStep()
        assertEquals(Phase.CHAPTER_COMPLETE, vm.playing().phase)
        assertEquals(false, vm.onNextChapter()) // replays end at home
        assertEquals(before.chapterIndex, progress.get("p1", "lantern").chapterIndex)
        assertEquals(before.recentHintLevels, progress.get("p1", "lantern").recentHintLevels)
    }

    private fun startAt(chapter: Int, step: Int) = runBlocking {
        progress.save("p1", progress.get("p1", "lantern").copy(chapterIndex = chapter, stepIndex = step))
    }

    /** Saved position of a step by its id, so tests don't break when story lines are added. */
    private fun positionOf(stepId: String): Pair<Int, Int> {
        runBlocking { content.load("lantern") }.chapters.forEachIndexed { c, chapter ->
            val i = chapter.steps.indexOfFirst { it.id == stepId }
            if (i >= 0) return c to i
        }
        error("No step $stepId")
    }

    private fun startAt(stepId: String) = positionOf(stepId).let { (c, i) -> startAt(c, i) }

    /** Starts at a saved step, passes the recap, and records every effect from then on. */
    private fun ChapterPlayerViewModel.openSavedStep(effects: MutableList<PlayerEffect>) = runBlocking {
        await { it.phase == Phase.RECAP }
        backgroundScope.launch { this@openSavedStep.effects.collect { effects += it } }
        onRecapContinue()
        await { it.phase == Phase.STEP }
    }

    private val effects = mutableListOf<PlayerEffect>()
    private val backgroundScope = kotlinx.coroutines.CoroutineScope(UnconfinedTestDispatcher(scheduler))

    private fun spoken() = effects.filterIsInstance<PlayerEffect.Speak>().map { it.line }
    private fun sounds() = effects.filterIsInstance<PlayerEffect.Sound>().map { it.cue }

    @Test fun `entering a step speaks its line with the recorded-voice id`() {
        startAt("ch1_tidy_kitchen")
        val vm = vm()
        vm.openSavedStep(effects)
        assertEquals(SpokenLine("ch1_tidy_kitchen_goal", "Find the honey pot, the loaf of bread and the strawberries."), spoken().last())
        assertTrue(!effects.filterIsInstance<PlayerEffect.Speak>().last().companion) // story line: always read
    }

    @Test fun `narration off means no speech, and captions cannot be hidden`() = runBlocking {
        settings.update { it.copy(narrationEnabled = false, captionsEnabled = false) }
        startAt("ch1_tidy_kitchen")
        val vm = vm()
        vm.openSavedStep(effects)
        assertTrue(spoken().isEmpty())
        assertTrue(vm.playing().showStoryText)
        assertTrue(!vm.playing().canListen)
    }

    @Test fun `captions off with narration on hides story text`() = runBlocking {
        settings.update { it.copy(captionsEnabled = false) }
        startAt("ch1_bramble_hello")
        val vm = vm()
        vm.openSavedStep(effects)
        assertTrue(!vm.playing().showStoryText)
        vm.onListenAgain()
        assertEquals("ch1_bramble_hello", spoken().last().id)
    }

    @Test fun `found plays a chime, a wrong pick plays nothing and is spoken kindly`() {
        startAt("ch1_tidy_kitchen")
        val vm = vm()
        vm.openSavedStep(effects)
        val ui = vm.playing().hiddenObject!!
        val wrong = ui.cells.indexOfFirst { c -> ui.targets.none { it.id == c.id } }
        vm.onHiddenCellSelected(wrong)
        assertTrue(sounds().isEmpty())
        assertTrue(spoken().last().text.startsWith("That's a lovely"))
        assertEquals(net.rubinny.willowbrook.game.engine.Phrases.idFor(spoken().last().text), spoken().last().id) // recorded play-time phrase
        assertTrue(effects.filterIsInstance<PlayerEffect.Speak>().last().companion) // TalkBack reads it itself
        vm.onHiddenCellSelected(ui.cells.indexOfFirst { it.id == "honey" })
        assertEquals(listOf(SoundCue.FOUND), sounds())
    }

    @Test fun `hints are spoken once, not every second`() {
        startAt("ch1_tidy_kitchen")
        val vm = vm()
        vm.openSavedStep(effects)
        // Count only Marmalade's lines: the story lines (recap, goal) arrive from another thread at slightly
        // different moments, so including them would make the count racy.
        fun companionLines() = effects.filterIsInstance<PlayerEffect.Speak>().filter { it.companion }.map { it.line }
        assertEquals(0, companionLines().size)
        advance(20_000)
        advance(5_000)
        assertEquals(1, companionLines().size)
        assertEquals("Remember, we're looking for the honey pot.", companionLines().last().text)
        vm.onHelpMe()
        assertEquals(SpokenLine("ch1_tidy_kitchen_hint1", "The honey pot is golden and round."), companionLines().last())
    }

    @Test fun `recall question - wrong option is dimmed, right option completes`() {
        startAt("ch1_recall_fruit")
        val vm = vm()
        vm.openSavedStep(effects)
        val ui = vm.playing().choice!!
        assertEquals(2, ui.options.size) // gentle: answer + one other
        assertTrue(ui.options.any { it.id == "pear" })
        val wrong = ui.options.indexOfFirst { it.id != "pear" }
        vm.onChoiceSelected(wrong)
        assertEquals(setOf(ui.options[wrong].id), vm.playing().choice!!.tried)
        vm.onChoiceSelected(ui.options.indexOfFirst { it.id == "pear" })
        assertTrue(vm.playing().choice!!.complete)
        assertTrue(vm.playing().companionMessage!!.contains("pear"))
        assertEquals(SoundCue.COMPLETE, sounds().last())
    }

    @Test fun `sorting - gentle uses 4 items and a wrong bin gets a kind question`() {
        startAt("ch2_sort_basket")
        val vm = vm()
        vm.openSavedStep(effects)
        val ui = vm.playing().sorting!!
        assertEquals(4, ui.total)
        val step = vm.playing().step as Step.Sorting
        val current = ui.current!!
        val right = step.items.first { it.item == current.id }.category
        vm.onSortingBinSelected(step.categories.indexOfFirst { it.id != right })
        assertTrue(vm.playing().companionMessage!!, vm.playing().companionMessage!!.startsWith("I wonder, would we keep"))
        assertEquals(current, vm.playing().sorting!!.current)
        vm.onSortingBinSelected(step.categories.indexOfFirst { it.id == right })
        assertEquals(1, vm.playing().sorting!!.placedCount)
    }

    @Test fun `spell - the magic is shown with notes, input waits, mistakes show it again`() {
        startAt("ch2_bloom_spell")
        val vm = vm()
        vm.openSavedStep(effects)
        val spell = vm.playing().spell!!
        assertTrue(spell.showing)
        assertEquals(2, spell.length) // gentle
        vm.onSpellSymbolSelected(SpellSymbol.FLOWER.ordinal) // ignored while showing
        assertEquals(0, vm.playing().spell!!.position)

        advance(3_000) // lead-in
        assertEquals(SpellSymbol.FLOWER.ordinal, vm.playing().spell!!.showingSymbol)
        assertEquals(SoundCue.NOTE_FLOWER, sounds().last())
        advance(1_600)
        assertEquals(SpellSymbol.LEAF.ordinal, vm.playing().spell!!.showingSymbol)
        advance(1_600)
        assertTrue(!vm.playing().spell!!.showing)
        assertEquals("Now you try. Press the symbols in the same order.", vm.playing().companionMessage)

        vm.onSpellSymbolSelected(SpellSymbol.MOON.ordinal) // mistake
        assertTrue(vm.playing().spell!!.showing)
        advance(1_200 + 2 * 1_600)
        vm.onSpellSymbolSelected(SpellSymbol.FLOWER.ordinal)
        vm.onSpellSymbolSelected(SpellSymbol.LEAF.ordinal)
        assertTrue(vm.playing().spell!!.complete)
        assertEquals(SoundCue.COMPLETE, sounds().last())
    }

    @Test fun `chapter 2 can be played to the end`() = runBlocking {
        startAt(2, 0)
        val vm = vm()
        vm.await { it.phase == Phase.RECAP }
        vm.onRecapContinue()
        vm.await { it.phase == Phase.CHAPTER_INTRO }
        vm.onChapterIntroContinue()
        while (vm.playing().phase == Phase.STEP) vm.solveCurrentStep()
        assertEquals(Phase.CHAPTER_COMPLETE, vm.playing().phase)
        val saved = withTimeout(10_000) { progress.progress("p1", "lantern").first { it.chapterIndex == 3 } }
        assertTrue("ch2_garden" in saved.completedChapters)
    }

    @Test fun `chapters 3 and 4 can be played to the end, and the case is solved`() = runBlocking {
        startAt(3, 0)
        val vm = vm()
        vm.await { it.phase == Phase.RECAP }
        vm.onRecapContinue()
        vm.await { it.phase == Phase.CHAPTER_INTRO }
        vm.onChapterIntroContinue()
        while (vm.playing().phase == Phase.STEP) vm.solveCurrentStep()
        assertEquals(Phase.CHAPTER_COMPLETE, vm.playing().phase)
        assertTrue(vm.onNextChapter())
        assertEquals(4, vm.playing().chapterIndex)
        vm.onChapterIntroContinue()
        while (vm.playing().phase == Phase.STEP) vm.solveCurrentStep()
        assertEquals(Phase.CHAPTER_COMPLETE, vm.playing().phase)
        assertTrue(!vm.onNextChapter()) // last chapter
        val saved = withTimeout(10_000) { progress.progress("p1", "lantern").first { it.chapterIndex == 5 } }
        assertTrue(saved.completedChapters.containsAll(listOf("ch3_library", "ch4_square")))
    }

    @Test fun `let's chat reads the question aloud`() = runBlocking {
        startAt(1, 0)
        val vm = vm()
        vm.await { it.phase == Phase.RECAP }
        backgroundScope.launch { vm.effects.collect { effects += it } }
        vm.onChat(0)
        assertEquals("ch1_bakery_chat1", spoken().last().id)
        vm.onChat(1)
        assertEquals("ch1_bakery_chat2", spoken().last().id)
    }

    @Test fun `finished case shows the finished screen`() = runBlocking {
        progress.save("p1", progress.get("p1", "lantern").copy(chapterIndex = 99))
        val vm = vm()
        backgroundScope.launch { vm.effects.collect { effects += it } }
        assertNotNull(vm.await { it.phase == Phase.CASE_FINISHED })
    }
}
