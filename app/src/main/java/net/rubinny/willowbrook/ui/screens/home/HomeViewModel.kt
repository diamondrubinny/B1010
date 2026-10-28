package net.rubinny.willowbrook.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.rubinny.willowbrook.data.profiles.Profile
import net.rubinny.willowbrook.data.profiles.ProfileRepository
import net.rubinny.willowbrook.data.settings.SettingsRepository

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object ProfileMissing : HomeUiState
    data class Ready(val profile: Profile) : HomeUiState
}

class HomeViewModel(
    profileId: String,
    profiles: ProfileRepository,
    settings: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = profiles.profiles
        .map { list -> list.firstOrNull { it.id == profileId }?.let { HomeUiState.Ready(it) } ?: HomeUiState.ProfileMissing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    init {
        viewModelScope.launch { settings.update { it.copy(lastProfileId = profileId) } }
    }
}
