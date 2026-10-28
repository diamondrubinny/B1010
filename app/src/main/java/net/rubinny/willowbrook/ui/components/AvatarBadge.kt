package net.rubinny.willowbrook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import net.rubinny.willowbrook.data.profiles.Avatar
import net.rubinny.willowbrook.ui.theme.WillowType

/** Coloured circle with the player's initial. Decorative: the parent button carries the spoken label. */
@Composable
fun AvatarBadge(avatar: Avatar, initial: String, size: Dp = 88.dp) {
    Box(
        modifier = Modifier
            .size(size)
            // Dark ring keeps the badge visible on the gold focused-button background.
            .border(3.dp, Color(0xFF16202A), CircleShape)
            .background(Color(avatar.argb), CircleShape)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = initial.take(1).uppercase(), style = WillowType.title, color = Color(0xFF16202A))
    }
}
