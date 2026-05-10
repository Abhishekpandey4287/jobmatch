package com.jobmatch.platform

import androidx.compose.runtime.Composable

expect object MediaPicker {

    @Composable
    fun registerPicker(
        onFilePicked: (String?) -> Unit
    ): () -> Unit
}