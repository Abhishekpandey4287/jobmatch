package com.jobmatch.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "jobmatch_prefs")

private lateinit var appContext: Context

fun initDataStoreContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createDataStore(): DataStore<Preferences> = appContext.dataStore