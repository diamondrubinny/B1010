package net.rubinny.willowbrook.game.stage

enum class Quality(val particleFactor: Float) { HIGH(1f), LOW(0.5f) }

/**
 * Keeps the scene smooth on cheap TV boxes: if frames are slow (average over [windowFrames] above
 * [slowFrameMillis]), it drops to [Quality.LOW] at once; it returns to HIGH only after [recoverSeconds]
 * of comfortably fast frames, so it does not keep switching back and forth.
 */
class QualityGovernor(
    initial: Quality,
    private val windowFrames: Int = 60,
    private val slowFrameMillis: Float = 30f,
    private val fastFrameMillis: Float = 20f,
    private val recoverSeconds: Float = 30f,
) {
    var quality: Quality = initial
        private set
    private val window = FloatArray(windowFrames)
    private var filled = 0
    private var next = 0
    private var goodSeconds = 0f

    fun onFrame(frameMillis: Float): Quality {
        window[next] = frameMillis
        next = (next + 1) % windowFrames
        if (filled < windowFrames) filled++
        if (filled < windowFrames) return quality
        val average = window.average().toFloat()
        when {
            average > slowFrameMillis -> {
                quality = Quality.LOW
                goodSeconds = 0f
            }
            quality == Quality.LOW && average < fastFrameMillis -> {
                goodSeconds += frameMillis / 1000f
                if (goodSeconds >= recoverSeconds) {
                    quality = Quality.HIGH
                    goodSeconds = 0f
                }
            }
            else -> goodSeconds = 0f
        }
        return quality
    }
}
