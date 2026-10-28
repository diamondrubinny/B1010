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
import net.rubinny.willowbrook.game.model.Step
import net.rubinny.willowbrook.newDataStore
import net.rubinny.willowbrook.ui.screens.play.ChapterPlayerViewModel
import net.rubinny.willowbrook.ui.screens.play.Phase
import net.rubinny.willowbrook.ui.screens.play.PlayerUiState
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

    @After fun tearDown() = Dispatchers.resetMain()

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
        progress.save("p1", progress.get("p1", "lantern").copy(chapterIndex = 1, stepIndex = 2))
        val vm = vm()
        val recap = vm.await { it.phase == Phase.RECAP }
        assertTrue(recap.recapText!!.contains("bakery"))
        vm.onRecapContinue()
        val step = vm.await { it.phase == Phase.STEP }
        assertTrue(step.step is Step.HiddenObject)
        assertEquals(2, step.stepIndex)
    }

    @Test fun `wrong pick gets kind feedback and nothing is lost`() = runBlocking {
        progress.save("p1", progress.get("p1", "lantern").copy(chapterIndex = 1, stepIndex = 2))
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
        progress.save("p1", progress.get("p1", "lantern").copy(chapterIndex = 1, stepIndex = 2))
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
        progress.save("p1", progress.get("p1", "lantern").copy(chapterIndex = 1, stepIndex = 4))
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

    @Test fun `finished case shows the finished screen`() = runBlocking {
        progress.save("p1", progress.get("p1", "lantern").copy(chapterIndex = 99))
        val vm = vm()
        assertNotNull(vm.await { it.phase == Phase.CASE_FINISHED })
    }
}
