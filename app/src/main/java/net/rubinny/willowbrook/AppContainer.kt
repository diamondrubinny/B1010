package net.rubinny.willowbrook

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import net.rubinny.willowbrook.audio.AndroidNarrator
import net.rubinny.willowbrook.audio.MusicPlayer
import net.rubinny.willowbrook.audio.Narrator
import net.rubinny.willowbrook.audio.SoundPlayer
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.data.profiles.ProfileRepository
import net.rubinny.willowbrook.data.progress.ProgressRepository
import net.rubinny.willowbrook.data.settings.SettingsRepository

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "willowbrook")

/** Manual dependency container — small enough that a DI framework would add more weight than value. */
class AppContainer(context: Context) {
    private val dataStore = context.applicationContext.appDataStore

    val settingsRepository = SettingsRepository(dataStore)
    val profileRepository = ProfileRepository(dataStore)
    val progressRepository = ProgressRepository(dataStore)
    val contentRepository = ContentRepository { path -> context.applicationContext.assets.open(path) }

    /** Created on first use (text-to-speech start-up takes a moment) and kept for the app's lifetime. */
    val musicPlayer: MusicPlayer by lazy { MusicPlayer(context.applicationContext) }

    /** Scene sounds dip less than music while someone speaks: they are already very soft. */
    val ambiencePlayer: MusicPlayer by lazy { MusicPlayer(context.applicationContext, duckLevel = 0.5f) }
    val narrator: Narrator by lazy {
        AndroidNarrator(context.applicationContext).also { n -> n.onSpeakingChanged = { musicPlayer.setDucked(it); ambiencePlayer.setDucked(it) } }
    }
    val soundPlayer: SoundPlayer by lazy { SoundPlayer(context.applicationContext) }

    /** Removes a player together with all of their progress. */
    suspend fun deleteProfile(profileId: String) {
        progressRepository.deleteForProfile(profileId)
        profileRepository.delete(profileId)
        settingsRepository.update { if (it.lastProfileId == profileId) it.copy(lastProfileId = null) else it }
    }

    /** "Delete all data": profiles, progress, settings and PIN. Cannot be undone. */
    suspend fun deleteAllData() {
        dataStore.edit { it.clear() }
    }
}
