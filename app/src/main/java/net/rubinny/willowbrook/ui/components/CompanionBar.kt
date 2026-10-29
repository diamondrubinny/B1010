package net.rubinny.willowbrook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

/**
 * Marmalade's speech bubble. Always in the same place, so the player knows where help appears.
 * A polite live region lets screen readers announce new messages without interrupting.
 */
@Composable
fun CompanionBar(message: String?, modifier: Modifier = Modifier) {
    val colors = LocalWillowColors.current
    Row(
        modifier = modifier
            .heightIn(min = 72.dp)
            .background(colors.surface, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = message?.let { "Marmalade says: $it" } ?: "Marmalade is here to help."
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Portrait(key = "marmalade_face", emoji = "🐈", size = 56.dp)
        Text(
            text = message ?: "I'm here if you need me.",
            style = WillowType.body,
            color = if (message != null) colors.textPrimary else colors.textSecondary,
        )
    }
}
