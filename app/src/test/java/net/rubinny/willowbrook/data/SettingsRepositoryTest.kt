package net.rubinny.willowbrook.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.rubinny.willowbrook.data.settings.Difficulty
import net.rubinny.willowbrook.data.settings.Settings
import net.rubinny.willowbrook.data.settings.SettingsLimits
import net.rubinny.willowbrook.data.settings.SettingsRepository
import net.rubinny.willowbrook.data.settings.sanitized
import net.rubinny.willowbrook.newDataStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test fun `defaults are senior friendly`() = runBlocking {
        val s = SettingsRepository(tmp.newDataStore()).current()
        assertEquals(Settings(), s)
        assertEquals(true, s.captionsEnabled)
        assertEquals(true, s.narrationEnabled)
        assertEquals(Difficulty.AUTO, s.difficulty)
    }

    @Test fun `updates persist`() = runBlocking {
        val repo = SettingsRepository(tmp.newDataStore())
        repo.update { it.copy(textScale = 1.4f, highContrast = true, difficulty = Difficulty.GENTLE, lastProfileId = "p1") }
        val s = repo.current()
        assertEquals(1.4f, s.textScale)
        assertEquals(true, s.highContrast)
        assertEquals(Difficulty.GENTLE, s.difficulty)
        assertEquals("p1", s.lastProfileId)
    }

    @Test fun `values are clamped to safe ranges`() = runBlocking {
        val repo = SettingsRepository(tmp.newDataStore())
        repo.update { it.copy(textScale = 9f, speechRate = 0.01f, hintDelaySec = 999, musicVolume = -3f, ambienceVolume = 7f) }
        val s = repo.current()
        assertEquals(SettingsLimits.TEXT_SCALE_MAX, s.textScale)
        assertEquals(SettingsLimits.SPEECH_RATE_MIN, s.speechRate)
        assertEquals(SettingsLimits.HINT_DELAY_MAX, s.hintDelaySec)
        assertEquals(SettingsLimits.MUSIC_VOLUME_MIN, s.musicVolume)
        assertEquals(SettingsLimits.MUSIC_VOLUME_MAX, s.ambienceVolume)
    }

    @Test fun `stepping does not accumulate float drift`() {
        var s = Settings()
        repeat(3) { s = s.copy(textScale = s.textScale + SettingsLimits.TEXT_SCALE_STEP).sanitized() }
        assertEquals(1.6f, s.textScale)
        repeat(3) { s = s.copy(textScale = s.textScale - SettingsLimits.TEXT_SCALE_STEP).sanitized() }
        assertEquals(1.0f, s.textScale)
    }

    @Test fun `pin state lifecycle`() = runBlocking {
        val repo = SettingsRepository(tmp.newDataStore())
        assertNull(repo.pinState.first().hash)
        repo.setPinHash("pbkdf2\$1\$aa\$bb")
        repo.recordPinFailure(3, 0L)
        assertEquals(3, repo.pinState.first().failedAttempts)
        repo.clearPinFailures()
        assertEquals(0, repo.pinState.first().failedAttempts)
        assertEquals("pbkdf2\$1\$aa\$bb", repo.pinState.first().hash)
    }

    @Test fun `clearAll removes pin and settings`() = runBlocking {
        val repo = SettingsRepository(tmp.newDataStore())
        repo.setPinHash("x")
        repo.update { it.copy(textScale = 1.6f) }
        repo.clearAll()
        assertNull(repo.pinState.first().hash)
        assertEquals(Settings(), repo.current())
    }
}
