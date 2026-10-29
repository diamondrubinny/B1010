package net.rubinny.willowbrook.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import net.rubinny.willowbrook.data.profiles.ProfileRules
import net.rubinny.willowbrook.data.settings.Difficulty
import net.rubinny.willowbrook.data.settings.SettingsLimits as L
import net.rubinny.willowbrook.ui.components.ActionRow
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.ConfirmDialog
import net.rubinny.willowbrook.ui.components.ScreenScaffold
import net.rubinny.willowbrook.ui.components.StepperRow
import net.rubinny.willowbrook.ui.components.ToggleRow
import net.rubinny.willowbrook.ui.components.requestFocusSafely
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType
import kotlin.math.roundToInt

private sealed interface PendingConfirm {
    data class DeleteProfile(val id: String, val nickname: String) : PendingConfirm
    data object DeleteAll : PendingConfirm
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onAddPlayer: () -> Unit,
    onChangePin: () -> Unit,
    onAbout: () -> Unit,
    onSummary: () -> Unit,
    onAllDataDeleted: () -> Unit,
    onClose: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state.settings
    var pending by remember { mutableStateOf<PendingConfirm?>(null) }
    var deleteAllStep by rememberSaveable { mutableIntStateOf(0) }
    val firstFocus = remember { FocusRequester() }
    val rowWidth = Modifier.widthIn(max = 860.dp)

    ScreenScaffold(title = "Caregiver settings", horizontalAlignment = Alignment.CenterHorizontally) {
        BigButton(
            text = "Done",
            onClick = onClose,
            minWidth = 240.dp,
            modifier = Modifier.focusRequester(firstFocus),
        )

        Section("Reading and seeing")
        StepperRow(
            label = "Text size",
            valueText = "${(s.textScale * 100).roundToInt()}%",
            onDecrease = { viewModel.update { it.copy(textScale = it.textScale - L.TEXT_SCALE_STEP) } },
            onIncrease = { viewModel.update { it.copy(textScale = it.textScale + L.TEXT_SCALE_STEP) } },
            canDecrease = s.textScale > L.TEXT_SCALE_MIN,
            canIncrease = s.textScale < L.TEXT_SCALE_MAX,
            modifier = rowWidth,
        )
        ToggleRow("Extra-strong contrast", s.highContrast, { v -> viewModel.update { it.copy(highContrast = v) } }, rowWidth)
        ToggleRow("Less movement on screen", s.reduceMotion, { v -> viewModel.update { it.copy(reduceMotion = v) } }, rowWidth)

        Section("Voice and sound")
        ToggleRow("Read the story aloud", s.narrationEnabled, { v -> viewModel.update { it.copy(narrationEnabled = v) } }, rowWidth)
        ToggleRow("Show words on screen (captions)", s.captionsEnabled, { v -> viewModel.update { it.copy(captionsEnabled = v) } }, rowWidth)
        StepperRow(
            label = "Speaking speed",
            valueText = "${(s.speechRate * 100).roundToInt()}%",
            onDecrease = { viewModel.update { it.copy(speechRate = it.speechRate - L.SPEECH_RATE_STEP) } },
            onIncrease = { viewModel.update { it.copy(speechRate = it.speechRate + L.SPEECH_RATE_STEP) } },
            canDecrease = s.speechRate > L.SPEECH_RATE_MIN,
            canIncrease = s.speechRate < L.SPEECH_RATE_MAX,
            modifier = rowWidth,
        )
        StepperRow(
            label = "Sound volume",
            valueText = if (s.musicVolume <= 0f) "Off" else "${(s.musicVolume * 100).roundToInt()}%",
            onDecrease = { viewModel.update { it.copy(musicVolume = it.musicVolume - L.MUSIC_VOLUME_STEP) } },
            onIncrease = { viewModel.update { it.copy(musicVolume = it.musicVolume + L.MUSIC_VOLUME_STEP) } },
            canDecrease = s.musicVolume > L.MUSIC_VOLUME_MIN,
            canIncrease = s.musicVolume < L.MUSIC_VOLUME_MAX,
            modifier = rowWidth,
        )

        StepperRow(
            label = "Music volume",
            valueText = if (s.backgroundMusicVolume <= 0f) "Off" else "${(s.backgroundMusicVolume * 100).roundToInt()}%",
            onDecrease = { viewModel.update { it.copy(backgroundMusicVolume = it.backgroundMusicVolume - L.MUSIC_VOLUME_STEP) } },
            onIncrease = { viewModel.update { it.copy(backgroundMusicVolume = it.backgroundMusicVolume + L.MUSIC_VOLUME_STEP) } },
            canDecrease = s.backgroundMusicVolume > L.MUSIC_VOLUME_MIN,
            canIncrease = s.backgroundMusicVolume < L.MUSIC_VOLUME_MAX,
            modifier = rowWidth,
        )

        Section("Puzzles")
        ActionRow(
            label = "Difficulty: ${s.difficulty.displayName()}",
            detail = "Press OK to change. ${s.difficulty.explanation()}",
            onClick = {
                val all = Difficulty.entries
                viewModel.update { it.copy(difficulty = all[(it.difficulty.ordinal + 1) % all.size]) }
            },
            modifier = rowWidth,
        )
        StepperRow(
            label = "Wait before offering a hint",
            valueText = "${s.hintDelaySec} seconds",
            onDecrease = { viewModel.update { it.copy(hintDelaySec = it.hintDelaySec - L.HINT_DELAY_STEP) } },
            onIncrease = { viewModel.update { it.copy(hintDelaySec = it.hintDelaySec + L.HINT_DELAY_STEP) } },
            canDecrease = s.hintDelaySec > L.HINT_DELAY_MIN,
            canIncrease = s.hintDelaySec < L.HINT_DELAY_MAX,
            modifier = rowWidth,
        )

        Section("Players")
        ActionRow(
            label = "Progress summary",
            detail = "Chapters finished and how much help was needed",
            onClick = onSummary,
            modifier = rowWidth,
        )
        state.profiles.forEach { profile ->
            ActionRow(
                label = "Remove ${profile.nickname}",
                detail = "Deletes this player and their progress",
                onClick = { pending = PendingConfirm.DeleteProfile(profile.id, profile.nickname) },
                modifier = rowWidth,
            )
        }
        if (state.profiles.size < ProfileRules.MAX_PROFILES) {
            ActionRow(label = "Add a player", onClick = onAddPlayer, modifier = rowWidth)
        }

        Section("Privacy and safety")
        ActionRow(label = "Change caregiver PIN", onClick = onChangePin, modifier = rowWidth)
        ActionRow(label = "About, privacy and accessibility", onClick = onAbout, modifier = rowWidth)
        ActionRow(
            label = "Delete all data",
            detail = "Removes all players, progress, settings and the PIN",
            onClick = { deleteAllStep = 1; pending = PendingConfirm.DeleteAll },
            modifier = rowWidth,
        )
        Spacer(Modifier.height(24.dp))
    }

