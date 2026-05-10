package com.jobmatch

import androidx.compose.runtime.Composable
import com.jobmatch.presentation.navigation.JobMatchNavHost
import com.jobmatch.presentation.theme.JobMatchTheme

/**
 * Application root.
 * Applies the design system theme and launches the navigation host.
 */
@Composable
fun App() {
    JobMatchTheme {
        JobMatchNavHost()
    }
}