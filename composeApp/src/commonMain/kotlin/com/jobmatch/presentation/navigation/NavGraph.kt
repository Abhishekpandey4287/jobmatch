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
import com.jobmatch.presentation.profile.*


object Routes {
    const val PHONE         = "phone"
    const val OTP           = "otp/{phone}"
    const val PROFILE_SETUP = "profile_setup"  // shown ONCE after first login
    const val JOB_FEED      = "jobs"
    const val JOB_DETAIL    = "jobs/{jobId}"
    const val APPLICATIONS  = "applications"
    const val PROFILE       = "profile"        // editable profile tab

    fun otp(phone: String)     = "otp/$phone"
    fun jobDetail(jobId: Long) = "jobs/$jobId"
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom nav — only visible inside the main app (after auth + profile done)
// ─────────────────────────────────────────────────────────────────────────────

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
    APPLIED(
        route          = Routes.APPLICATIONS,
        label          = "Applied",
        selectedIcon   = Icons.Filled.Assignment,
        unselectedIcon = Icons.Outlined.Assignment,
    ),
    PROFILE(
        route          = Routes.PROFILE,
        label          = "Profile",
        selectedIcon   = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
    ),
}

private val bottomNavRoutes = setOf(
    Routes.JOB_FEED,
    Routes.APPLICATIONS,
    Routes.PROFILE,
)

// ─────────────────────────────────────────────────────────────────────────────
// Root nav host
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun JobMatchNavHost() {
    val navController  = rememberNavController()
    val sessionManager = AppDependencies.sessionManager

    //  Resolve start destination from persisted token.
    //  null = still loading (show nothing to avoid flash of wrong screen)
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        sessionManager.tokenFlow.collect { token ->
            if (startDestination == null) {
                startDestination = if (token != null) Routes.JOB_FEED else Routes.PHONE
            }
        }
    }

    if (startDestination == null) return   // splash / loading moment

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute      = currentBackStack?.destination?.route
    val showBottomBar     = currentRoute in bottomNavRoutes

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
                                    imageVector        = if (selected) tab.selectedIcon else tab.unselectedIcon,
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

            // ── STEP 1 ── Phone number entry ──────────────────────────────────
            composable(Routes.PHONE) {
                PhoneScreen(
                    onOtpSent = { phone ->
                        navController.navigate(Routes.otp(phone))
                    }
                )
            }

            // ── STEP 2 ── OTP verification ────────────────────────────────────
            composable(
                route     = Routes.OTP,
                arguments = listOf(navArgument("phone") { type = NavType.StringType }),
            ) { back ->
                val phone = back.arguments?.getString("phone") ?: ""
                OtpScreen(
                    phone      = phone,
                    onVerified = { isNewUser ->
                        if (isNewUser) {
                            // Profile not complete → force profile setup
                            navController.navigate(Routes.PROFILE_SETUP) {
                                popUpTo(Routes.PHONE) { inclusive = true }
                            }
                        } else {
                            // Returning user with complete profile → straight to jobs
                            navController.navigate(Routes.JOB_FEED) {
                                popUpTo(Routes.PHONE) { inclusive = true }
                            }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── STEP 3 ── Profile setup (FIRST TIME ONLY, no back button) ─────
            composable(Routes.PROFILE_SETUP) {
                ProfileSetupScreen(
                    onComplete = {
                        // Profile saved → now show job feed for the first time
                        navController.navigate(Routes.JOB_FEED) {
                            popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                        }
                    }
                )
            }

            // ── STEP 4 ── Job feed (scrollable, infinite scroll, real-time) ───
            composable(Routes.JOB_FEED) {
                JobFeedScreen(
                    onJobClick = { jobId ->
                        navController.navigate(Routes.jobDetail(jobId))
                    }
                )
            }

            // ── STEP 5 ── Job detail + Apply button ───────────────────────────
            composable(
                route     = Routes.JOB_DETAIL,
                arguments = listOf(navArgument("jobId") { type = NavType.LongType }),
            ) { back ->
                val jobId = back.arguments?.getLong("jobId") ?: 0L
                JobDetailScreen(
                    jobId  = jobId,
                    onBack = { navController.popBackStack() },
                )
            }

            // ── TAB 2 ── My applications (calls API on every visit) ────────────
            composable(Routes.APPLICATIONS) {
                MyApplicationsScreen()
            }

            // ── TAB 3 ── Editable profile + logout ────────────────────────────
            composable(Routes.PROFILE) {
                ProfileEditScreen(
                    onLogout = {
                        navController.navigate(Routes.PHONE) {
                            popUpTo(0) { inclusive = true }   // clear entire back stack
                        }
                    }
                )
            }
        }
    }
}