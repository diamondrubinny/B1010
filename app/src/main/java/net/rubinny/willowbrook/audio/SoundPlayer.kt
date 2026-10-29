package net.rubinny.willowbrook.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/** Short effects via SoundPool (low latency). Volume 0 = silent. */
class SoundPlayer(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val loaded = mutableSetOf<Int>()
    private val ids: Map<SoundCue, Int> = SoundCue.entries.associateWith { pool.load(context, it.resId, 1) }

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status -> if (status == 0) loaded += sampleId }
    }

    fun play(cue: SoundCue, volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        if (v <= 0f) return
        val id = ids[cue] ?: return
        if (id !in loaded) return // still loading on a slow TV box: skip rather than delay the game
        pool.play(id, v, v, 1, 0, 1f)
    }

    fun release() = pool.release()
}
