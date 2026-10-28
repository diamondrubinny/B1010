package net.rubinny.willowbrook.ui.screens.profiles

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
import net.rubinny.willowbrook.ui.theme.Dimens

@Composable
fun ProfilePickerScreen(
    viewModel: ProfilesViewModel,
    onProfileChosen: (String) -> Unit,
    onCreateProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitApp: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val primaryFocus = remember { FocusRequester() }

    BackHandler { confirmExit = true }

    if (state.loading) {
        ScreenScaffold(title = "Willowbrook Mysteries") {}
        return
    }

    if (state.profiles.isEmpty()) {
        ScreenScaffold(
            title = "Welcome to Willowbrook",
            subtitle = "A gentle village mystery. First, let's set up a player.",
        ) {
            Spacer(Modifier.height(Dimens.gap))
            BigButton(
                text = "Set up a player",
                onClick = onCreateProfile,
                modifier = Modifier.focusRequester(primaryFocus),
            )
            BigButton(text = "Caregiver settings", onClick = onOpenSettings, minWidth = 280.dp)
        }
    } else {
        // Remembered player goes first, so "OK" right away continues as the same person.
        val ordered = state.profiles.sortedByDescending { it.id == state.lastProfileId }
        ScreenScaffold(title = "Who is playing today?") {
            Spacer(Modifier.height(Dimens.gap))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                ordered.forEachIndexed { index, profile ->
                    BigButton(
                        text = profile.nickname,
                        accessibilityLabel = "Play as ${profile.nickname}",
                        minWidth = 200.dp,
                        onClick = {
                            viewModel.rememberChoice(profile.id)
                            onProfileChosen(profile.id)
                        },
                        modifier = if (index == 0) Modifier.focusRequester(primaryFocus) else Modifier,
                        leading = { AvatarBadge(profile.avatar, profile.nickname) },
                    )
                }
            }
            Spacer(Modifier.height(Dimens.gapLarge))
            BigButton(text = "Caregiver settings", onClick = onOpenSettings, minWidth = 280.dp)
        }
    }

    LaunchedEffect(state.profiles.size) { primaryFocus.requestFocusSafely() }

    if (confirmExit) {
        ConfirmDialog(
            title = "Would you like to rest now?",
            message = "You can come back to Willowbrook any time.",
            confirmText = "Yes, close the game",
            dismissText = "Stay here",
            onConfirm = onExitApp,
            onDismiss = { confirmExit = false },
        )
    }
}
