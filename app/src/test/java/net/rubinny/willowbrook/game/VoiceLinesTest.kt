package net.rubinny.willowbrook.game

import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.game.engine.VoiceLines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class VoiceLinesTest {
    private val case = ContentRepository.parseAndValidate(File("src/main/assets/cases/lantern.json").readText())
    private val scriptFile = File("../docs/voice/${case.id}.csv")

    private fun csv(): String = buildString {
        appendLine("id,speaker,text")
        VoiceLines.script(case).forEach { (speaker, line) ->
            appendLine(listOf(line.id!!, speaker, line.text).joinToString(",") { "\"" + it.replace("\"", "\"\"") + "\"" })
        }
    }

    @Test fun `every fixed line has a unique, file-safe id`() {
        val ids = VoiceLines.script(case).map { it.second.id!! }
        assertEquals(ids.size, ids.toSet().size)
        ids.forEach { assertTrue(it, it.matches(Regex("[a-z0-9_]+"))) }
    }

    /** Every scripted line has its recording, and no stale recording is left behind (wording changes change ids). */
    @Test fun `every line is recorded and no recording is orphaned`() {
        val voiceDir = File("src/main/assets/voice")
        val recorded = voiceDir.listFiles { f -> f.name.endsWith(".ogg") }!!.map { it.name.removeSuffix(".ogg") }.toSet()
        val scripted = VoiceLines.script(case).map { it.second.id!! }.toSet()
        assertEquals("Missing recordings", emptySet<String>(), scripted - recorded)
        assertEquals("Orphaned recordings", emptySet<String>(), recorded - scripted)
    }

    @Test fun `hints are included for recording`() {
        val ids = VoiceLines.script(case).map { it.second.id }
        assertTrue("ch1_tidy_kitchen_hint3" in ids)
        assertTrue("ch1_recall_fruit_hint1" in ids)
    }

    /**
     * The recording script must match the game. After changing story text, regenerate with:
     * ./gradlew :app:testDebugUnitTest -PupdateVoiceScript=true
     */
    @Test fun `voice script is up to date`() {
        val expected = csv()
        if (System.getProperty("updateVoiceScript") == "true") {
            scriptFile.parentFile.mkdirs()
            scriptFile.writeText(expected)
        }
        assertTrue("Missing ${scriptFile.path}; run with -PupdateVoiceScript=true", scriptFile.exists())
        assertEquals("Voice script is out of date; run with -PupdateVoiceScript=true", expected, scriptFile.readText())
    }
}
