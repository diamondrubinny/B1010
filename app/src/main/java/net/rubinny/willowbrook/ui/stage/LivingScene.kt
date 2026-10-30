package net.rubinny.willowbrook.ui.stage

import android.app.ActivityManager
import android.content.Context
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import net.rubinny.willowbrook.game.stage.CameraView
import net.rubinny.willowbrook.game.stage.Motion
import net.rubinny.willowbrook.game.stage.ParticleKind
import net.rubinny.willowbrook.game.stage.ParticleSystem
import net.rubinny.willowbrook.game.stage.Quality
import net.rubinny.willowbrook.game.stage.QualityGovernor
import net.rubinny.willowbrook.game.stage.SceneFx
import net.rubinny.willowbrook.game.stage.StageCamera
import kotlin.math.roundToInt

/** Parallax depths: the far wall moves least, the furniture in front moves most. */
private const val DEPTH_FAR = 0.6f
private const val DEPTH_LIGHT = 0.7f
private const val DEPTH_PARTICLES = 0.85f
private const val DEPTH_NEAR = 1f

/**
 * A scene that gently lives: a far layer, warm lights, ambient particles and a near layer, seen through a
 * slow camera. With [still] (the "Less movement on screen" setting) it is one calm picture with steady
 * light and no particles.
 *
 * Only the drawing reads the clock, so each frame redraws the canvas without recomposing anything.
 * The frame loop uses withInfiniteAnimationFrameNanos: it pauses when the app is in the background, and
 * test frameworks can stop it. [fixedSeconds] draws one moment instead (screenshots).
 */
@Composable
fun LivingScene(
    far: ImageBitmap,
    near: ImageBitmap?,
    fx: SceneFx?,
    still: Boolean,
    sceneKey: String,
    modifier: Modifier = Modifier,
    fixedSeconds: Float? = null,
) {
    val context = LocalContext.current
    val camera = remember { StageCamera() }
    val governor = remember { QualityGovernor(initialQuality(context)) }
    val systems = remember(sceneKey, fx) {
        fx?.emitters.orEmpty().mapIndexed { i, e -> ParticleSystem(e, seed = sceneKey.hashCode() * 31 + i) }
            .onEach { it.setQuality(governor.quality.particleFactor) }
    }
    var seconds by remember(sceneKey) { mutableFloatStateOf(0f) }

    if (fixedSeconds != null) {
        LaunchedEffect(sceneKey, fixedSeconds) {
            // Simulate up to that moment in calm 1/30 s steps, so the picture matches a running scene.
            var t = 0f
            while (t < fixedSeconds) {
                systems.forEach { it.update(1f / 30f) }
                t += 1f / 30f
            }
            seconds = fixedSeconds
        }
    } else if (!still) {
        LaunchedEffect(sceneKey) {
            var last = 0L
            while (true) {
                withInfiniteAnimationFrameNanos { now ->
                    if (last != 0L) {
                        val dt = (now - last) / 1_000_000_000f
                        val before = governor.quality
                        if (governor.onFrame(dt * 1000f) != before) systems.forEach { it.setQuality(governor.quality.particleFactor) }
                        systems.forEach { it.update(dt) }
                        seconds += dt.coerceAtMost(0.1f)
                    }
                    last = now
                }
            }
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val view = if (still) CameraView.STILL else camera.at(seconds)
        val frame = SceneFrame(size, far.width.toFloat(), far.height.toFloat(), view)
        drawLayer(far, frame, DEPTH_FAR)
        fx?.lights?.forEach { light ->
            val strength = light.strength * (if (still) 1f else Motion.flicker(seconds, light.flicker, light.seed))
            val center = frame.toScreen(light.x, light.y, DEPTH_LIGHT)
            val radius = light.radius * frame.sceneWidth
            drawCircle(
                brush = Brush.radialGradient(
                    0f to Color(light.color).copy(alpha = (0.75f * strength).coerceIn(0f, 1f)),
                    0.45f to Color(light.color).copy(alpha = (0.3f * strength).coerceIn(0f, 1f)),
                    1f to Color.Transparent,
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }
        if (!still) systems.forEach { drawParticles(it, frame) }
        near?.let { drawLayer(it, frame, DEPTH_NEAR) }
    }
}

/** Maps scene fractions to screen pixels for a camera view (cover-fit, like ContentScale.Crop). */
private class SceneFrame(screen: Size, imageW: Float, imageH: Float, private val view: CameraView) {
    private val cover = maxOf(screen.width / imageW, screen.height / imageH)
    val sceneWidth = imageW * cover * view.zoom
    val sceneHeight = imageH * cover * view.zoom
    private val cx = screen.width / 2f
    private val cy = screen.height / 2f

    fun left(depth: Float) = cx - (0.5f + view.panX * depth) * sceneWidth
    fun top(depth: Float) = cy - (0.5f + view.panY * depth) * sceneHeight
    fun toScreen(fx: Float, fy: Float, depth: Float) = Offset(left(depth) + fx * sceneWidth, top(depth) + fy * sceneHeight)
}

private fun DrawScope.drawLayer(image: ImageBitmap, frame: SceneFrame, depth: Float) {
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(frame.left(depth).roundToInt(), frame.top(depth).roundToInt()),
        dstSize = IntSize(frame.sceneWidth.roundToInt() + 1, frame.sceneHeight.roundToInt() + 1),
        filterQuality = FilterQuality.Low,
    )
}

private fun DrawScope.drawParticles(system: ParticleSystem, frame: SceneFrame) {
    val unit = frame.sceneHeight
    for (i in 0 until system.active) {
        val p = system.particles[i]
        val alpha = system.alpha(p).coerceIn(0f, 1f)
        if (alpha <= 0.01f) continue
        val at = frame.toScreen(p.x, p.y, DEPTH_PARTICLES)
        val color = Color(p.color)
        val r = p.size * unit
        when (system.emitter.kind) {
            ParticleKind.MOTE -> drawCircle(color.copy(alpha = alpha), r, at)
            ParticleKind.FIREFLY -> {
                drawCircle(
                    Brush.radialGradient(listOf(color.copy(alpha = alpha * 0.6f), Color.Transparent), center = at, radius = r * 4f),
                    r * 4f,
                    at,
                )
                drawCircle(color.copy(alpha = alpha), r, at)
            }
            ParticleKind.STEAM -> drawCircle(
                Brush.radialGradient(listOf(color.copy(alpha = alpha), Color.Transparent), center = at, radius = r),
                r,
                at,
            )
            ParticleKind.LEAF -> rotate(p.turn, at) {
                drawOval(color.copy(alpha = alpha), topLeft = Offset(at.x - r, at.y - r * 0.45f), size = Size(r * 2f, r * 0.9f))
            }
        }
    }
}

/** Cheap TV boxes (low-RAM, or under 3 GB) start with fewer particles. */
private fun initialQuality(context: Context): Quality {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return Quality.LOW
    val info = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
    return if (am.isLowRamDevice || info.totalMem < 3L * 1024 * 1024 * 1024) Quality.LOW else Quality.HIGH
}

/** Loads a scene's layers; returns null when there is no artwork for it. */
@Composable
fun rememberSceneLayers(farId: Int, nearId: Int): Pair<ImageBitmap, ImageBitmap?>? {
    if (farId == 0) return null
    val far = ImageBitmap.imageResource(farId)
    val near = if (nearId != 0) ImageBitmap.imageResource(nearId) else null
    return far to near
}
