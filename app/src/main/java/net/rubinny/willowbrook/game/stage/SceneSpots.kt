package net.rubinny.willowbrook.game.stage

import kotlin.random.Random

/**
 * Where a hidden object sits: [x] is a fraction of the free width (area width minus object width), [row] is
 * 0 (upper) or 1 (lower), and [lift] (0..1) moves it a little toward the middle when the area has room to
 * spare. Rows are laid out flush top and bottom first, so two rows never overlap if the area is at least
 * two objects tall ([SceneSpots.MIN_HEIGHT_FACTOR]).
 */
data class Spot(val x: Float, val row: Int, val lift: Float) {
    /** Top of the object in an area [areaH] tall, for objects [objH] tall. */
    fun top(areaH: Float, objH: Float): Float {
        val slack = (areaH - 2 * objH).coerceAtLeast(0f)
        return if (row == 0) lift * slack / 2 else (areaH - objH - lift * slack / 2).coerceAtLeast(0f)
    }

    fun left(areaW: Float, objW: Float): Float = x * (areaW - objW).coerceAtLeast(0f)
}

/**
 * Places hidden objects on the scene in two loose rows, like things left on a sill, a chair or the floor.
 * Positions are fixed for each scene (same place every time: consistency helps memory), with a little
 * scene-specific height variation so it never looks like a grid. The order spreads the first objects apart,
 * so a gentle round with few objects still uses the whole room.
 */
object SceneSpots {
    const val MAX = 9

    /** The play area must be this many object heights tall (two rows that never overlap). */
    const val MIN_HEIGHT_FACTOR = 2f

    private val upper = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
    private val lower = listOf(0.125f, 0.375f, 0.625f, 0.875f)

    /** Spread-out order: corners first, then fill in. */
    private val order = listOf(
        0 to 0, 1 to 1, 0 to 4, 1 to 2, 0 to 2, 1 to 0, 1 to 3, 0 to 1, 0 to 3,
    )

    fun forScene(scene: String?): List<Spot> {
        val random = Random((scene ?: "").hashCode())
        return order.map { (row, column) ->
            Spot(x = if (row == 0) upper[column] else lower[column], row = row, lift = random.nextFloat())
        }
    }
}
