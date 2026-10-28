package net.rubinny.willowbrook.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.PinPad
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

/** Two columns so the keypad fits a 540dp-high TV canvas: explanation on the left, keypad on the right. */
@Composable
fun PinGateScreen(viewModel: PinViewModel, onUnlocked: () -> Unit, onCancel: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalWillowColors.current

    LaunchedEffect(state.unlocked) { if (state.unlocked) onUnlocked() }

    val title = when (state.mode) {
        PinMode.LOADING, PinMode.VERIFY -> "Caregiver settings"
        PinMode.CREATE -> "Choose a 4-digit caregiver PIN"
        PinMode.CONFIRM -> "Type the same PIN again"
    }
    val subtitle = when (state.mode) {
        PinMode.VERIFY -> "Please type the caregiver PIN."
        PinMode.CREATE, PinMode.CONFIRM -> "The PIN keeps settings from being changed by accident."
        PinMode.LOADING -> null
    }
    val status = if (state.lockedSeconds > 0) "Too many tries. Please wait ${state.lockedSeconds} seconds." else state.message

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = Dimens.safeHorizontal, vertical = Dimens.safeVertical),
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.gap)) {
            Text(text = title, style = WillowType.title, color = colors.textPrimary, modifier = Modifier.semantics { heading() })
            if (subtitle != null) Text(text = subtitle, style = WillowType.body, color = colors.textSecondary)
            if (status != null) {
                Text(
                    text = status,
                    style = WillowType.body,
                    color = colors.danger,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            BigButton(text = "Cancel", onClick = onCancel, minWidth = 240.dp)
        }
        if (state.mode != PinMode.LOADING) {
            PinPad(
                entered = state.entered,
                onDigit = viewModel::onDigit,
                onDelete = viewModel::onDelete,
                enabled = !state.busy && state.lockedSeconds == 0,
            )
        }
    }
}
