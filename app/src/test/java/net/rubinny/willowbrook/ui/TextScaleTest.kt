package net.rubinny.willowbrook.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.tv.material3.Text
import net.rubinny.willowbrook.ui.theme.WillowType
import net.rubinny.willowbrook.ui.theme.WillowbrookTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The caregiver "Text size" setting must actually enlarge text everywhere. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-television-xhdpi")
class TextScaleTest {
    @get:Rule val rule = createComposeRule()

    @Test fun textScaleEnlargesText() {
        var scale by mutableFloatStateOf(1.0f)
        rule.setContent {
            WillowbrookTheme(textScale = scale) {
                Text("Willowbrook", style = WillowType.body, modifier = Modifier.testTag("t"))
            }
        }
        val h1 = rule.onNodeWithTag("t").getUnclippedBoundsInRoot().let { it.bottom - it.top }.value
        scale = 1.6f
        rule.waitForIdle()
        val h16 = rule.onNodeWithTag("t").getUnclippedBoundsInRoot().let { it.bottom - it.top }.value
        assertEquals(1.6f, h16 / h1, 0.05f)
    }
}
