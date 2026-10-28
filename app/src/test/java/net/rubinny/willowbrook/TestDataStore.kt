package net.rubinny.willowbrook

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.rules.TemporaryFolder

/** Real file-backed DataStore in a temp folder, so tests exercise actual persistence. */
fun TemporaryFolder.newDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + SupervisorJob())) {
        newFile("test-${System.nanoTime()}.preferences_pb").also { it.delete() }
    }
