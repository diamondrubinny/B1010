package net.rubinny.willowbrook.game.stage

import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Gentle ambient particles. Nothing sudden, nothing that could look like insects swarming or smoke from a fire. */
enum class ParticleKind {
    /** Dust in a beam of light: drifts slowly upward and sideways. */
    MOTE,

    /** An autumn leaf: falls slowly, swaying and turning. */
    LEAF,

    /** A soft firefly light at dusk: wanders and glows on and off slowly. */
    FIREFLY,

    /** Warm steam from fresh baking: rises, widens and fades. */
    STEAM,
}

/** A rectangle in scene fractions (0..1 of the scene width and height). */
data class Area(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width get() = right - left
    val height get() = bottom - top
    fun contains(x: Float, y: Float) = x in left..right && y in top..bottom
}

/** Where and how particles appear. [size] is a fraction of the scene height; [colors] are ARGB. */
data class Emitter(val kind: ParticleKind, val area: Area, val count: Int, val size: Float, val colors: List<Long>)

class Particle {
    var x = 0f
    var y = 0f
    var vx = 0f
    var vy = 0f
    var age = 0f
    var life = 1f
    var size = 0f
    var phase = 0f
    var turn = 0f
    var color = 0L

    /** 0..1: fades in over the first 15% of its life and out over the last 25%. */
    val fade: Float
        get() {
            val t = (age / life).coerceIn(0f, 1f)
            return minOf(t / 0.15f, (1f - t) / 0.25f, 1f).coerceAtLeast(0f)
        }
}

/**
 * Simulates one emitter. Deterministic for a given [seed], so screenshots and tests are repeatable.
 * Only [active] particles (count × quality factor) are updated and drawn, never more than [Emitter.count].
 */
class ParticleSystem(val emitter: Emitter, seed: Int) {
    private val random = Random(seed)
    val particles: List<Particle> = List(emitter.count) { Particle().also { respawn(it, initial = true) } }
    var active: Int = emitter.count
        private set

    fun setQuality(factor: Float) {
        active = ceil(emitter.count * factor.coerceIn(0f, 1f)).toInt().coerceIn(0, emitter.count)
    }

    fun update(dt: Float) {
        val step = dt.coerceIn(0f, 0.1f) // after a pause, continue calmly instead of jumping
        for (i in 0 until active) {
            val p = particles[i]
            p.age += step
            when (emitter.kind) {
                ParticleKind.MOTE -> {
                    p.x += (p.vx + 0.004f * sin(p.age * 0.6f + p.phase)) * step
                    p.y += p.vy * step
                }
                ParticleKind.LEAF -> {
                    p.x += (p.vx + 0.012f * sin(p.age * 1.4f + p.phase)) * step
                    p.y += p.vy * step
                    p.turn += 40f * sin(p.age * 0.9f + p.phase) * step
                }
                ParticleKind.FIREFLY -> {
                    val heading = p.phase + 0.35f * p.age
                    p.x += 0.012f * cos(heading) * step
                    p.y += 0.008f * sin(heading * 1.3f) * step
                }
                ParticleKind.STEAM -> {
                    p.x += 0.004f * sin(p.age * 1.1f + p.phase) * step
                    p.y += p.vy * step
                    p.size += emitter.size * 0.35f * step
                }
            }
            val area = emitter.area
            if (emitter.kind == ParticleKind.MOTE || emitter.kind == ParticleKind.FIREFLY) {
                // Drifting lights stay in their patch of light and only fade away at the end of their life.
                p.x = p.x.coerceIn(area.left, area.right)
                p.y = p.y.coerceIn(area.top, area.bottom)
            }
            if (p.age >= p.life || !area.contains(p.x, p.y)) respawn(p, initial = false)
        }
    }

    /** Opacity 0..1 for drawing (fireflies also glow on and off, about every 5 seconds). */
    fun alpha(p: Particle): Float = when (emitter.kind) {
        ParticleKind.FIREFLY -> p.fade * (0.55f + 0.45f * sin(p.age * 1.2f + p.phase))
        ParticleKind.STEAM -> p.fade * 0.35f
        ParticleKind.MOTE -> p.fade * 0.7f
        ParticleKind.LEAF -> p.fade
    }

    private fun Random.between(a: Float, b: Float) = a + nextFloat() * (b - a)

    private fun respawn(p: Particle, initial: Boolean) {
        val a = emitter.area
        p.age = 0f
        p.phase = random.between(0f, 6.28f)
        p.size = emitter.size * random.between(0.7f, 1.3f)
        p.color = emitter.colors[random.nextInt(emitter.colors.size)]
        p.turn = random.between(0f, 360f)
        p.x = random.between(a.left, a.right)
        when (emitter.kind) {
            ParticleKind.MOTE -> {
                p.y = random.between(a.top, a.bottom)
                p.vx = random.between(-0.004f, 0.004f)
                p.vy = random.between(-0.010f, -0.004f)
                p.life = random.between(8f, 14f)
            }
            ParticleKind.LEAF -> {
                // Start along the top edge, or anywhere at the beginning so the scene is not empty.
                p.y = if (initial) random.between(a.top, a.bottom) else a.top
                p.vx = random.between(-0.008f, 0.008f)
                p.vy = random.between(0.025f, 0.045f)
                p.life = a.height / p.vy + 1f
            }
            ParticleKind.FIREFLY -> {
                p.y = random.between(a.top, a.bottom)
                p.life = random.between(10f, 20f)
            }
            ParticleKind.STEAM -> {
                p.y = if (initial) random.between(a.top, a.bottom) else a.bottom
                p.vy = random.between(-0.030f, -0.018f)
                // Fully faded before it reaches the top of its area.
                p.life = minOf(random.between(3.5f, 5.5f), (p.y - a.top) / -p.vy).coerceAtLeast(0.5f)
            }
        }
        // At the start, spread ages so particles don't all fade in together.
        if (initial && emitter.kind != ParticleKind.LEAF) p.age = random.between(0f, p.life * 0.6f)
    }
}
