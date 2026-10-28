package net.rubinny.willowbrook.data.profiles

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.UUID

class ProfileRepository(
    private val dataStore: DataStore<Preferences>,
    private val clock: () -> Long = System::currentTimeMillis,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(Profile.serializer())

    val profiles: Flow<List<Profile>> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { decode(it[KEY_PROFILES]) }

    suspend fun get(id: String): Profile? = profiles.first().firstOrNull { it.id == id }

    sealed interface AddResult {
        data class Added(val profile: Profile) : AddResult
        data class Rejected(val message: String) : AddResult
    }

    suspend fun add(rawNickname: String, avatar: Avatar): AddResult {
        val nickname = when (val v = ProfileRules.validateNickname(rawNickname)) {
            is ProfileRules.NicknameResult.Invalid -> return AddResult.Rejected(v.message)
            is ProfileRules.NicknameResult.Valid -> v.nickname
        }
        var result: AddResult = AddResult.Rejected("Something went wrong. Please try again.")
        dataStore.edit { prefs ->
            val current = decode(prefs[KEY_PROFILES])
            result = when {
                current.size >= ProfileRules.MAX_PROFILES ->
                    AddResult.Rejected("There can be up to ${ProfileRules.MAX_PROFILES} players.")
                current.any { it.nickname.equals(nickname, ignoreCase = true) } ->
                    AddResult.Rejected("That name is already used.")
                else -> {
                    val profile = Profile(idFactory(), nickname, avatar, clock())
                    prefs[KEY_PROFILES] = json.encodeToString(serializer, current + profile)
                    AddResult.Added(profile)
                }
            }
        }
        return result
    }

    suspend fun delete(id: String) {
        dataStore.edit { prefs ->
            val remaining = decode(prefs[KEY_PROFILES]).filterNot { it.id == id }
            prefs[KEY_PROFILES] = json.encodeToString(serializer, remaining)
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.remove(KEY_PROFILES) }
    }

    /** A corrupted value must never crash the app; it is treated as "no profiles". */
    private fun decode(raw: String?): List<Profile> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(serializer, raw)
        } catch (_: SerializationException) {
            emptyList()
        } catch (_: IllegalArgumentException) {
            emptyList()
        }
    }

    private companion object {
        val KEY_PROFILES = stringPreferencesKey("profiles_json")
    }
}
