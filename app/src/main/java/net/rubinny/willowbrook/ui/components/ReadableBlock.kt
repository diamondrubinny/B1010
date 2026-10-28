package net.rubinny.willowbrook.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

/**
 * A focusable block of text. A TV remote can only scroll by moving focus,
 * so long text is split into blocks that each take focus in turn.
 */
@Composable
fun ReadableBlock(heading: String?, body: String, modifier: Modifier = Modifier) {
    val colors = LocalWillowColors.current
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .border(Dimens.focusBorder, if (focused) colors.focusRing else Color.Transparent, shape)
            .padding(24.dp),
    ) {
        if (heading != null) {
            Text(text = heading, style = WillowType.button, color = colors.accent, modifier = Modifier.semantics { heading() })
        }
        Text(text = body, style = WillowType.body, color = colors.textPrimary)
    }
}
