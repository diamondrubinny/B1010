package net.rubinny.willowbrook.game.engine

import net.rubinny.willowbrook.game.model.CaseContent
import net.rubinny.willowbrook.game.model.Chapter
import net.rubinny.willowbrook.game.model.Step

/**
 * Something to say aloud. When [id] is set, a recorded file assets/voice/<id>.ogg is used if present;
 * otherwise (or for lines made up during play, id = null) the TV's text-to-speech reads [text].
 */
data class SpokenLine(val id: String?, val text: String)

/**
 * The single source of ids and wording for every fixed spoken line. The voice script for AI recording
 * (docs/voice/<case>.csv) is generated from here, so recorded files always match what the game says.
 */
object VoiceLines {
    fun chapterLabel(index: Int): String = if (index == 0) "Getting started" else "Chapter $index"

    fun chapterIntro(chapter: Chapter, index: Int) =
        SpokenLine("${chapter.id}_intro", "${chapterLabel(index)}. ${chapter.title}. ${chapter.goal}")

    fun recapMidChapter(chapter: Chapter) =
        SpokenLine("${chapter.id}_recap", "Welcome back! Last time: ${chapter.recap} Let's carry on.")

    fun recapAfterChapter(finished: Chapter) =
        SpokenLine("${finished.id}_recap_done", "Welcome back! Last time you finished ${finished.title}. Let's carry on.")

    fun chapterComplete(chapter: Chapter) =
        SpokenLine("${chapter.id}_complete", "Well done, detective! You finished ${chapter.title}.")

    fun caseSolved(case: CaseContent) =
        SpokenLine("${case.id}_solved", "You solved ${case.title}! You can play any chapter again from Case files.")

    fun chat(chapter: Chapter, index: Int): SpokenLine? =
        chapter.chat.getOrNull(index)?.let { SpokenLine("${chapter.id}_chat${index + 1}", it) }

    fun whatAreWeDoing(case: CaseContent, chapter: Chapter) =
        SpokenLine("${chapter.id}_what", "${case.premise} Right now: ${chapter.goal}")

    /** The main line of a step: story text, or the task of an activity. */
    fun stepLine(step: Step): SpokenLine = when (step) {
        is Step.Narration -> SpokenLine(step.id, step.text)
        is Step.Reward -> SpokenLine(step.id, step.text)
        is Step.HiddenObject -> SpokenLine("${step.id}_goal", step.goal)
        is Step.MemoryMatch -> SpokenLine("${step.id}_goal", step.goal)
        is Step.Sorting -> SpokenLine("${step.id}_goal", step.goal)
        is Step.Spell -> SpokenLine("${step.id}_goal", step.goal)
        is Step.Choice -> SpokenLine("${step.id}_question", step.question)
    }

    /** Fixed verbal hints authored in content (index is 0-based). */
    fun hint(step: Step, index: Int): SpokenLine? = when (step) {
        is Step.HiddenObject -> step.hints.getOrNull(index)?.let { SpokenLine("${step.id}_hint${index + 1}", it) }
        is Step.Choice -> if (index == 0) SpokenLine("${step.id}_hint1", step.hint) else null
        else -> null
    }

    /** Every fixed line of a case with its speaker, in story order — the recording script. */
    fun script(case: CaseContent): List<Pair<String, SpokenLine>> {
        val lines = mutableListOf<Pair<String, SpokenLine>>()
        case.chapters.forEachIndexed { index, chapter ->
            lines += "Narrator" to chapterIntro(chapter, index)
            lines += "Narrator" to recapMidChapter(chapter)
            lines += "Narrator" to recapAfterChapter(chapter)
            lines += "Narrator" to whatAreWeDoing(case, chapter)
            chapter.steps.forEach { step ->
                val speaker = (step as? Step.Narration)?.speaker ?: if (step is Step.Narration || step is Step.Reward) "Narrator" else "Marmalade"
                lines += speaker to stepLine(step)
                var i = 0
                while (true) {
                    val h = hint(step, i++) ?: break
                    lines += "Marmalade" to h
                }
            }
            lines += "Narrator" to chapterComplete(chapter)
            chapter.chat.indices.forEach { i -> lines += "Narrator" to chat(chapter, i)!! }
        }
        lines += "Narrator" to caseSolved(case)
        return lines
    }
}
