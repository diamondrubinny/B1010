package net.rubinny.willowbrook.ui.screens.casefiles

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.Text
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.data.progress.ProgressRepository
import net.rubinny.willowbrook.ui.components.ActionRow
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.ScreenScaffold
import net.rubinny.willowbrook.ui.components.requestFocusSafely
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

enum class ChapterStatus { DONE, CURRENT }

data class ChapterEntry(val index: Int, val title: String, val location: String, val status: ChapterStatus)

data class CaseFilesUiState(
    val loading: Boolean = true,
    val caseTitle: String = "",
    val chapters: List<ChapterEntry> = emptyList(),
    val moreToCome: Boolean = false,
    val error: Boolean = false,
)

/** Lists finished chapters (to play again) and the current one. Chapters not reached yet are not shown. */
class CaseFilesViewModel(
    profileId: String,
    content: ContentRepository,
    progress: ProgressRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CaseFilesUiState())
    val state: StateFlow<CaseFilesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val case = content.load(ContentRepository.DEFAULT_CASE)
                val p = progress.get(profileId, case.id)
                val entries = case.chapters.mapIndexedNotNull { i, c ->
                    when {
                        c.id in p.completedChapters -> ChapterEntry(i, c.title, c.location, ChapterStatus.DONE)
                        i == p.chapterIndex -> ChapterEntry(i, c.title, c.location, ChapterStatus.CURRENT)
                        else -> null
                    }
                }
                _state.value = CaseFilesUiState(false, case.title, entries, moreToCome = entries.size < case.chapters.size)
            } catch (_: Exception) {
                _state.value = CaseFilesUiState(loading = false, error = true)
            }
        }
    }
}

@Composable
fun CaseFilesScreen(
    viewModel: CaseFilesViewModel,
    onReplay: (Int) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalWillowColors.current
    val focus = remember { FocusRequester() }
    val width = Modifier.widthIn(max = 860.dp)

    ScreenScaffold(title = "Case files", subtitle = state.caseTitle.ifEmpty { null }) {
        BigButton("Back", onClick = onBack, minWidth = 240.dp, modifier = Modifier.focusRequester(focus))
        if (state.error) Text("The case files could not be opened.", style = WillowType.body, color = colors.danger)
        state.chapters.forEach { entry ->
            val label = if (entry.index == 0) entry.title else "Chapter ${entry.index}: ${entry.title}"
            when (entry.status) {
                ChapterStatus.DONE -> ActionRow(
                    label = "✓ $label",
                    detail = "${entry.location}. Press OK to play again.",
                    onClick = { onReplay(entry.index) },
                    modifier = width,
                )
                ChapterStatus.CURRENT -> ActionRow(
                    label = "→ $label",
                    detail = "${entry.location}. Press OK to carry on.",
                    onClick = onContinue,
                    modifier = width,
                )
            }
        }
        if (state.moreToCome) Text("More chapters are waiting to be discovered.", style = WillowType.body, color = colors.textSecondary)
    }
    LaunchedEffect(Unit) { focus.requestFocusSafely() }
}
