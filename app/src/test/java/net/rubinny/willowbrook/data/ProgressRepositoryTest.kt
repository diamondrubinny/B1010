package net.rubinny.willowbrook.data

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.runBlocking
import net.rubinny.willowbrook.data.progress.CaseProgress
import net.rubinny.willowbrook.data.progress.ProgressRepository
import net.rubinny.willowbrook.newDataStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProgressRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test fun `new player starts at the beginning`() = runBlocking {
        val p = ProgressRepository(tmp.newDataStore()).get("p1", "lantern")
        assertEquals(CaseProgress("lantern"), p)
        assertFalse(p.hasStarted)
    }

    @Test fun `progress is saved per player`() = runBlocking {
        val repo = ProgressRepository(tmp.newDataStore(), clock = { 99L })
        repo.save("p1", CaseProgress("lantern", chapterIndex = 1, stepIndex = 2, completedChapters = listOf("ch0")))
        assertEquals(2, repo.get("p1", "lantern").stepIndex)
        assertEquals(99L, repo.get("p1", "lantern").updatedAt)
        assertEquals(0, repo.get("p2", "lantern").stepIndex)
    }

    @Test fun `hint history is trimmed (data minimisation)`() = runBlocking {
        val repo = ProgressRepository(tmp.newDataStore())
        repo.save("p1", CaseProgress("lantern", recentHintLevels = List(50) { it % 5 }))
        assertEquals(CaseProgress.MAX_HINT_HISTORY, repo.get("p1", "lantern").recentHintLevels.size)
    }

    @Test fun `deleting a player removes only their progress`() = runBlocking {
        val repo = ProgressRepository(tmp.newDataStore())
        repo.save("p1", CaseProgress("lantern", stepIndex = 3))
        repo.save("p2", CaseProgress("lantern", stepIndex = 4))
        repo.deleteForProfile("p1")
        assertEquals(0, repo.get("p1", "lantern").stepIndex)
        assertEquals(4, repo.get("p2", "lantern").stepIndex)
    }

    @Test fun `corrupted progress restarts instead of crashing`() = runBlocking {
        val store = tmp.newDataStore()
        store.edit { it[stringPreferencesKey("progress:p1:lantern")] = "not json" }
        assertEquals(CaseProgress("lantern"), ProgressRepository(store).get("p1", "lantern"))
    }
}
