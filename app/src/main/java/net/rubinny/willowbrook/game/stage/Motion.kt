package net.rubinny.willowbrook.game.stage

import kotlin.math.PI
import kotlin.math.sin

/** Soft curves and gentle oscillations for the living scenes. Pure functions: deterministic and easy to test. */
object Motion {
    /** No light changes faster than this (WCAG 2.3.1 allows up to 3 flashes a second; we stay far below). */
    const val MAX_FLICKER_HZ = 0.5f

    fun smoothstep(t: Float): Float {
        val x = t.coerceIn(0f, 1f)
        return x * x * (3f - 2f * x)
    }

    fun easeOutCubic(t: Float): Float {
        val x = 1f - t.coerceIn(0f, 1f)
        return 1f - x * x * x
    }

    fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    /** A calm wave in [-1, 1]. */
    fun wave(seconds: Float, hz: Float, phase: Float = 0f): Float = sin(2.0 * PI * hz * seconds + phase).toFloat()

    /**
     * Candle-like flicker in [1 - amplitude, 1 + amplitude]: three slow waves whose weights add up to 1,
     * none faster than [MAX_FLICKER_HZ], so it never reads as flashing.
     */
    fun flicker(seconds: Float, amplitude: Float, seed: Int = 0): Float {
        val s = seed * 1.7f
        val v = 0.5f * wave(seconds, 0.23f, s) + 0.3f * wave(seconds, 0.41f, 2f * s) + 0.2f * wave(seconds, MAX_FLICKER_HZ, 3f * s)
        return 1f + amplitude * v
    }
}
