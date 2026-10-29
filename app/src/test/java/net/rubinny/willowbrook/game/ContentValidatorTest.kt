package net.rubinny.willowbrook.game

import net.rubinny.willowbrook.data.content.ContentException
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.game.engine.ContentValidator
import net.rubinny.willowbrook.game.model.CaseContent
import net.rubinny.willowbrook.game.model.Step
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ContentValidatorTest {
    private val shippedText = File("src/main/assets/cases/lantern.json").readText()
    private val shipped: CaseContent = ContentRepository.parseAndValidate(shippedText)

    /** Returns a copy of the shipped case with one step replaced. */
    private fun withStep(id: String, transform: (Step) -> Step): CaseContent = shipped.copy(
        chapters = shipped.chapters.map { c -> c.copy(steps = c.steps.map { if (it.id == id) transform(it) else it }) },
    )

    private fun errors(case: CaseContent) = ContentValidator.validate(case)

    @Test fun `shipped case passes every content rule`() {
        assertEquals("lantern", shipped.id)
        assertTrue(shipped.chapters.size >= 3)
        assertEquals(emptyList<String>(), errors(shipped))
    }

    @Test fun `shipped content uses every activity type`() {
        val types = shipped.chapters.flatMap { it.steps }.map { it::class }.toSet()
        listOf(Step.HiddenObject::class, Step.MemoryMatch::class, Step.Choice::class, Step.Sorting::class, Step.Spell::class)
            .forEach { assertTrue("missing $it", it in types) }
    }

    @Test fun `theft and danger words are rejected`() {
        val bad = withStep("ch1_bramble_hello") { (it as Step.Narration).copy(text = "My charm was stolen by a thief!") }
        val e = errors(bad)
        assertTrue(e.toString(), e.any { "stolen" in it })
        assertTrue(e.toString(), e.any { "thief" in it })
    }

    @Test fun `banned words match whole words only`() {
        // "die" must not match "diet"; "rob" must not match "robin".
        val ok = withStep("ch0_intro") { (it as Step.Narration).copy(text = "A robin on a diet visits Willowbrook.") }
        assertEquals(emptyList<String>(), errors(ok))
    }

    @Test fun `overlong text is rejected`() {
        val bad = withStep("ch1_arrive") { (it as Step.Narration).copy(text = "A".repeat(ContentValidator.MAX_TEXT_LENGTH + 1)) }
        assertTrue(errors(bad).any { "too long" in it })
    }

    @Test fun `malformed keys are rejected, not ignored`() {
        val bad = shippedText.replaceFirst("\"id\": \"ch1_twinkle\"", "\"id \": \"ch1_twinkle\"")
        assertTrue(bad != shippedText)
        assertThrows { ContentRepository.parseAndValidate(bad) }
    }

    @Test fun `hidden object rules`() {
        assertTrue(errors(withStep("ch0_find_glasses") { (it as Step.HiddenObject).copy(targets = listOf("unicorn")) }).isNotEmpty())
        assertTrue(errors(withStep("ch0_find_glasses") { (it as Step.HiddenObject).copy(hints = emptyList()) }).isNotEmpty())
        assertTrue(
            errors(withStep("ch1_tidy_kitchen") { (it as Step.HiddenObject).copy(targets = it.targets + "apple", hints = it.hints + "x") })
                .any { "targets" in it },
        )
    }

    @Test fun `choice rules`() {
        val notAnOption = withStep("ch1_recall_fruit") { (it as Step.Choice).copy(answer = "apple") }
        assertTrue(errors(notAnOption).any { "answer" in it })
        val tooMany = withStep("ch1_recall_fruit") { (it as Step.Choice).copy(options = listOf("pear", "apple", "lemon", "cherries")) }
        assertTrue(errors(tooMany).any { "options" in it })
    }

    @Test fun `sorting rules`() {
        val emptyCategory = withStep("ch2_sort_basket") { s ->
            (s as Step.Sorting).copy(items = s.items.filter { it.category == "garden" })
        }
        assertTrue(errors(emptyCategory).any { "empty" in it })
        val unknownCategory = withStep("ch2_sort_basket") { s ->
            (s as Step.Sorting).copy(items = s.items.map { it.copy(category = "attic") })
        }
        assertTrue(errors(unknownCategory).any { "unknown category" in it })
    }

    @Test fun `spell rules`() {
        assertTrue(errors(withStep("ch2_bloom_spell") { (it as Step.Spell).copy(sequence = listOf("star")) }).isNotEmpty())
        assertTrue(errors(withStep("ch2_bloom_spell") { (it as Step.Spell).copy(sequence = listOf("star", "comet")) }).any { "unknown symbol" in it })
    }

    @Test fun `chat questions are open questions that never test memory`() {
        fun withChat(chat: List<String>) = shipped.copy(chapters = shipped.chapters.mapIndexed { i, c -> if (i == 1) c.copy(chat = chat) else c })
        assertTrue(errors(withChat(listOf("Do you remember your first school?"))).any { "test memory" in it })
        assertTrue(errors(withChat(listOf("Tell me about baking."))).any { "question" in it })
        assertTrue(errors(withChat(List(5) { "What do you like?" })).any { "more than" in it })
        assertTrue(errors(withChat(listOf("Did you visit a hospital?"))).any { "hospital" in it })
    }

    @Test fun `every chapter offers chat questions`() {
        shipped.chapters.forEach { assertTrue(it.id, it.chat.isNotEmpty()) }
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
