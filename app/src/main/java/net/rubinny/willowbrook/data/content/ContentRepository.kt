package net.rubinny.willowbrook.data.content

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.rubinny.willowbrook.game.engine.ContentValidator
import net.rubinny.willowbrook.game.model.CaseContent
import java.io.InputStream

/** Thrown when bundled content is unreadable or breaks content rules. Shown as a friendly error screen. */
class ContentException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Loads story content packaged in the APK (never from the network) and validates it.
 * [openAsset] is injected so the repository can be unit-tested without Android.
 */
class ContentRepository(private val openAsset: (String) -> InputStream) {
    private val mutex = Mutex()
    private val cache = mutableMapOf<String, CaseContent>()

    suspend fun load(caseId: String): CaseContent = mutex.withLock {
        cache[caseId] ?: withContext(Dispatchers.IO) { parse(caseId) }.also { cache[caseId] = it }
    }

    private fun parse(caseId: String): CaseContent {
        require(caseId.matches(Regex("[a-z0-9_]+"))) { "Invalid case id" }
        val text = try {
            openAsset("cases/$caseId.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            throw ContentException("Case '$caseId' could not be read", e)
        }
        return parseAndValidate(text)
    }

    companion object {
        const val DEFAULT_CASE = "lantern"

        val json = Json { ignoreUnknownKeys = false; classDiscriminator = "type" }

        fun parseAndValidate(text: String): CaseContent {
            val content = try {
                json.decodeFromString(CaseContent.serializer(), text)
            } catch (e: Exception) {
                throw ContentException("Case file is not valid", e)
            }
            val errors = ContentValidator.validate(content)
            if (errors.isNotEmpty()) throw ContentException("Case '${content.id}' breaks content rules: ${errors.joinToString("; ")}")
            return content
        }
    }
}
