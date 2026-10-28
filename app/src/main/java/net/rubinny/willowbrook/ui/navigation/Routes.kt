package net.rubinny.willowbrook.ui.navigation

import android.net.Uri

object Routes {
    const val PROFILES = "profiles"
    const val CREATE_PROFILE = "profiles/new"
    const val HOME = "home/{profileId}"
    const val PLAY = "play/{profileId}?chapter={chapter}"
    const val CASE_FILES = "casefiles/{profileId}"
    const val PIN_GATE = "pin"
    const val CHANGE_PIN = "pin/change"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    const val ARG_PROFILE_ID = "profileId"
    const val ARG_CHAPTER = "chapter"

    fun home(profileId: String) = "home/${Uri.encode(profileId)}"
    /** [replayChapter] = null continues saved progress; a number replays that chapter. */
    fun play(profileId: String, replayChapter: Int? = null) =
        "play/${Uri.encode(profileId)}?chapter=${replayChapter ?: -1}"
    fun caseFiles(profileId: String) = "casefiles/${Uri.encode(profileId)}"
}
