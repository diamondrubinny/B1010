package net.rubinny.willowbrook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

/**
 * Two-choice confirmation. The SAFE option (dismiss) always gets initial focus,
 * so an accidental double-press of OK never triggers the destructive action.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalWillowColors.current
    val safeFocus = remember { FocusRequester() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Must run inside the dialog's own composition/window, otherwise the request is lost.
        LaunchedEffect(Unit) { safeFocus.requestFocusSafely() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.92f))
                .padding(horizontal = Dimens.safeHorizontal, vertical = Dimens.safeVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .background(colors.surface, RoundedCornerShape(Dimens.cornerRadius))
                    .padding(Dimens.gapLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.gap),
            ) {
                Text(
                    text = title,
                    style = WillowType.title,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Text(text = message, style = WillowType.body, color = colors.textSecondary, textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                    BigButton(
                        text = dismissText,
                        onClick = onDismiss,
                        minWidth = 280.dp,
                        modifier = Modifier.focusRequester(safeFocus),
                    )
                    BigButton(text = confirmText, onClick = onConfirm, minWidth = 280.dp)
                }
            }
        }
    }
}
