package com.petal.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.petal.app.ui.components.BottomNavBar
import com.petal.app.ui.screens.auth.ForgotPasswordScreen
import com.petal.app.ui.screens.auth.LoginScreen
import com.petal.app.ui.screens.auth.SignUpScreen
import com.petal.app.ui.screens.calendar.CalendarScreen
import com.petal.app.ui.screens.dashboard.DashboardScreen
import com.petal.app.ui.screens.education.ArticleScreen
import com.petal.app.ui.screens.education.EducationScreen
import com.petal.app.ui.screens.education.HealthQAScreen
import com.petal.app.ui.screens.insights.ChartsScreen
import com.petal.app.ui.screens.insights.CycleTrendsScreen
import com.petal.app.ui.screens.insights.DailyInsightsScreen
import com.petal.app.ui.screens.insights.RecommendationsScreen
import com.petal.app.ui.screens.log.QuickLogScreen
import com.petal.app.ui.screens.onboarding.ImportDataScreen
import com.petal.app.ui.screens.onboarding.OnboardingScreen
import com.petal.app.ui.screens.partner.CaregiverScreen
import com.petal.app.ui.screens.partner.PartnerDashboardScreen
import com.petal.app.ui.screens.partner.PartnerNotificationScreen
import com.petal.app.ui.screens.partner.PartnerSetupScreen
import com.petal.app.ui.screens.settings.DataExportScreen
import com.petal.app.ui.screens.settings.NotificationSettingsScreen
import com.petal.app.ui.screens.settings.PrivacySettingsScreen
import com.petal.app.ui.screens.settings.SettingsScreen
import com.petal.app.ui.screens.settings.ShareSettingsScreen
import com.petal.app.ui.viewmodel.AuthViewModel

@Composable
fun PetalNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)
    val hasOnboarded by authViewModel.hasOnboarded.collectAsState(initial = false)

    val startDestination = when {
        !isLoggedIn -> Screen.Login.route
        !hasOnboarded -> Screen.Onboarding.route
        else -> Screen.Dashboard.route
    }

    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    // Adaptive layout: use NavigationRail on wide screens (tablets)
    val configuration = LocalConfiguration.current
    val useRail = configuration.screenWidthDp >= 600

    val showBottomNav = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Calendar.route,
        Screen.QuickLog.route,
        Screen.QuickLog.baseRoute,
        Screen.Partner.route,
        Screen.Settings.route
    )

    val navContent: @Composable (Modifier) -> Unit = { modifier ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
            enterTransition = {
                fadeIn(animationSpec = tween(280)) +
                    slideInHorizontally(
                        initialOffsetX = { 80 },
                        animationSpec = tween(280)
                    )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(280)) +
                    slideInHorizontally(
                        initialOffsetX = { -80 },
                        animationSpec = tween(280)
                    )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) +
                    slideOutHorizontally(
                        targetOffsetX = { 80 },
                        animationSpec = tween(200)
                    )
            }
        ) {
            // Auth
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSignUpSuccess = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Onboarding
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.ImportData.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Import Data (onboarding step)
            composable(Screen.ImportData.route) {
                ImportDataScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onImportComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Main screens
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToInsights = { navController.navigate(Screen.DailyInsights.route) },
                    onNavigateToRecommendations = { navController.navigate(Screen.Recommendations.route) },
                    onNavigateToCharts = { navController.navigate(Screen.Charts.route) },
                    onNavigateToEducation = { navController.navigate(Screen.Education.route) },
                    onNavigateToQuickLog = { navController.navigate(Screen.QuickLog.createRoute()) }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onNavigateToLog = { dayInfo ->
                        navController.navigate(
                            Screen.QuickLog.createRoute(
                                date = dayInfo?.date?.toString(),
                                entryId = dayInfo?.entryId
                            )
                        )
                    }
                )
            }

            composable(
                route = Screen.QuickLog.route,
                arguments = listOf(
                    navArgument("date") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("entryId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                QuickLogScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Partner
            composable(Screen.Partner.route) {
                PartnerDashboardScreen(
                    onNavigateToSetup = { navController.navigate(Screen.PartnerSetup.route) },
                    onNavigateToCaregiver = { navController.navigate(Screen.Caregiver.route) }
                )
            }
            composable(Screen.PartnerSetup.route) {
                PartnerSetupScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Caregiver.route) {
                CaregiverScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PartnerNotifications.route) {
                PartnerNotificationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Insights
            composable(Screen.DailyInsights.route) {
                DailyInsightsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Recommendations.route) {
                RecommendationsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Charts.route) {
                ChartsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.CycleTrends.route) {
                CycleTrendsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Education
            composable(Screen.Education.route) {
                EducationScreen(
                    onNavigateToQA = { navController.navigate(Screen.HealthQA.route) },
                    onNavigateToArticle = { id -> navController.navigate(Screen.Article.createRoute(id)) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.HealthQA.route) {
                HealthQAScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Article.route) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                ArticleScreen(
                    articleId = articleId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings.route) },
                    onNavigateToPrivacy = { navController.navigate(Screen.PrivacySettings.route) },
                    onNavigateToSharing = { navController.navigate(Screen.ShareSettings.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PrivacySettings.route) {
                PrivacySettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ShareSettings.route) {
                ShareSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.DataExport.route) {
                DataExportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }

    // Adaptive layout: Rail on tablets, bottom bar on phones
    if (useRail && showBottomNav) {
        Row {
            BottomNavBar(
                currentRoute = currentRoute ?: "",
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                useRail = true
            )
            navContent(Modifier.weight(1f))
        }
    } else {
        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    BottomNavBar(
                        currentRoute = currentRoute ?: "",
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            navContent(Modifier.padding(innerPadding))
        }
    }
}
