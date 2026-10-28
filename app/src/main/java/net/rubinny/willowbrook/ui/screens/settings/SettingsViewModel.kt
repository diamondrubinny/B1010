package net.rubinny.willowbrook.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.rubinny.willowbrook.AppContainer
import net.rubinny.willowbrook.data.profiles.Profile
import net.rubinny.willowbrook.data.settings.Settings

data class SettingsUiState(
    val loading: Boolean = true,
    val settings: Settings = Settings(),
    val profiles: List<Profile> = emptyList(),
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        container.settingsRepository.settings,
        container.profileRepository.profiles,
    ) { s, p -> SettingsUiState(loading = false, settings = s, profiles = p) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** Values are clamped by the repository, so callers can simply add or subtract a step. */
    fun update(transform: (Settings) -> Settings) {
        viewModelScope.launch { container.settingsRepository.update(transform) }
    }

    fun deleteProfile(id: String) {
        viewModelScope.launch { container.deleteProfile(id) }
    }

    fun deleteAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            container.deleteAllData()
            onDone()
        }
    }
}
