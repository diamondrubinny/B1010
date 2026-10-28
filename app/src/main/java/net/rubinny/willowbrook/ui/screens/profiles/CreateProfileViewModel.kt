package net.rubinny.willowbrook.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rubinny.willowbrook.data.profiles.Avatar
import net.rubinny.willowbrook.data.profiles.ProfileRepository
import net.rubinny.willowbrook.data.profiles.ProfileRules

data class CreateProfileUiState(
    val nickname: String = "",
    val avatar: Avatar = Avatar.AMBER,
    val error: String? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
)

class CreateProfileViewModel(private val profiles: ProfileRepository) : ViewModel() {
    private val _state = MutableStateFlow(CreateProfileUiState())
    val state: StateFlow<CreateProfileUiState> = _state.asStateFlow()

    fun onNicknameChange(value: String) {
        // Hard cap while typing; full validation happens on save.
        _state.update { it.copy(nickname = value.take(ProfileRules.NICKNAME_MAX_LENGTH), error = null) }
    }

    fun onAvatarChange(avatar: Avatar) = _state.update { it.copy(avatar = avatar) }

    fun save() {
        val current = _state.value
        if (current.saving || current.saved) return // prevents double submission
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val result = profiles.add(current.nickname, current.avatar)) {
                is ProfileRepository.AddResult.Added -> _state.update { it.copy(saving = false, saved = true) }
                is ProfileRepository.AddResult.Rejected -> _state.update { it.copy(saving = false, error = result.message) }
            }
        }
    }
}
