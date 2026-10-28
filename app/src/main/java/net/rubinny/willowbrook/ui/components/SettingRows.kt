package net.rubinny.willowbrook.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalReduceMotion
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

@Composable
private fun RowSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = LocalWillowColors.current
    val shape = RoundedCornerShape(16.dp)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = colors.surface,
            contentColor = colors.textPrimary,
            focusedContainerColor = colors.surfaceFocused,
            focusedContentColor = colors.textOnFocus,
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(Dimens.focusBorder, colors.focusRing), shape = shape),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (LocalReduceMotion.current) 1f else 1.02f),
    ) {
        content()
    }
}

/** Press OK to switch on/off. The state is spoken ("On"/"Off") and also written, not shown by colour only. */
@Composable
fun ToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val state = if (checked) "On" else "Off"
    RowSurface(
        onClick = { onToggle(!checked) },
        modifier = modifier
            .fillMaxWidth()
            .semantics { stateDescription = state },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = WillowType.body, modifier = Modifier.weight(1f))
            Text(text = state, style = WillowType.button)
        }
    }
}

/** Opens a sub-action (e.g. change PIN). */
@Composable
fun ActionRow(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, detail: String? = null) {
    RowSurface(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp)) {
            Text(text = label, style = WillowType.body)
            if (detail != null) Text(text = detail, style = WillowType.label)
        }
    }
}

/**
 * A value with "Less" and "More" buttons. Left/Right on the remote move focus between the buttons,
 * so we avoid hijacking D-pad keys (which would trap keyboard-only users).
 * Buttons at a limit stay focusable (dimmed, no-op): disabling a focused button would drop focus.
 */
@Composable
fun StepperRow(
    label: String,
    valueText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    canDecrease: Boolean,
    canIncrease: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalWillowColors.current
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gap),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = WillowType.body, color = colors.textPrimary)
            Text(text = valueText, style = WillowType.button, color = colors.accent)
        }
        SmallStepButton(
            text = "Less",
            atLimit = !canDecrease,
            onClick = { if (canDecrease) onDecrease() },
            accessibilityLabel = "$label: less. Now $valueText" + if (!canDecrease) ", lowest" else "",
        )
        SmallStepButton(
            text = "More",
            atLimit = !canIncrease,
            onClick = { if (canIncrease) onIncrease() },
            accessibilityLabel = "$label: more. Now $valueText" + if (!canIncrease) ", highest" else "",
        )
    }
}

@Composable
private fun SmallStepButton(text: String, atLimit: Boolean, onClick: () -> Unit, accessibilityLabel: String) {
    val colors = LocalWillowColors.current
    val shape = RoundedCornerShape(16.dp)
    Surface(
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = accessibilityLabel },
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = colors.surface,
            contentColor = if (atLimit) colors.textSecondary.copy(alpha = 0.6f) else colors.textPrimary,
            focusedContainerColor = if (atLimit) colors.textSecondary else colors.surfaceFocused,
            focusedContentColor = colors.textOnFocus,
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(Dimens.focusBorder, colors.focusRing), shape = shape),
        ),
    ) {
        Text(text = text, style = WillowType.button, modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp))
    }
}
