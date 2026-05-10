package com.jobmatch.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.compose.*
import com.jobmatch.di.AppDependencies
import com.jobmatch.presentation.applications.MyApplicationsScreen
import com.jobmatch.presentation.auth.OtpScreen
import com.jobmatch.presentation.auth.PhoneScreen
import com.jobmatch.presentation.jobs.JobDetailScreen
import com.jobmatch.presentation.jobs.JobFeedScreen
import com.jobmatch.presentation.profile.ProfileSetupScreen
import kotlinx.coroutines.flow.map


object Routes {
    const val PHONE         = "phone"
    const val OTP           = "otp/{phone}"
    const val PROFILE_SETUP = "profile_setup"
    const val JOB_FEED      = "jobs"
    const val JOB_DETAIL    = "jobs/{jobId}"
    const val APPLICATIONS  = "applications"

    fun otp(phone: String)       = "otp/$phone"
    fun jobDetail(jobId: Long)   = "jobs/$jobId"
}


private enum class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    JOBS(
        route          = Routes.JOB_FEED,
        label          = "Jobs",
        selectedIcon   = Icons.Filled.Work,
        unselectedIcon = Icons.Outlined.Work,
    ),
    APPLICATIONS(
        route          = Routes.APPLICATIONS,
        label          = "Applied",
        selectedIcon   = Icons.Filled.Assignment,
        unselectedIcon = Icons.Outlined.Assignment,
    ),
}

private val bottomNavRoutes = setOf(Routes.JOB_FEED, Routes.APPLICATIONS)


@Composable
fun JobMatchNavHost() {
    val navController  = rememberNavController()
    val sessionManager = AppDependencies.sessionManager

    /**
     * Start destination logic:
     *  - No token           → Phone (login)
     *  - Token + no profile → ProfileSetup  (user logged in before but never completed profile)
     *  - Token + profile    → JobFeed
     *
     * We use "phone" as safe initial value while the DataStore read resolves.
     */
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        sessionManager.tokenFlow.collect { token ->
            if (startDestination == null) {
                startDestination = if (token != null) Routes.JOB_FEED else Routes.PHONE
            }
        }
    }

    // Show nothing until we know the start destination (avoids flash of wrong screen)
    if (startDestination == null) return

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.JOB_FEED) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = {
                                Icon(
                                    if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label,
                                )
                            },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController    = navController,
            startDestination = startDestination!!,
            modifier         = Modifier.padding(innerPadding),
        ) {

            composable(Routes.PHONE) {
                PhoneScreen(
                    onOtpSent = { phone ->
                        navController.navigate(Routes.otp(phone))
                    }
                )
            }

            composable(
                route     = Routes.OTP,
                arguments = listOf(navArgument("phone") { type = NavType.StringType }),
            ) { backStack ->
                val phone = backStack.arguments?.getString("phone") ?: ""
                OtpScreen(
                    phone      = phone,
                    onVerified = { isNewUser ->
                        if (isNewUser) {
                            navController.navigate(Routes.PROFILE_SETUP) {
                                popUpTo(Routes.PHONE) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.JOB_FEED) {
                                popUpTo(Routes.PHONE) { inclusive = true }
                            }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.PROFILE_SETUP) {
                ProfileSetupScreen(
                    onComplete = {
                        navController.navigate(Routes.JOB_FEED) {
                            popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.JOB_FEED) {
                JobFeedScreen(
                    onJobClick = { jobId ->
                        navController.navigate(Routes.jobDetail(jobId))
                    }
                )
            }

            composable(
                route     = Routes.JOB_DETAIL,
                arguments = listOf(navArgument("jobId") { type = NavType.LongType }),
            ) { backStack ->
                val jobId = backStack.arguments?.getLong("jobId") ?: 0L
                JobDetailScreen(
                    jobId  = jobId,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.APPLICATIONS) {
                MyApplicationsScreen()
            }
        }
    }
}