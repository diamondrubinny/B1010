package net.rubinny.willowbrook.ui.screens.settings

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.rubinny.willowbrook.AppContainer
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.game.engine.EffectiveDifficulty
import net.rubinny.willowbrook.game.engine.ProgressSummary
import net.rubinny.willowbrook.game.engine.ProgressSummaryData
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.ReadableBlock
import net.rubinny.willowbrook.ui.components.ScreenScaffold
import net.rubinny.willowbrook.ui.components.requestFocusSafely
import java.text.DateFormat
import java.util.Date
import java.util.Locale

data class PlayerSummary(val nickname: String, val data: ProgressSummaryData)

data class SummaryUiState(val loading: Boolean = true, val players: List<PlayerSummary> = emptyList(), val error: Boolean = false)

class SummaryViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(SummaryUiState())
    val state: StateFlow<SummaryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val case = container.contentRepository.load(ContentRepository.DEFAULT_CASE)
                val settings = container.settingsRepository.current()
                val players = container.profileRepository.profiles.first().map { profile ->
                    val progress = container.progressRepository.get(profile.id, case.id)
                    PlayerSummary(profile.nickname, ProgressSummary.summarize(case, progress, settings.difficulty))
                }
                _state.value = SummaryUiState(loading = false, players = players)
            } catch (_: Exception) {
                _state.value = SummaryUiState(loading = false, error = true)
            }
        }
    }
}

/** Plain-language text for one player. Kept separate so it can be unit-tested. */
fun describe(data: ProgressSummaryData, locale: Locale = Locale.UK): String = buildString {
    append("Chapters finished: ${data.chaptersDone} of ${data.totalChapters}. ")
    append(data.nextChapterTitle?.let { "Next: $it. " } ?: "The whole case is solved. ")
    data.lastPlayedAt?.let {
        append("Last played: ${DateFormat.getDateInstance(DateFormat.LONG, locale).format(Date(it))}. ")
    } ?: append("Not played yet. ")
    when (val help = data.helpNeeded) {
        null -> append("Too few puzzles so far to say how much help was needed. ")
        else -> {
            append("Recent puzzles: ${help.text.lowercase()}")
            data.trend?.let { append(", ${it.text}") }
            append(". ")
        }
    }
    append(
        "Puzzles are currently " +
            (if (data.difficulty == EffectiveDifficulty.GENTLE) "gentle (fewer choices)." else "standard (more choices)."),
    )
}

@Composable
fun SummaryScreen(viewModel: SummaryViewModel, onClose: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focus = remember { FocusRequester() }
    val width = Modifier.widthIn(max = 860.dp)
    ScreenScaffold(title = "Progress summary") {
        BigButton("Back", onClick = onClose, minWidth = 240.dp, modifier = Modifier.focusRequester(focus))
        when {
            state.loading -> Unit
            state.error -> ReadableBlock(null, "The summary could not be opened.", width)
            state.players.isEmpty() -> ReadableBlock(null, "There are no players yet.", width)
            else -> state.players.forEach { ReadableBlock(it.nickname, describe(it.data), width) }
        }
        ReadableBlock(
            heading = "Please note",
            body = "This summary is only about the game. It is not a medical assessment. " +
                "Many things, like tiredness, mood or a busy day, change how a game goes. " +
                "It is kept on this TV only and covers the last 20 puzzles.",
            modifier = width,
        )
    }
    LaunchedEffect(Unit) { focus.requestFocusSafely() }
}
