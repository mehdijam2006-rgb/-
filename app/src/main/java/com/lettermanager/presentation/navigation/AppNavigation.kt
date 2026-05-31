package com.lettermanager.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lettermanager.presentation.ui.archived.ArchivedScreen
import com.lettermanager.presentation.ui.auth.SettingsScreen
import com.lettermanager.presentation.ui.financial.FinancialScreen
import com.lettermanager.presentation.ui.letters.ActiveLettersScreen
import com.lettermanager.presentation.ui.letters.CreateLetterScreen
import com.lettermanager.presentation.ui.letters.LetterDetailScreen
import com.lettermanager.presentation.ui.search.SearchScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object ActiveLetters : Screen("active_letters", "فعال", Icons.Default.Mail)
    object Archived      : Screen("archived",        "بایگانی", Icons.Default.Archive)
    object Search        : Screen("search",          "جستجو",   Icons.Default.Search)
    object Financial     : Screen("financial",       "مالی",    Icons.Default.AccountBalance)

    object CreateLetter  : Screen("create_letter",   "نامه جدید", Icons.Default.Add)
    object Settings      : Screen("settings",        "تنظیمات",   Icons.Default.Settings)
    object LetterDetail  : Screen("letter_detail/{letterId}", "جزئیات", Icons.Default.Info) {
        fun createRoute(letterId: Int) = "letter_detail/$letterId"
    }
}

val bottomNavItems = listOf(
    Screen.ActiveLetters,
    Screen.Archived,
    Screen.Search,
    Screen.Financial
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon  = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        )
                    }
                    // Settings button in bottom nav
                    NavigationBarItem(
                        icon  = { Icon(Icons.Default.Settings, contentDescription = "تنظیمات") },
                        label = { Text("تنظیمات") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true,
                        onClick = { navController.navigate(Screen.Settings.route) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController   = navController,
            startDestination = Screen.ActiveLetters.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.ActiveLetters.route) {
                ActiveLettersScreen(
                    onNavigateToDetail  = { navController.navigate(Screen.LetterDetail.createRoute(it)) },
                    onNavigateToCreate  = { navController.navigate(Screen.CreateLetter.route) }
                )
            }
            composable(Screen.Archived.route) {
                ArchivedScreen(
                    onNavigateToDetail = { navController.navigate(Screen.LetterDetail.createRoute(it)) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateToDetail = { navController.navigate(Screen.LetterDetail.createRoute(it)) }
                )
            }
            composable(Screen.Financial.route) { FinancialScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.CreateLetter.route) {
                CreateLetterScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route     = Screen.LetterDetail.route,
                arguments = listOf(navArgument("letterId") { type = NavType.IntType })
            ) {
                LetterDetailScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
