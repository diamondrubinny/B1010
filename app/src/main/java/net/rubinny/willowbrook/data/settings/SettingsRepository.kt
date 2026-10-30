package net.rubinny.willowbrook.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<Settings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toSettings() }

    suspend fun current(): Settings = settings.first()

    suspend fun update(transform: (Settings) -> Settings) {
        dataStore.edit { prefs ->
            val next = transform(prefs.toSettings()).sanitized()
            prefs[Keys.TEXT_SCALE] = next.textScale
            prefs[Keys.SPEECH_RATE] = next.speechRate
            prefs[Keys.NARRATION] = next.narrationEnabled
            prefs[Keys.CAPTIONS] = next.captionsEnabled
            prefs[Keys.DIFFICULTY] = next.difficulty.name
            prefs[Keys.HINT_DELAY] = next.hintDelaySec
            prefs[Keys.REDUCE_MOTION] = next.reduceMotion
            prefs[Keys.HIGH_CONTRAST] = next.highContrast
            prefs[Keys.MUSIC_VOLUME] = next.musicVolume
            prefs[Keys.BACKGROUND_MUSIC] = next.backgroundMusicVolume
            prefs[Keys.AMBIENCE] = next.ambienceVolume
            if (next.lastProfileId == null) prefs.remove(Keys.LAST_PROFILE) else prefs[Keys.LAST_PROFILE] = next.lastProfileId
        }
    }

    /**
     * Earlier versions kept a caregiver PIN (salted hash and lockout state). Settings no longer have a PIN,
     * so anything left from it is removed rather than kept on the device without a purpose.
     */
    suspend fun removeLegacyPin() {
        dataStore.edit {
            it.remove(Keys.LEGACY_PIN_HASH)
            it.remove(Keys.LEGACY_PIN_FAILED)
            it.remove(Keys.LEGACY_PIN_LOCKED_UNTIL)
        }
    }

    /** Removes every setting. Used by "Delete all data". */
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    private fun Preferences.toSettings(): Settings {
        val defaults = Settings()
        return Settings(
            textScale = this[Keys.TEXT_SCALE] ?: defaults.textScale,
            speechRate = this[Keys.SPEECH_RATE] ?: defaults.speechRate,
            narrationEnabled = this[Keys.NARRATION] ?: defaults.narrationEnabled,
            captionsEnabled = this[Keys.CAPTIONS] ?: defaults.captionsEnabled,
            difficulty = this[Keys.DIFFICULTY]
                ?.let { name -> Difficulty.entries.firstOrNull { it.name == name } }
                ?: defaults.difficulty,
            hintDelaySec = this[Keys.HINT_DELAY] ?: defaults.hintDelaySec,
            reduceMotion = this[Keys.REDUCE_MOTION] ?: defaults.reduceMotion,
            highContrast = this[Keys.HIGH_CONTRAST] ?: defaults.highContrast,
            musicVolume = this[Keys.MUSIC_VOLUME] ?: defaults.musicVolume,
            backgroundMusicVolume = this[Keys.BACKGROUND_MUSIC] ?: defaults.backgroundMusicVolume,
            ambienceVolume = this[Keys.AMBIENCE] ?: defaults.ambienceVolume,
            lastProfileId = this[Keys.LAST_PROFILE],
        ).sanitized()
    }

    private object Keys {
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val NARRATION = booleanPreferencesKey("narration_enabled")
        val CAPTIONS = booleanPreferencesKey("captions_enabled")
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val HINT_DELAY = intPreferencesKey("hint_delay_sec")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val BACKGROUND_MUSIC = floatPreferencesKey("background_music_volume")
        val AMBIENCE = floatPreferencesKey("ambience_volume")
        val LAST_PROFILE = stringPreferencesKey("last_profile_id")
        val LEGACY_PIN_HASH = stringPreferencesKey("pin_hash")
        val LEGACY_PIN_FAILED = intPreferencesKey("pin_failed_attempts")
        val LEGACY_PIN_LOCKED_UNTIL = longPreferencesKey("pin_locked_until")
    }
}
