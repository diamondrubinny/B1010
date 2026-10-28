package net.rubinny.willowbrook.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.data.profiles.Profile
import net.rubinny.willowbrook.data.profiles.ProfileRepository
import net.rubinny.willowbrook.data.progress.ProgressRepository
import net.rubinny.willowbrook.data.settings.SettingsRepository
import net.rubinny.willowbrook.game.model.CaseContent

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object ProfileMissing : HomeUiState
    data class Ready(val profile: Profile, val continueTitle: String, val continueSubtitle: String) : HomeUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    profileId: String,
    profiles: ProfileRepository,
    settings: SettingsRepository,
    progress: ProgressRepository,
    content: ContentRepository,
) : ViewModel() {

    private val caseFlow = flow<CaseContent?> {
        emit(runCatching { content.load(ContentRepository.DEFAULT_CASE) }.getOrNull())
    }

    val state: StateFlow<HomeUiState> = combine(
        profiles.profiles,
        progress.progress(profileId, ContentRepository.DEFAULT_CASE),
        caseFlow,
    ) { list, p, case ->
        val profile = list.firstOrNull { it.id == profileId } ?: return@combine HomeUiState.ProfileMissing
        val title = when {
            case == null -> "Continue the case"
            p.chapterIndex >= case.chapters.size -> "Visit the case again"
            p.hasStarted -> "Continue the case"
            else -> "Start the case"
        }
        val subtitle = when {
            case == null -> "The Lantern of Willowbrook"
            p.chapterIndex >= case.chapters.size -> case.title
            else -> case.chapters[p.chapterIndex].title
        }
        HomeUiState.Ready(profile, title, subtitle)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    init {
        viewModelScope.launch { settings.update { it.copy(lastProfileId = profileId) } }
    }
}
