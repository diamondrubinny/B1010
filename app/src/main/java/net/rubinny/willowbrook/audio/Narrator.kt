package net.rubinny.willowbrook.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import net.rubinny.willowbrook.game.engine.SpokenLine
import java.util.Locale

/** Reads story lines aloud. Implementations must never throw: speech is a help, never a requirement. */
interface Narrator {
    fun speak(line: SpokenLine, rate: Float)
    fun stop()
    fun release()
}

/**
 * Plays the recorded AI voice file assets/voice/<id>.ogg when it exists; otherwise uses the TV's
 * on-device text-to-speech (British English, falling back to any English voice). Nothing leaves the device.
 */
class AndroidNarrator(private val context: Context) : Narrator, TextToSpeech.OnInitListener {
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext, this)
    private var ttsReady = false
    private var pending: Pair<SpokenLine, Float>? = null
    private var player: MediaPlayer? = null

    /** Names of recorded voice files, read once. */
    private val recorded: Set<String> by lazy {
        try {
            context.assets.list("voice")?.toSet().orEmpty()
        } catch (_: Exception) {
            emptySet()
        }
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            Log.w(TAG, "Text-to-speech is not available on this device")
            return
        }
        val uk = tts.setLanguage(Locale.UK)
        if (uk == TextToSpeech.LANG_MISSING_DATA || uk == TextToSpeech.LANG_NOT_SUPPORTED) tts.setLanguage(Locale.ENGLISH)
        ttsReady = true
        pending?.let { (line, rate) -> speak(line, rate) }
        pending = null
    }

    override fun speak(line: SpokenLine, rate: Float) {
        stop()
        val file = line.id?.let { "$it.ogg" }?.takeIf { it in recorded }
        if (file != null && playRecorded(file)) return
        if (!ttsReady) {
            pending = line to rate // engine still starting: say it as soon as it is ready
            return
        }
        tts.setSpeechRate(rate.coerceIn(0.5f, 1.5f))
        tts.speak(line.text, TextToSpeech.QUEUE_FLUSH, null, line.id ?: "line")
    }

    private fun playRecorded(file: String): Boolean = try {
        val afd = context.assets.openFd("voice/$file")
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            setOnCompletionListener { it.release(); if (player === it) player = null }
            prepare()
            start()
        }
        true
    } catch (e: Exception) {
        Log.w(TAG, "Could not play recorded line, using text-to-speech instead")
        false
    }

    override fun stop() {
        pending = null
        player?.let {
            try {
                it.stop()
            } catch (_: IllegalStateException) {
            }
            it.release()
        }
        player = null
        if (ttsReady) tts.stop()
    }

    override fun release() {
        stop()
        tts.shutdown()
    }

    private companion object {
        const val TAG = "Narrator"
    }
}
