package net.rubinny.willowbrook.game

import kotlinx.coroutines.runBlocking
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.game.stage.Area
import net.rubinny.willowbrook.game.stage.CameraView
import net.rubinny.willowbrook.game.stage.Emitter
import net.rubinny.willowbrook.game.stage.Motion
import net.rubinny.willowbrook.game.stage.ParticleKind
import net.rubinny.willowbrook.game.stage.ParticleSystem
import net.rubinny.willowbrook.game.stage.Quality
import net.rubinny.willowbrook.game.stage.QualityGovernor
import net.rubinny.willowbrook.game.stage.SceneFxCatalog
import net.rubinny.willowbrook.game.stage.StageCamera
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs

class StageEngineTest {

    @Test fun `camera never shows the edge of the picture, at any moment`() {
        val camera = StageCamera()
        var t = 0f
        while (t < 600f) {
            val v = camera.at(t)
            assertTrue("zoom at $t", v.zoom >= 1f)
            val limit = CameraView.maxPan(v.zoom)
            assertTrue("pan x at $t", abs(v.panX) <= limit + 1e-6f)
            assertTrue("pan y at $t", abs(v.panY) <= limit + 1e-6f)
            t += 0.25f
        }
    }

    @Test fun `camera opens a little closer and settles, slowly`() {
        val camera = StageCamera()
        assertTrue(camera.at(0f).zoom > camera.at(6f).zoom)
        // Calm: between two frames at 30 fps nothing moves more than a hair.
        var t = 0f
        while (t < 120f) {
            val a = camera.at(t)
            val b = camera.at(t + 1f / 30f)
            assertTrue(abs(a.zoom - b.zoom) < 0.002f)
            assertTrue(abs(a.panX - b.panX) < 0.0005f)
            t += 0.5f
        }
    }

    @Test fun `light flicker stays gentle`() {
        var t = 0f
        while (t < 120f) {
            val f = Motion.flicker(t, 0.12f, seed = 3)
            assertTrue(f in 0.88f..1.12f)
            t += 0.05f
        }
        assertTrue(Motion.MAX_FLICKER_HZ <= 0.5f) // far below 3 flashes per second (WCAG 2.3.1)
        assertTrue(SceneFxCatalog.all.values.flatMap { it.lights }.all { it.flicker <= 0.15f })
    }

    @Test fun `particles are deterministic, capped and stay in their area`() {
        for (kind in ParticleKind.entries) {
            val area = Area(0.2f, 0.2f, 0.6f, 0.7f)
            val emitter = Emitter(kind, area, count = 10, size = 0.01f, colors = listOf(0xFFFFFFFF))
            val a = ParticleSystem(emitter, seed = 7)
            val b = ParticleSystem(emitter, seed = 7)
            repeat(30 * 60) {
                a.update(1f / 30f)
                b.update(1f / 30f)
                a.particles.forEach { p ->
                    assertTrue("$kind x=${p.x}", p.x in area.left..area.right)
                    assertTrue("$kind y=${p.y}", p.y in area.top..area.bottom)
                    assertTrue(a.alpha(p) in 0f..1f)
                }
            }
            assertEquals(a.particles.map { it.x to it.y }, b.particles.map { it.x to it.y })
            assertEquals(10, a.particles.size)
            a.setQuality(Quality.LOW.particleFactor)
            assertEquals(5, a.active)
        }
    }

    @Test fun `a long pause does not make particles jump`() {
        val system = ParticleSystem(Emitter(ParticleKind.MOTE, Area(0f, 0f, 1f, 1f), 5, 0.01f, listOf(0xFFFFFFFF)), seed = 1)
        val before = system.particles.map { it.y }
        system.update(30f) // e.g. the TV went to the home screen for 30 seconds
        system.particles.zip(before).forEach { (p, y) -> assertTrue(abs(p.y - y) < 0.01f) }
    }

    @Test fun `quality drops on slow frames at once, and recovers only after a calm while`() {
        val governor = QualityGovernor(Quality.HIGH, windowFrames = 10, recoverSeconds = 5f)
        repeat(10) { governor.onFrame(50f) }
        assertEquals(Quality.LOW, governor.quality)
        repeat(100) { governor.onFrame(16f) } // 1.6 s of good frames: not yet
        assertEquals(Quality.LOW, governor.quality)
        repeat(300) { governor.onFrame(16f) }
        assertEquals(Quality.HIGH, governor.quality)
    }

    @Test fun `every scene in the story has effects and both artwork layers`() {
        val content = ContentRepository { path -> File("src/main/assets/$path").inputStream() }
        val scenes = runBlocking { content.load("lantern") }.chapters.mapNotNull { it.scene }.toSet()
        assertTrue(scenes.isNotEmpty())
        for (scene in scenes) {
            assertTrue("fx for $scene", SceneFxCatalog.forScene(scene) != null)
            assertTrue("far layer for $scene", File("src/main/res/drawable-nodpi/scene_$scene.webp").exists())
            assertTrue("near layer for $scene", File("src/main/res/drawable-nodpi/scene_${scene}_near.webp").exists())
        }
        for (fx in SceneFxCatalog.all.values) {
            fx.lights.forEach { assertTrue(it.x in 0f..1f && it.y in 0f..1f) }
            fx.emitters.forEach { assertTrue(it.area.left >= 0f && it.area.right <= 1f && it.area.top >= 0f && it.area.bottom <= 1f) }
        }
    }
}

class SceneSpotsTest {
    /** Objects placed on the scene never overlap, for every scene and any play area big enough to allow it. */
    @Test fun `scene spots never overlap`() {
        val objW = 176f
        val objH = 146f
        for (scene in listOf("cottage", "bakery", "garden", "library", "square", null)) {
            val spots = net.rubinny.willowbrook.game.stage.SceneSpots.forScene(scene)
            assertEquals(net.rubinny.willowbrook.game.stage.SceneSpots.MAX, spots.size)
            for (areaH in listOf(objH * net.rubinny.willowbrook.game.stage.SceneSpots.MIN_HEIGHT_FACTOR, 320f, 400f)) {
                for (areaW in listOf(objW * 5, 900f, 1200f)) {
                    val rects = spots.map { it.left(areaW, objW) to it.top(areaH, objH) }
                    for (i in rects.indices) for (j in i + 1 until rects.size) {
                        val (ax, ay) = rects[i]
                        val (bx, by) = rects[j]
                        val apart = abs(ax - bx) >= objW - 0.5f || abs(ay - by) >= objH - 0.5f
                        assertTrue("$scene $i/$j overlap at ${areaW}x$areaH", apart)
                    }
                    rects.forEach { (x, y) -> assertTrue(x >= 0f && x + objW <= areaW + 0.5f && y >= 0f && y + objH <= areaH + 0.5f) }
                }
            }
        }
    }
}
