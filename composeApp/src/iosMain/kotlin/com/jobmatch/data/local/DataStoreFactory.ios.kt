package com.jobmatch.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun createDataStore(): DataStore<Preferences> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory         = NSDocumentDirectory,
        inDomain          = NSUserDomainMask,
        appropriateForURL = null,
        create            = false,
        error             = null,
    )!!.path!!

    return androidx.datastore.preferences.preferencesDataStore(
        path = "$documentDirectory/jobmatch_prefs.preferences_pb"
    )
}