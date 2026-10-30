package net.rubinny.willowbrook.game.stage

/**
 * A warm light in the scene (fire, oven, lamp, lantern, sun). Position and [radius] are fractions of the
 * scene width/height ([radius] of the width). [flicker] is the gentle variation (0 = steady, at most 0.15).
 */
data class Light(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Long,
    val strength: Float,
    val flicker: Float,
    val seed: Int = 0,
)

/** Everything that makes one scene feel alive. */
data class SceneFx(val lights: List<Light>, val emitters: List<Emitter>)

/** Effects for each scene (positions match the artwork in tools/art/scenes.py, 1920 x 1080). */
object SceneFxCatalog {
    private const val SW = 1920f
    private const val SH = 1080f
    private fun area(l: Int, t: Int, r: Int, b: Int) = Area(l / SW, t / SH, r / SW, b / SH)
    private fun light(x: Int, y: Int, r: Int, color: Long, strength: Float, flicker: Float, seed: Int) =
        Light(x / SW, y / SH, r / SW, color, strength, flicker, seed)

    private const val SUNBEAM = 0xFFFFF3C4
    private val autumn = listOf(0xFFE8762C, 0xFFD9862C, 0xFFF0B84A, 0xFFC96A2C)

    val all: Map<String, SceneFx> = mapOf(
        "cottage" to SceneFx(
            lights = listOf(light(500, 660, 340, 0xFFFFB347, 0.85f, 0.12f, 1)),
            emitters = listOf(Emitter(ParticleKind.MOTE, area(1390, 190, 1730, 760), 14, 0.006f, listOf(SUNBEAM))),
        ),
        "bakery" to SceneFx(
            lights = listOf(light(420, 580, 340, 0xFFFF9F43, 0.85f, 0.10f, 2)),
            emitters = listOf(
                Emitter(ParticleKind.STEAM, area(960, 470, 1040, 585), 6, 0.03f, listOf(0xFFFFFFFF)),
                Emitter(ParticleKind.MOTE, area(760, 60, 1880, 620), 8, 0.005f, listOf(0xFFFFF8EC)),
            ),
        ),
        "garden" to SceneFx(
            lights = listOf(light(1600, 180, 300, 0xFFFFD166, 0.55f, 0.04f, 3)),
            emitters = listOf(Emitter(ParticleKind.LEAF, area(80, 260, 1840, 920), 10, 0.016f, autumn)),
        ),
        "library" to SceneFx(
            lights = listOf(light(1040, 650, 220, 0xFFFFE08A, 0.7f, 0.05f, 4)),
            emitters = listOf(Emitter(ParticleKind.MOTE, area(770, 150, 1150, 700), 14, 0.006f, listOf(SUNBEAM))),
        ),
        "square" to SceneFx(
            lights = listOf(light(960, 530, 300, 0xFFFFD166, 0.85f, 0.10f, 5)),
            emitters = listOf(Emitter(ParticleKind.FIREFLY, area(60, 470, 1860, 1000), 12, 0.008f, listOf(0xFFFFE680))),
        ),
    )

    fun forScene(scene: String?): SceneFx? = scene?.let { all[it] }
}
