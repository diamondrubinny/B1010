package net.rubinny.willowbrook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

/** One-button information dialog, e.g. "What are we doing?". */
@Composable
fun InfoDialog(title: String, paragraphs: List<String>, buttonText: String, onDismiss: () -> Unit) {
    val colors = LocalWillowColors.current
    val focus = remember { FocusRequester() }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        LaunchedEffect(Unit) { focus.requestFocusSafely() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.92f))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.safeHorizontal, vertical = Dimens.safeVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 880.dp)
                    .background(colors.surface, RoundedCornerShape(Dimens.cornerRadius))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = title,
                    style = WillowType.title,
                    color = colors.accent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                paragraphs.forEach {
                    Text(text = it, style = WillowType.body, color = colors.textPrimary, textAlign = TextAlign.Center)
                }
                BigButton(text = buttonText, onClick = onDismiss, minWidth = 300.dp, modifier = Modifier.focusRequester(focus))
            }
        }
    }
}
