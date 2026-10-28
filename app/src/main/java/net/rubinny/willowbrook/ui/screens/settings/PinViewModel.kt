package net.rubinny.willowbrook.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.rubinny.willowbrook.data.settings.SettingsRepository
import net.rubinny.willowbrook.security.PinHasher
import net.rubinny.willowbrook.security.PinLockout

enum class PinMode { LOADING, VERIFY, CREATE, CONFIRM }

data class PinUiState(
    val mode: PinMode = PinMode.LOADING,
    val entered: String = "",
    val message: String? = null,
    val busy: Boolean = false,
    val lockedSeconds: Int = 0,
    val unlocked: Boolean = false,
)

/**
 * Caregiver PIN gate. First use (or [forceCreate]) asks for a new PIN twice;
 * afterwards it verifies, with a 30-second lockout after 5 wrong attempts.
 */
class PinViewModel(
    private val settings: SettingsRepository,
    private val forceCreate: Boolean = false,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val _state = MutableStateFlow(PinUiState())
    val state: StateFlow<PinUiState> = _state.asStateFlow()
    private var firstEntry: String? = null

    init {
        viewModelScope.launch {
            val pin = settings.pinState.first()
            if (pin.hash == null || forceCreate) {
                _state.update { it.copy(mode = PinMode.CREATE) }
            } else {
                _state.update { it.copy(mode = PinMode.VERIFY) }
                startCountdownIfLocked(pin.lockedUntilMillis)
            }
        }
    }

    fun onDigit(d: Char) {
        val s = _state.value
        if (s.busy || s.lockedSeconds > 0 || s.mode == PinMode.LOADING || s.entered.length >= PinHasher.PIN_LENGTH) return
        val next = s.entered + d
        _state.update { it.copy(entered = next, message = null) }
        if (next.length == PinHasher.PIN_LENGTH) submit(next)
    }

    fun onDelete() = _state.update { it.copy(entered = it.entered.dropLast(1)) }

    private fun submit(pin: String) {
        when (_state.value.mode) {
            PinMode.CREATE -> {
                firstEntry = pin
                _state.update { it.copy(mode = PinMode.CONFIRM, entered = "") }
            }
            PinMode.CONFIRM -> if (pin == firstEntry) {
                _state.update { it.copy(busy = true) }
                viewModelScope.launch {
                    val hash = withContext(Dispatchers.Default) { PinHasher.hash(pin) }
                    settings.setPinHash(hash)
                    _state.update { it.copy(busy = false, unlocked = true) }
                }
            } else {
                firstEntry = null
                _state.update {
                    it.copy(mode = PinMode.CREATE, entered = "", message = "The two PINs were different. Let's try again.")
                }
            }
            PinMode.VERIFY -> verify(pin)
            PinMode.LOADING -> Unit
        }
    }

    private fun verify(pin: String) {
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val stored = settings.pinState.first()
            val ok = stored.hash != null && withContext(Dispatchers.Default) { PinHasher.verify(pin, stored.hash) }
            if (ok) {
                settings.clearPinFailures()
                _state.update { it.copy(busy = false, unlocked = true) }
            } else {
                val (failures, lockedUntil) = PinLockout.afterFailure(stored.failedAttempts, clock())
                settings.recordPinFailure(failures, lockedUntil)
                _state.update { it.copy(busy = false, entered = "", message = "That PIN is not right. Please try again.") }
                startCountdownIfLocked(lockedUntil)
            }
        }
    }

    private fun startCountdownIfLocked(lockedUntil: Long) {
        if (PinLockout.remainingLockMillis(lockedUntil, clock()) <= 0) return
        viewModelScope.launch {
            while (true) {
                val remaining = PinLockout.remainingLockMillis(lockedUntil, clock())
                if (remaining <= 0) break
                _state.update { it.copy(lockedSeconds = ((remaining + 999) / 1000).toInt()) }
                delay(1_000)
            }
            _state.update { it.copy(lockedSeconds = 0, message = null) }
        }
    }
}
