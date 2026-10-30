package net.rubinny.willowbrook.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import net.rubinny.willowbrook.R

/** Each place in the story has its own piece, so the music itself helps say "where we are". */
enum class MusicTrack(val resId: Int) {
    MENU(R.raw.music_menu),
    COTTAGE(R.raw.music_cottage),
    BAKERY(R.raw.music_bakery),
    GARDEN(R.raw.music_garden),
    LIBRARY(R.raw.music_library),
    FINALE(R.raw.music_finale),
    ;

    companion object {
        fun forScene(scene: String?): MusicTrack = when (scene) {
            "bakery" -> BAKERY
            "garden" -> GARDEN
            "library" -> LIBRARY
            "square" -> FINALE
            else -> COTTAGE
        }
    }
}

/** Soft scene sounds under the music (tools/generate_ambience.py). No voices, knocks or alarms. */
enum class Ambience(val resId: Int) {
    COTTAGE(R.raw.amb_cottage),
    BAKERY(R.raw.amb_bakery),
    GARDEN(R.raw.amb_garden),
    LIBRARY(R.raw.amb_library),
    SQUARE(R.raw.amb_square),
    ;

    companion object {
        fun forScene(scene: String?): Ambience? = entries.firstOrNull { it.name.equals(scene, ignoreCase = true) }
    }
}

/**
 * A quiet looping sound: the background music, or (a second instance) the scene ambience.
 * Loops cross-fade slowly (no sudden sound), dip to [duckLevel] while someone is speaking, and pause
 * when the TV switches to another app. Sound is a comfort, never a requirement: every failure is
 * caught and the game simply carries on in silence.
 */
class MusicPlayer(private val context: Context, private val duckLevel: Float = 0.3f) {
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var track: Int? = null
    private var volume = 0.5f
    private var ducked = false
    private var inBackground = false
    private var fade = 0f
    private var fadeTarget = 0f

    fun play(next: MusicTrack?) = playRes(next?.resId)

    fun play(next: Ambience?) = playRes(next?.resId)

    private fun playRes(next: Int?) {
        if (next == track) return
        track = next
        fadeTo(0f) {
            release()
            if (next != null && !inBackground) start(next)
        }
    }

    fun setVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
        apply()
        if (volume > 0f && player == null && !inBackground) track?.let(::start)
    }

    /** Lower the music while the story is being read aloud. */
    fun setDucked(value: Boolean) {
        ducked = value
        apply()
    }

    fun onBackground() {
        inBackground = true
        safe { player?.pause() }
    }

    fun onForeground() {
        inBackground = false
        if (player == null) track?.let(::start) else safe { player?.start() }
    }

    private fun start(next: Int) {
        if (volume <= 0f) return
        safe {
            player = MediaPlayer.create(
                context,
                next,
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                0,
            )?.apply {
                isLooping = true
                setVolume(0f, 0f)
                start()
            }
            fade = 0f
            fadeTo(1f, durationMs = 2_000)
        }
    }

    private fun fadeTo(target: Float, durationMs: Long = 700, done: (() -> Unit)? = null) {
        fadeTarget = target
        handler.removeCallbacksAndMessages(null)
        val steps = 20
        val delta = (target - fade) / steps
        fun step(i: Int) {
            fade = if (i >= steps) target else (fade + delta).coerceIn(0f, 1f)
            apply()
            if (i >= steps) done?.invoke() else handler.postDelayed({ step(i + 1) }, durationMs / steps)
        }
        if (player == null) {
            fade = target
            done?.invoke()
        } else {
            step(1)
        }
    }

    private fun apply() {
        val v = volume * fade * (if (ducked) duckLevel else 1f)
        safe { player?.setVolume(v, v) }
    }

    private fun release() {
        safe {
            player?.stop()
            player?.release()
        }
        player = null
    }

    fun shutdown() {
        handler.removeCallbacksAndMessages(null)
        release()
    }

    private inline fun safe(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.w("MusicPlayer", "Music unavailable: ${e.javaClass.simpleName}")
            player = null
        }
    }
}
