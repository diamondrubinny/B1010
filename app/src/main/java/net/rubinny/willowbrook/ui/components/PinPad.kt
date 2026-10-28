package net.rubinny.willowbrook.ui.components

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import net.rubinny.willowbrook.security.PinHasher
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

/**
 * On-screen number pad for the caregiver PIN. Works with the D-pad, and remotes that have
 * number keys can type directly. Entered digits are never displayed or announced.
 */
@Composable
fun PinPad(
    entered: String,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalWillowColors.current
    val firstKey = remember { FocusRequester() }
    val dots = (0 until PinHasher.PIN_LENGTH).joinToString("  ") { if (it < entered.length) "●" else "○" }

    Column(
        modifier = modifier.onPreviewKeyEvent { event ->
            if (!enabled || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            when (val code = event.key.nativeKeyCode) {
                in AndroidKeyEvent.KEYCODE_0..AndroidKeyEvent.KEYCODE_9 -> {
                    onDigit('0' + (code - AndroidKeyEvent.KEYCODE_0)); true
                }
                AndroidKeyEvent.KEYCODE_DEL -> { onDelete(); true }
                else -> false
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = dots,
            style = WillowType.display,
            color = colors.accent,
            modifier = Modifier.semantics {
                contentDescription = "${entered.length} of ${PinHasher.PIN_LENGTH} digits entered"
                liveRegion = LiveRegionMode.Polite
            },
        )
        val rows = listOf(listOf('1', '2', '3'), listOf('4', '5', '6'), listOf('7', '8', '9'))
        rows.forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEachIndexed { colIndex, digit ->
                    val m = if (rowIndex == 0 && colIndex == 0) Modifier.focusRequester(firstKey) else Modifier
                    BigButton(text = digit.toString(), onClick = { if (enabled) onDigit(digit) }, minWidth = 140.dp, minHeight = 76.dp, modifier = m)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BigButton(text = "0", onClick = { if (enabled) onDigit('0') }, minWidth = 140.dp, minHeight = 76.dp)
            BigButton(text = "Delete", onClick = { if (enabled) onDelete() }, minWidth = 296.dp, minHeight = 76.dp, accessibilityLabel = "Delete last digit")
        }
    }
    LaunchedEffect(Unit) { firstKey.requestFocusSafely() }
}
