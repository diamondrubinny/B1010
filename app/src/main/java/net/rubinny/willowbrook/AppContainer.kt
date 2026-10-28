package net.rubinny.willowbrook

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import net.rubinny.willowbrook.data.profiles.ProfileRepository
import net.rubinny.willowbrook.data.settings.SettingsRepository

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "willowbrook")

/** Manual dependency container — small enough that a DI framework would add more weight than value. */
class AppContainer(context: Context) {
    private val dataStore = context.applicationContext.appDataStore

    val settingsRepository = SettingsRepository(dataStore)
    val profileRepository = ProfileRepository(dataStore)

    /** "Delete all data": profiles, progress, settings and PIN. Cannot be undone. */
    suspend fun deleteAllData() {
        dataStore.edit { it.clear() }
    }
}
