package net.rubinny.willowbrook.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureScreenRoboImage
import net.rubinny.willowbrook.ui.components.SceneBackground
import net.rubinny.willowbrook.ui.theme.WillowbrookTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshots of every living scene at a fixed moment (layers, light and particles), and still
 * ("Less movement on screen"). Saved to build/screenshots/ for review; also proves each scene renders.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-television-xhdpi")
class LivingSceneTest {
    @get:Rule val rule = createComposeRule()

    private val scenes = listOf("cottage", "bakery", "garden", "library", "square")

    @Test fun livingScenes() {
        var current by mutableStateOf(scenes.first())
        var still by mutableStateOf(false)
        rule.setContent {
            WillowbrookTheme(reduceMotion = still) {
                SceneBackground(scene = current, fixedSeconds = if (still) null else 20f) {}
            }
        }
        for (scene in scenes) {
            current = scene
            still = false
            rule.waitForIdle()
            captureScreenRoboImage("build/screenshots/60_scene_${scene}_living.png")
            still = true
            rule.waitForIdle()
            captureScreenRoboImage("build/screenshots/61_scene_${scene}_still.png")
        }
    }
}
