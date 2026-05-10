package com.jobmatch.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** Platform provides the DataStore instance (Android context, iOS NSDocumentDirectory). */
expect fun createDataStore(): DataStore<Preferences>