    LaunchedEffect(Unit) { firstFocus.requestFocusSafely() }

    when (val p = pending) {
        is PendingConfirm.DeleteProfile -> ConfirmDialog(
            title = "Remove ${p.nickname}?",
            message = "This player's progress will be deleted. This cannot be undone.",
            confirmText = "Remove",
            dismissText = "Keep",
            onConfirm = { viewModel.deleteProfile(p.id); pending = null },
            onDismiss = { pending = null },
        )
        PendingConfirm.DeleteAll -> ConfirmDialog(
            // Two steps for the most destructive action in the app.
            title = if (deleteAllStep == 1) "Delete all data?" else "Are you completely sure?",
            message = if (deleteAllStep == 1) {
                "All players, progress, settings and the PIN will be removed from this TV."
            } else {
                "This cannot be undone."
            },
            confirmText = if (deleteAllStep == 1) "Continue" else "Delete everything",
            dismissText = "Cancel",
            onConfirm = {
                if (deleteAllStep == 1) {
                    deleteAllStep = 2
                } else {
                    pending = null
                    viewModel.deleteAllData(onAllDataDeleted)
                }
            },
            onDismiss = { pending = null; deleteAllStep = 0 },
        )
        null -> Unit
    }
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(8.dp))
    Text(
        text = title,
        style = WillowType.title,
        color = LocalWillowColors.current.accent,
        modifier = Modifier.semantics { heading() },
    )
}

private fun Difficulty.displayName() = when (this) {
    Difficulty.AUTO -> "Automatic"
    Difficulty.GENTLE -> "Gentle"
    Difficulty.STANDARD -> "Standard"
}

private fun Difficulty.explanation() = when (this) {
    Difficulty.AUTO -> "Adjusts quietly to how the player is doing."
    Difficulty.GENTLE -> "Fewer choices and more help."
    Difficulty.STANDARD -> "More choices, help still available."
}
