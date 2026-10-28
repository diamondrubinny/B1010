package net.rubinny.willowbrook.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.rubinny.willowbrook.data.profiles.Profile
import net.rubinny.willowbrook.data.profiles.ProfileRepository
import net.rubinny.willowbrook.data.settings.SettingsRepository

data class ProfilesUiState(
    val loading: Boolean = true,
    val profiles: List<Profile> = emptyList(),
    val lastProfileId: String? = null,
)

class ProfilesViewModel(
    private val profiles: ProfileRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<ProfilesUiState> = combine(profiles.profiles, settings.settings) { list, s ->
        ProfilesUiState(loading = false, profiles = list, lastProfileId = s.lastProfileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfilesUiState())

    fun rememberChoice(profileId: String) {
        viewModelScope.launch { settings.update { it.copy(lastProfileId = profileId) } }
    }
}
