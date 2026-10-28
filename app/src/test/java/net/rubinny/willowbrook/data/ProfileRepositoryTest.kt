package net.rubinny.willowbrook.data

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.rubinny.willowbrook.data.profiles.Avatar
import net.rubinny.willowbrook.data.profiles.ProfileRepository
import net.rubinny.willowbrook.data.profiles.ProfileRepository.AddResult
import net.rubinny.willowbrook.data.profiles.ProfileRules
import net.rubinny.willowbrook.newDataStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProfileRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private var nextId = 0
    private fun repo(store: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> = tmp.newDataStore()) =
        ProfileRepository(store, clock = { 42L }, idFactory = { "id-${nextId++}" })

    @Test fun `add and read back`() = runBlocking {
        val r = repo()
        val result = r.add("  Ruth  ", Avatar.SAGE)
        assertTrue(result is AddResult.Added)
        val all = r.profiles.first()
        assertEquals(1, all.size)
        assertEquals("Ruth", all[0].nickname) // trimmed
        assertEquals(Avatar.SAGE, all[0].avatar)
        assertEquals(42L, all[0].createdAt)
    }

    @Test fun `at most four players`() = runBlocking {
        val r = repo()
        listOf("Ann", "Bob", "Cy", "Di").forEach { assertTrue(r.add(it, Avatar.SKY) is AddResult.Added) }
        val fifth = r.add("Ed", Avatar.SKY)
        assertTrue(fifth is AddResult.Rejected)
        assertEquals(ProfileRules.MAX_PROFILES, r.profiles.first().size)
    }

    @Test fun `duplicate names rejected case-insensitively`() = runBlocking {
        val r = repo()
        r.add("Moshe", Avatar.ROSE)
        assertTrue(r.add("moshe", Avatar.SKY) is AddResult.Rejected)
    }

    @Test fun `invalid names rejected`() = runBlocking {
        val r = repo()
        assertTrue(r.add("   ", Avatar.ROSE) is AddResult.Rejected)
        assertTrue(r.add("<script>", Avatar.ROSE) is AddResult.Rejected)
        assertTrue(r.add("A".repeat(ProfileRules.NICKNAME_MAX_LENGTH + 1), Avatar.ROSE) is AddResult.Rejected)
        assertTrue(r.profiles.first().isEmpty())
    }

    @Test fun `names with apostrophes, hyphens and non-latin letters are allowed`() {
        listOf("O'Brien", "Mary-Ann", "Dr. Lee", "Zoë", "שרה").forEach {
            assertTrue("$it should be valid", ProfileRules.validateNickname(it) is ProfileRules.NicknameResult.Valid)
        }
    }

    @Test fun `delete removes only that player`() = runBlocking {
        val r = repo()
        val a = (r.add("Ann", Avatar.SKY) as AddResult.Added).profile
        r.add("Bob", Avatar.SKY)
        r.delete(a.id)
        assertEquals(listOf("Bob"), r.profiles.first().map { it.nickname })
    }

    @Test fun `corrupted storage is treated as empty instead of crashing`() = runBlocking {
        val store = tmp.newDataStore()
        store.edit { it[stringPreferencesKey("profiles_json")] = "{not json" }
        val r = repo(store)
        assertTrue(r.profiles.first().isEmpty())
        assertTrue(r.add("Ann", Avatar.SKY) is AddResult.Added) // recovers
    }
}
