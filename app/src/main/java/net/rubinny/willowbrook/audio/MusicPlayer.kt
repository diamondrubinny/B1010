package net.rubinny.willowbrook.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import net.rubinny.willowbrook.R

enum class MusicTrack(val resId: Int) {
    MENU(R.raw.music_menu),
    STORY(R.raw.music_story),
    FINALE(R.raw.music_finale),
}

/**
 * Quiet background music. Tracks cross-fade slowly (no sudden sound), the music dips while someone
 * is speaking, and it pauses when the TV switches to another app. Music is a comfort, never a
 * requirement: every failure is caught and the game simply carries on in silence.
 */
class MusicPlayer(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var track: MusicTrack? = null
    private var volume = 0.5f
    private var ducked = false
    private var inBackground = false
    private var fade = 0f
    private var fadeTarget = 0f

    fun play(next: MusicTrack?) {
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

    private fun start(next: MusicTrack) {
        if (volume <= 0f) return
        safe {
            player = MediaPlayer.create(
                context,
                next.resId,
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
        val v = volume * fade * (if (ducked) DUCK_LEVEL else 1f)
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

    private companion object {
        const val DUCK_LEVEL = 0.3f
    }
}
