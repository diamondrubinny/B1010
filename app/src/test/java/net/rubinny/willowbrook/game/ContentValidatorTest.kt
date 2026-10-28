package net.rubinny.willowbrook.game

import net.rubinny.willowbrook.data.content.ContentException
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.game.engine.ContentValidator
import net.rubinny.willowbrook.game.model.Step
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ContentValidatorTest {
    private val shippedText = File("src/main/assets/cases/lantern.json").readText()

    @Test fun `shipped case passes every content rule`() {
        val case = ContentRepository.parseAndValidate(shippedText) // throws with the full error list
        assertEquals("lantern", case.id)
        assertTrue(case.chapters.size >= 2)
    }

    @Test fun `every activity in shipped content is solvable`() {
        val case = ContentRepository.parseAndValidate(shippedText)
        case.chapters.flatMap { it.steps }.forEach { step ->
            when (step) {
                is Step.HiddenObject -> step.targets.forEach { assertTrue(case.item(it) != null) }
                is Step.MemoryMatch -> assertTrue(step.pairs.size >= ContentValidator.MIN_PAIRS)
                else -> Unit
            }
        }
    }

    @Test fun `theft and danger words are rejected`() {
        val bad = shippedText.replace(
            "rolled off my table while I was baking",
            "was stolen by a thief while I was baking",
        )
        val errors = ContentValidator.validate(ContentRepository.json.decodeFromString(
            net.rubinny.willowbrook.game.model.CaseContent.serializer(), bad))
        assertTrue(errors.any { "stolen" in it })
        assertTrue(errors.any { "thief" in it })
    }

    @Test fun `banned words match whole words only`() {
        // "die" must not match "diet"; "rob" must not match "robin".
        val ok = shippedText.replace("Welcome to Willowbrook, a quiet village", "A robin on a diet visits Willowbrook, a quiet village")
        ContentRepository.parseAndValidate(ok)
    }

    @Test fun `overlong text is rejected`() {
        val long = "A".repeat(ContentValidator.MAX_TEXT_LENGTH + 1)
        val bad = shippedText.replace("The bakery smells of warm bread. Mrs. Bramble is busy baking for the fair.", long)
        assertThrows { ContentRepository.parseAndValidate(bad) }
    }

    @Test fun `malformed keys are rejected, not ignored`() {
        val bad = shippedText.replaceFirst("\"id\": \"ch1_twinkle\"", "\"id \": \"ch1_twinkle\"")
        assertThrows { ContentRepository.parseAndValidate(bad) }
    }

    @Test fun `unknown item, missing hint and too many targets are rejected`() {
        val unknown = shippedText.replace("\"targets\": [\"glasses\"]", "\"targets\": [\"unicorn\"]")
        assertThrows { ContentRepository.parseAndValidate(unknown) }
        val noHint = shippedText.replace("\"hints\": [\"Your reading glasses have two round lenses.\"]", "\"hints\": []")
        assertThrows { ContentRepository.parseAndValidate(noHint) }
        val tooMany = shippedText.replace(
            "\"targets\": [\"honey\", \"bread\", \"strawberries\"]",
            "\"targets\": [\"honey\", \"bread\", \"strawberries\", \"apple\"]",
        )
        assertThrows { ContentRepository.parseAndValidate(tooMany) }
    }

    private fun assertThrows(block: () -> Unit) {
        try {
            block()
        } catch (_: ContentException) {
            return
        }
        throw AssertionError("Expected ContentException")
    }
}
