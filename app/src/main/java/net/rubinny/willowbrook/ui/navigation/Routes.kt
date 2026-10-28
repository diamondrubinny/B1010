package net.rubinny.willowbrook.ui.navigation

import android.net.Uri

object Routes {
    const val PROFILES = "profiles"
    const val CREATE_PROFILE = "profiles/new"
    const val HOME = "home/{profileId}"
    const val CASE = "case/{profileId}"
    const val PIN_GATE = "pin"
    const val CHANGE_PIN = "pin/change"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    const val ARG_PROFILE_ID = "profileId"

    fun home(profileId: String) = "home/${Uri.encode(profileId)}"
    fun case(profileId: String) = "case/${Uri.encode(profileId)}"
}
