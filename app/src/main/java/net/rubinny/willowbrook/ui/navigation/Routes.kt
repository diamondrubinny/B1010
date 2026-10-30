package net.rubinny.willowbrook.ui.navigation

import android.net.Uri
import net.rubinny.willowbrook.data.content.ContentRepository

object Routes {
    const val PROFILES = "profiles"
    const val CREATE_PROFILE = "profiles/new"
    const val HOME = "home/{profileId}"
    const val PLAY = "play/{profileId}?chapter={chapter}&case={case}"
    const val CASE_FILES = "casefiles/{profileId}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val SUMMARY = "summary"

    const val ARG_PROFILE_ID = "profileId"
    const val ARG_CHAPTER = "chapter"
    const val ARG_CASE = "case"

    /** The standalone bead game (assets/cases/beads.json), played like a replay: it never changes story progress. */
    const val BEADS_CASE = "beads"

    fun home(profileId: String) = "home/${Uri.encode(profileId)}"
    /** [replayChapter] = null continues saved progress; a number replays that chapter. */
    fun play(profileId: String, replayChapter: Int? = null, caseId: String = ContentRepository.DEFAULT_CASE) =
        "play/${Uri.encode(profileId)}?chapter=${replayChapter ?: -1}&case=${Uri.encode(caseId)}"
    fun beads(profileId: String) = play(profileId, replayChapter = 0, caseId = BEADS_CASE)
    fun caseFiles(profileId: String) = "casefiles/${Uri.encode(profileId)}"
}
