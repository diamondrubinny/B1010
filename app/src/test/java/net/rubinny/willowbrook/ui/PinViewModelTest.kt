package net.rubinny.willowbrook.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import net.rubinny.willowbrook.data.settings.SettingsRepository
import net.rubinny.willowbrook.newDataStore
import net.rubinny.willowbrook.security.PinHasher
import net.rubinny.willowbrook.security.PinLockout
import net.rubinny.willowbrook.ui.screens.settings.PinMode
import net.rubinny.willowbrook.ui.screens.settings.PinUiState
import net.rubinny.willowbrook.ui.screens.settings.PinViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PinViewModelTest {
    @get:Rule val tmp = TemporaryFolder()

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private suspend fun PinViewModel.await(predicate: (PinUiState) -> Boolean): PinUiState =
        withTimeout(10_000) { state.first(predicate) }

    private fun PinViewModel.type(pin: String) = pin.forEach(::onDigit)

    @Test fun `first use creates a pin after typing it twice`() = runBlocking {
        val repo = SettingsRepository(tmp.newDataStore())
        val vm = PinViewModel(repo)
        vm.await { it.mode == PinMode.CREATE }
        vm.type("1357")
        vm.await { it.mode == PinMode.CONFIRM }
        vm.type("1357")
        vm.await { it.unlocked }
        assertTrue(PinHasher.verify("1357", repo.pinState.first().hash!!))
    }

    @Test fun `mismatched confirmation restarts creation`() = runBlocking {
        val vm = PinViewModel(SettingsRepository(tmp.newDataStore()))
        vm.await { it.mode == PinMode.CREATE }
        vm.type("1111")
        vm.await { it.mode == PinMode.CONFIRM }
        vm.type("2222")
        val s = vm.await { it.mode == PinMode.CREATE && it.message != null }
        assertEquals("", s.entered)
    }

    @Test fun `correct pin unlocks, wrong pin counts failures and locks`() = runBlocking {
        val repo = SettingsRepository(tmp.newDataStore())
        repo.setPinHash(PinHasher.hash("4321"))
        var now = 1_000_000L
        val vm = PinViewModel(repo, clock = { now })
        vm.await { it.mode == PinMode.VERIFY }

        repeat(PinLockout.FREE_ATTEMPTS) { attempt ->
            vm.type("0000")
            withTimeout(10_000) { repo.pinState.first { it.failedAttempts == attempt + 1 } }
            vm.await { !it.busy } // the ViewModel ignores digits while it is still checking
        }
        val locked = vm.await { it.lockedSeconds > 0 }
        assertTrue(locked.lockedSeconds in 1..30)

        // While locked, digits are ignored.
        vm.onDigit('4')
        assertEquals("", vm.state.value.entered)
    }

    @Test fun `verify success unlocks and clears failures`() = runBlocking {
        val repo = SettingsRepository(tmp.newDataStore())
        repo.setPinHash(PinHasher.hash("4321"))
        repo.recordPinFailure(2, 0L)
        val vm = PinViewModel(repo)
        vm.await { it.mode == PinMode.VERIFY }
        vm.type("4321")
        vm.await { it.unlocked }
        assertEquals(0, repo.pinState.first().failedAttempts)
    }
}
