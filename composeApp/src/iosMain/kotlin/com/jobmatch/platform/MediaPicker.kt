package com.jobmatch.platform

import androidx.compose.runtime.Composable

actual object MediaPicker {

    @Composable
    actual fun registerPicker(
        onFilePicked: (String?) -> Unit
    ): () -> Unit {

        return {
            // TODO Implement iOS picker later
        }
    }
}