package net.rubinny.willowbrook.ui.screens.notebook

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import net.rubinny.willowbrook.AppContainer
import net.rubinny.willowbrook.audio.Narrator
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.game.engine.Notebook
import net.rubinny.willowbrook.game.engine.SpokenLine
import net.rubinny.willowbrook.game.model.Item
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.ScreenScaffold
import net.rubinny.willowbrook.ui.components.requestFocusSafely
import net.rubinny.willowbrook.ui.screens.play.OptionTile
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

/** A page of the notebook as shown: the clue's picture and name, or a "still to find" page. */
data class NotebookPage(val item: Item, val line: SpokenLine?, val found: Boolean)

data class NotebookUiState(val loading: Boolean = true, val pages: List<NotebookPage> = emptyList(), val speechRate: Float = 0.85f)

class NotebookViewModel(private val container: AppContainer, private val profileId: String) : ViewModel() {
    private val _state = MutableStateFlow(NotebookUiState())
    val state: StateFlow<NotebookUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val pages = try {
                val case = container.contentRepository.load(ContentRepository.DEFAULT_CASE)
                val progress = container.progressRepository.get(profileId, case.id)
                Notebook.entries(case, progress).map { e ->
                    val item = case.item(e.clue.clue) ?: Item(e.clue.clue, e.clue.clue, "🔍")
                    if (e.found) NotebookPage(item, SpokenLine(e.clue.id, e.clue.text), true) else NotebookPage(STILL_TO_FIND, null, false)
                }
            } catch (_: Exception) {
                emptyList()
            }
            _state.value = NotebookUiState(loading = false, pages = pages, speechRate = container.settingsRepository.current().speechRate)
        }
    }

    companion object {
        val STILL_TO_FIND = Item("still_to_find", "Still to find", "❔")
    }
}

/**
 * The detective notebook: every clue found so far, as a memory aid. Choosing a clue reads it aloud again.
 * Clues not found yet are friendly "Still to find" pages, never hidden: the player can see how many there are.
 */
@Composable
fun NotebookScreen(viewModel: NotebookViewModel, narrator: Narrator, onClose: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalWillowColors.current
    val first = remember { FocusRequester() }
    BackHandler { narrator.stop(); onClose() }
    DisposableEffect(Unit) { onDispose { narrator.stop() } }

    val found = state.pages.count { it.found }
    ScreenScaffold(
        title = "📓 Detective notebook",
        subtitle = if (state.loading) null else "Clues found: $found of ${state.pages.size}",
    ) {
        if (!state.loading && found == 0) {
            Text("Your first clue is waiting at the cottage.", style = WillowType.body, color = colors.textSecondary)
        }
        Row(
            modifier = Modifier.widthIn(max = 900.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.gap),
        ) {
            state.pages.forEachIndexed { index, page ->
                OptionTile(
                    item = page.item,
                    tried = !page.found,
                    glowing = false,
                    onClick = { page.line?.let { narrator.speak(it, state.speechRate) } },
                    modifier = Modifier.weight(1f).then(if (index == 0) Modifier.focusRequester(first) else Modifier),
                )
            }
        }
        BigButton("Close the notebook", onClick = { narrator.stop(); onClose() }, minWidth = 420.dp)
    }
    LaunchedEffect(state.loading) { if (!state.loading) first.requestFocusSafely() }
}
