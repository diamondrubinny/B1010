package net.rubinny.willowbrook.audio

import android.speech.tts.TextToSpeech
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.rubinny.willowbrook.game.engine.SpokenLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowTextToSpeech

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class NarratorTest {

    private fun lastSpoken(): String? = shadowOf(ShadowTextToSpeech.getLastTextToSpeechInstance()).lastSpokenText

    @Test fun `without a recorded file the line is read by text-to-speech`() {
        val narrator = AndroidNarrator(ApplicationProvider.getApplicationContext())
        narrator.onInit(TextToSpeech.SUCCESS)
        narrator.speak(SpokenLine("ch0_intro", "Welcome to Willowbrook."), 0.85f)
        assertEquals("Welcome to Willowbrook.", lastSpoken())
        narrator.release()
    }

    @Test fun `a line requested before the speech engine is ready is spoken once it is ready`() {
        val narrator = AndroidNarrator(ApplicationProvider.getApplicationContext())
        narrator.speak(SpokenLine(null, "Hello!"), 1f)
        assertNull(lastSpoken())
        narrator.onInit(TextToSpeech.SUCCESS)
        assertEquals("Hello!", lastSpoken())
        narrator.release()
    }

    @Test fun `no speech engine on the device never crashes the game`() {
        val narrator = AndroidNarrator(ApplicationProvider.getApplicationContext())
        narrator.onInit(TextToSpeech.ERROR)
        narrator.speak(SpokenLine(null, "Hello!"), 1f)
        narrator.stop()
        narrator.release()
        assertNull(lastSpoken())
    }
}
