package net.rubinny.willowbrook.audio

import kotlinx.coroutines.runBlocking
import net.rubinny.willowbrook.data.content.ContentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AudioCatalogTest {
    private val scenes = runBlocking {
        ContentRepository { path -> File("src/main/assets/$path").inputStream() }.load("lantern")
    }.chapters.mapNotNull { it.scene }

    @Test fun `every scene has its own music and scene sounds, and the files exist`() {
        val tracks = scenes.map { MusicTrack.forScene(it) }
        assertEquals("each place sounds different", scenes.size, tracks.toSet().size)
        for (scene in scenes) assertNotNull("ambience for $scene", Ambience.forScene(scene))
        for (name in MusicTrack.entries.map { "music_" + it.name.lowercase() } + Ambience.entries.map { "amb_" + it.name.lowercase() }) {
            assertTrue("$name.ogg", File("src/main/res/raw/$name.ogg").exists())
        }
    }

    @Test fun `the square is the finale piece, and an unknown scene falls back calmly`() {
        assertEquals(MusicTrack.FINALE, MusicTrack.forScene("square"))
        assertEquals(MusicTrack.COTTAGE, MusicTrack.forScene(null))
        assertEquals(null, Ambience.forScene("nowhere"))
    }
}
