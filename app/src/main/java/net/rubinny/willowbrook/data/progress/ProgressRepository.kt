package net.rubinny.willowbrook.data.progress

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Where a player is in a case. Saved after every completed step.
 * When every chapter is done, [chapterIndex] equals the chapter count, so chapters added
 * in a later app update become the next chapter automatically.
 * [recentHintLevels] keeps only the last [MAX_HINT_HISTORY] activities (data minimisation);
 * it drives silent difficulty adjustment and the caregiver summary.
 */
@Serializable
data class CaseProgress(
    val caseId: String,
    val chapterIndex: Int = 0,
    val stepIndex: Int = 0,
    val completedChapters: List<String> = emptyList(),
    val recentHintLevels: List<Int> = emptyList(),
    val updatedAt: Long = 0L,
) {
    val hasStarted: Boolean get() = chapterIndex > 0 || stepIndex > 0 || completedChapters.isNotEmpty()

    companion object {
        const val MAX_HINT_HISTORY = 20
    }
}

class ProgressRepository(
    private val dataStore: DataStore<Preferences>,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun progress(profileId: String, caseId: String): Flow<CaseProgress> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { decode(it[key(profileId, caseId)]) ?: CaseProgress(caseId) }

    suspend fun get(profileId: String, caseId: String): CaseProgress = progress(profileId, caseId).first()

    suspend fun save(profileId: String, progress: CaseProgress) {
        val trimmed = progress.copy(
            recentHintLevels = progress.recentHintLevels.takeLast(CaseProgress.MAX_HINT_HISTORY),
            updatedAt = clock(),
        )
        dataStore.edit { it[key(profileId, progress.caseId)] = json.encodeToString(CaseProgress.serializer(), trimmed) }
    }

    /** Removes all progress of one player (called when the player is removed). */
    suspend fun deleteForProfile(profileId: String) {
        val prefix = "progress:${sanitize(profileId)}:"
        dataStore.edit { prefs ->
            prefs.asMap().keys.filter { it.name.startsWith(prefix) }.forEach { prefs.remove(it) }
        }
    }

    private fun decode(raw: String?): CaseProgress? = raw?.let {
        try {
            json.decodeFromString(CaseProgress.serializer(), it)
        } catch (_: Exception) {
            null // corrupted entry: start the case again rather than crash
        }
    }

    private fun key(profileId: String, caseId: String) = stringPreferencesKey("progress:${sanitize(profileId)}:${sanitize(caseId)}")

    private fun sanitize(s: String) = s.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
}
