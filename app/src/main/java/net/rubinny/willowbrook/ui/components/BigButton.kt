package net.rubinny.willowbrook.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalReduceMotion
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

/**
 * The single interactive building block of the app. Focus is shown three ways at once
 * (gold fill, thick white ring, slight growth) so it never relies on colour alone.
 */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accessibilityLabel: String? = null,
    minWidth: Dp = Dimens.buttonMinWidth,
    minHeight: Dp = Dimens.buttonMinHeight,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = LocalWillowColors.current
    val reduceMotion = LocalReduceMotion.current
    val shape = RoundedCornerShape(Dimens.cornerRadius)

    Surface(
        onClick = onClick,
        modifier = modifier
            .let { m -> if (accessibilityLabel != null) m.semantics { contentDescription = accessibilityLabel } else m },
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = colors.surface,
            contentColor = colors.textPrimary,
            focusedContainerColor = colors.surfaceFocused,
            focusedContentColor = colors.textOnFocus,
            pressedContainerColor = colors.surfaceFocused,
            pressedContentColor = colors.textOnFocus,
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(2.dp, colors.textSecondary.copy(alpha = 0.4f)), shape = shape),
            focusedBorder = Border(BorderStroke(Dimens.focusBorder, colors.focusRing), shape = shape),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (reduceMotion) 1f else 1.05f),
    ) {
        // Min size lives on the inner box so the content is centred inside the full button area.
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
                .padding(horizontal = 32.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                leading?.invoke()
                Text(text = text, style = WillowType.button, textAlign = TextAlign.Center)
                if (subtitle != null) {
                    Text(text = subtitle, style = WillowType.label, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
