package net.rubinny.willowbrook.game

import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.game.engine.VoiceLines
import net.rubinny.willowbrook.game.model.CaseContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Every case (the story and the standalone bead game) has an up-to-date recording script and recordings. */
class VoiceLinesTest {
    private val cases: List<CaseContent> = File("src/main/assets/cases").listFiles { f -> f.name.endsWith(".json") }!!
        .sortedBy { it.name }
        .map { ContentRepository.parseAndValidate(it.readText()) }

    private fun scriptFile(case: CaseContent) = File("../docs/voice/${case.id}.csv")

    private fun csv(case: CaseContent): String = buildString {
        appendLine("id,speaker,text")
        VoiceLines.script(case).forEach { (speaker, line) ->
            appendLine(listOf(line.id!!, speaker, line.text).joinToString(",") { "\"" + it.replace("\"", "\"\"") + "\"" })
        }
    }

    @Test fun `both cases are checked`() {
        assertEquals(listOf("beads", "lantern"), cases.map { it.id })
    }

    @Test fun `every fixed line has a unique, file-safe id`() {
        for (case in cases) {
            val ids = VoiceLines.script(case).map { it.second.id!! }
            assertEquals(case.id, ids.size, ids.toSet().size)
            ids.forEach { assertTrue(it, it.matches(Regex("[a-z0-9_]+"))) }
        }
    }

    /** The same id always means the same words, across cases (shared play-time phrases share one recording). */
    @Test fun `an id never means two different texts`() {
        val byId = cases.flatMap { VoiceLines.script(it) }.groupBy({ it.second.id!! }, { it.second.text })
        byId.forEach { (id, texts) -> assertEquals(id, 1, texts.toSet().size) }
    }

    /** Every scripted line has its recording, and no stale recording is left behind (wording changes change ids). */
    @Test fun `every line is recorded and no recording is orphaned`() {
        val voiceDir = File("src/main/assets/voice")
        val recorded = voiceDir.listFiles { f -> f.name.endsWith(".ogg") }!!.map { it.name.removeSuffix(".ogg") }.toSet()
        val scripted = cases.flatMap { VoiceLines.script(it) }.map { it.second.id!! }.toSet()
        assertEquals("Missing recordings", emptySet<String>(), scripted - recorded)
        assertEquals("Orphaned recordings", emptySet<String>(), recorded - scripted)
    }

    @Test fun `hints are included for recording`() {
        val ids = VoiceLines.script(cases.first { it.id == "lantern" }).map { it.second.id }
        assertTrue("ch1_tidy_kitchen_hint3" in ids)
        assertTrue("ch1_recall_fruit_hint1" in ids)
    }

    /**
     * The recording scripts must match the game. After changing story text, regenerate with:
     * ./gradlew :app:testDebugUnitTest -PupdateVoiceScript=true
     */
    @Test fun `voice scripts are up to date`() {
        for (case in cases) {
            val file = scriptFile(case)
            val expected = csv(case)
            if (System.getProperty("updateVoiceScript") == "true") {
                file.parentFile?.mkdirs()
                file.writeText(expected)
            }
            assertTrue("Missing ${file.path}; run with -PupdateVoiceScript=true", file.exists())
            assertEquals("Voice script ${file.name} is out of date; run with -PupdateVoiceScript=true", expected, file.readText())
        }
    }
}
