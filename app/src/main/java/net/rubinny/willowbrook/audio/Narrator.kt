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
    /** Called on the main thread when speech starts (true) and ends (false). Used to dip the music. */
    var onSpeakingChanged: ((Boolean) -> Unit)?

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
    private val main = android.os.Handler(android.os.Looper.getMainLooper())
    override var onSpeakingChanged: ((Boolean) -> Unit)? = null

    private fun speaking(value: Boolean) {
        main.post { onSpeakingChanged?.invoke(value) }
    }

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
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = speaking(true)
            override fun onDone(utteranceId: String?) = speaking(false)
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = speaking(false)
            override fun onStop(utteranceId: String?, interrupted: Boolean) = speaking(false)
        })
        val uk = tts.setLanguage(Locale.UK)
        if (uk == TextToSpeech.LANG_MISSING_DATA || uk == TextToSpeech.LANG_NOT_SUPPORTED) tts.setLanguage(Locale.ENGLISH)
        ttsReady = true
        pending?.let { (line, rate) -> speak(line, rate) }
        pending = null
    }

    override fun speak(line: SpokenLine, rate: Float) {
        stop()
        val file = line.id?.let { "$it.ogg" }?.takeIf { it in recorded }
        if (file != null && playRecorded(file, rate)) return
        if (!ttsReady) {
            pending = line to rate // engine still starting: say it as soon as it is ready
            return
        }
        tts.setSpeechRate(rate.coerceIn(0.5f, 1.5f))
        tts.speak(line.text, TextToSpeech.QUEUE_FLUSH, null, line.id ?: "line")
    }

    /**
     * Recordings were made at a calm pace that matches the default speaking speed (0.85);
     * the caregiver's speed setting scales playback without changing the pitch of the voice.
     */
    private fun playRecorded(file: String, rate: Float): Boolean = try {
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
            setOnCompletionListener {
                it.release()
                if (player === it) player = null
                speaking(false)
            }
            prepare()
            try {
                playbackParams = android.media.PlaybackParams().setSpeed((rate / DEFAULT_RATE).coerceIn(0.75f, 1.3f))
            } catch (_: Exception) {
                // Some TVs do not support speed changes: play at normal speed.
            }
            start()
        }
        speaking(true)
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
        speaking(false)
    }

    override fun release() {
        stop()
        tts.shutdown()
    }

    private companion object {
        const val TAG = "Narrator"
        const val DEFAULT_RATE = 0.85f
    }
}
