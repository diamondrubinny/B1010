package net.rubinny.willowbrook.ui.screens.profiles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import net.rubinny.willowbrook.data.profiles.Avatar
import net.rubinny.willowbrook.ui.components.AvatarBadge
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.ScreenScaffold
import net.rubinny.willowbrook.ui.components.requestFocusSafely
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

@Composable
fun CreateProfileScreen(viewModel: CreateProfileViewModel, onDone: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalWillowColors.current
    val nameFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var nameFocused by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onDone() }
    LaunchedEffect(Unit) { nameFocus.requestFocusSafely() }

    ScreenScaffold(
        title = "New player",
        subtitle = "Type a first name or nickname.",
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Name", style = WillowType.label, color = colors.textSecondary)
                BasicTextField(
                    value = state.nickname,
                    onValueChange = viewModel::onNicknameChange,
                    singleLine = true,
                    textStyle = WillowType.title.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.accent),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.save() }),
                    modifier = Modifier
                        .width(480.dp)
                        .focusRequester(nameFocus)
                        .onFocusChanged { nameFocused = it.isFocused }
                        // Up/Down always leave the field so a remote can never get stuck in it;
                        // Left/Right still move the text cursor.
                        .onPreviewKeyEvent { event ->
                            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                            when (event.key) {
                                Key.DirectionDown -> focusManager.moveFocus(FocusDirection.Down)
                                Key.DirectionUp -> focusManager.moveFocus(FocusDirection.Up)
                                else -> false
                            }
                        }
                        .semantics {
                            contentDescription = "Player name"
                            state.error?.let { error(it) }
                        },
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .background(colors.surface, RoundedCornerShape(16.dp))
                                .border(
                                    BorderStroke(
                                        if (nameFocused) Dimens.focusBorder else 2.dp,
                                        if (nameFocused) colors.focusRing else colors.textSecondary,
                                    ),
                                    RoundedCornerShape(16.dp),
                                )
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                        ) { inner() }
                    },
                )
            }
            BigButton(
                text = "Colour: ${state.avatar.label}",
                subtitle = "Press OK to change",
                minWidth = 300.dp,
                accessibilityLabel = "Colour ${state.avatar.label}. Press OK to change",
                onClick = {
                    val all = Avatar.entries
                    viewModel.onAvatarChange(all[(state.avatar.ordinal + 1) % all.size])
                },
                leading = { AvatarBadge(state.avatar, state.nickname.ifBlank { "?" }, size = 56.dp) },
            )
        }

        if (state.error != null) {
            Text(
                text = state.error.orEmpty(),
                style = WillowType.body,
                color = colors.danger,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        // Primary action first (left), matching every other screen.
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
            BigButton(text = if (state.saving) "Saving…" else "Save player", onClick = viewModel::save, minWidth = 280.dp)
            BigButton(text = "Cancel", onClick = onDone, minWidth = 240.dp)
        }
    }
}
