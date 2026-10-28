package net.rubinny.willowbrook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Text
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

/** Every screen uses the same frame: overscan-safe margins, one heading, then content. */
@Composable
fun ScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalWillowColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            // Large caregiver text sizes may not fit; focus changes scroll the focused item into view.
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.safeHorizontal, vertical = Dimens.safeVertical),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(Dimens.gap),
    ) {
        Text(
            text = title,
            style = WillowType.title,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        if (subtitle != null) {
            Text(text = subtitle, style = WillowType.body, color = colors.textSecondary, textAlign = TextAlign.Center)
        }
        content()
    }
}
