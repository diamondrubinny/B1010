package net.rubinny.willowbrook.game.stage

/**
 * What the camera shows: [zoom] (>= 1) over the scene and the centre offset [panX]/[panY] as a fraction
 * of the scene size. A layer at depth d is shifted by pan × d, so nearer layers move more (parallax).
 */
data class CameraView(val zoom: Float, val panX: Float, val panY: Float) {
    companion object {
        /** "Less movement on screen": the whole picture, still. */
        val STILL = CameraView(1f, 0f, 0f)

        /** The furthest the centre may move at [zoom] without showing the picture's edge. */
        fun maxPan(zoom: Float): Float = (1f - 1f / zoom.coerceAtLeast(1f)) / 2f
    }
}

/**
 * A slow, calm camera. When a scene opens it starts a little closer and settles back over [introSeconds]
 * (an establishing shot), then "breathes": a very slow zoom and drift, periods of 20–40 seconds.
 * Pan is always clamped by the current zoom, so the edge of the picture is never shown.
 */
class StageCamera(
    private val baseZoom: Float = 1.06f,
    private val breathZoom: Float = 0.01f,
    private val drift: Float = 0.012f,
    private val introZoom: Float = 0.08f,
    private val introSeconds: Float = 6f,
) {
    fun at(seconds: Float): CameraView {
        val settle = Motion.easeOutCubic(seconds / introSeconds)
        val zoom = baseZoom + introZoom * (1f - settle) + breathZoom * Motion.wave(seconds, 1f / 23f)
        val limit = CameraView.maxPan(zoom)
        val panX = (drift * Motion.wave(seconds, 1f / 31f)).coerceIn(-limit, limit)
        val panY = (drift * 0.6f * Motion.wave(seconds, 1f / 37f, 1f)).coerceIn(-limit, limit)
        return CameraView(zoom, panX, panY)
    }
}
