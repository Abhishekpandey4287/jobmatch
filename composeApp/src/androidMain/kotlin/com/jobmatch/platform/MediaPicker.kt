package com.jobmatch.platform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

actual object MediaPicker {

    @Composable
    actual fun registerPicker(
        onFilePicked: (String?) -> Unit
    ): () -> Unit {

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            onFilePicked(
                uri?.lastPathSegment ?: uri?.toString()
            )
        }

        return {
            launcher.launch("*/*")
        }
    }
}