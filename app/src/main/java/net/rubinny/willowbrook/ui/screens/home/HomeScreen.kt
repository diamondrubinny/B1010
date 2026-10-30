package net.rubinny.willowbrook.ui.screens.home

import net.rubinny.willowbrook.ui.components.SecretSettingsDialog
import net.rubinny.willowbrook.ui.components.secretSettingsHold
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.rubinny.willowbrook.ui.components.AvatarBadge
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.ConfirmDialog
import net.rubinny.willowbrook.ui.components.ScreenScaffold
import net.rubinny.willowbrook.ui.components.requestFocusSafely
import androidx.tv.material3.Text
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onContinue: () -> Unit,
    onCaseFiles: () -> Unit,
    onBeads: () -> Unit,
    onNotebook: () -> Unit,
    onChangePlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onProfileMissing: () -> Unit,
    onExitApp: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    var askSettings by rememberSaveable { mutableStateOf(false) }
    val continueFocus = remember { FocusRequester() }

    // Back on Home never exits silently: it asks first, with "Keep playing" pre-selected.
    BackHandler { confirmExit = true }

    when (val s = state) {
        HomeUiState.Loading -> ScreenScaffold(title = "Willowbrook Mysteries") {}
        HomeUiState.ProfileMissing -> LaunchedEffect(Unit) { onProfileMissing() }
        is HomeUiState.Ready -> {
            ScreenScaffold(title = "Hello, ${s.profile.nickname}", modifier = Modifier.secretSettingsHold { askSettings = true }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarBadge(s.profile.avatar, s.profile.nickname, size = 72.dp)
                    Text(
                        text = "The village of Willowbrook needs a good detective.",
                        style = WillowType.body,
                        color = LocalWillowColors.current.textSecondary,
                    )
                }
                // One dominant action; everything else is secondary and smaller.
                BigButton(
                    text = s.continueTitle,
                    subtitle = s.continueSubtitle,
                    onClick = onContinue,
                    minWidth = 640.dp,
                    modifier = Modifier.focusRequester(continueFocus),
                )
                Spacer(Modifier.height(8.dp))
                // A calm game to play any time, outside the story.
                BigButton(text = "✨ Lantern Beads", subtitle = "A calm game of light beads", onClick = onBeads, minWidth = 640.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                    BigButton(text = "📓 Notebook", onClick = onNotebook, minWidth = 240.dp, accessibilityLabel = "Detective notebook")
                    BigButton(text = "Case files", onClick = onCaseFiles, minWidth = 240.dp)
                    BigButton(text = "Change player", onClick = onChangePlayer, minWidth = 240.dp)
                }
            }
            LaunchedEffect(s.profile.id) { continueFocus.requestFocusSafely() }
        }
    }

    if (askSettings) {
        SecretSettingsDialog(onOpen = { askSettings = false; onOpenSettings() }, onDismiss = { askSettings = false })
    }

    if (confirmExit) {
        ConfirmDialog(
            title = "Would you like to rest now?",
            message = "Your progress is saved. You can come back any time.",
            confirmText = "Yes, rest now",
            dismissText = "Keep playing",
            onConfirm = onExitApp,
            onDismiss = { confirmExit = false },
        )
    }
}
