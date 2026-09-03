package com.bloom.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bloom.app.ui.screens.AffirmationsScreen
import com.bloom.app.ui.screens.CycleScreen
import com.bloom.app.ui.screens.DietScreen
import com.bloom.app.ui.screens.HabitsScreen
import com.bloom.app.ui.screens.JournalScreen
import com.bloom.app.ui.theme.BloomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BloomTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BloomApp()
                }
            }
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val navItems = listOf(
    NavItem("journal", "Дневник", Icons.Filled.Favorite),
    NavItem("habits", "Привычки", Icons.Filled.CheckCircle),
    NavItem("cycle", "Цикл", Icons.Filled.DateRange),
    NavItem("diet", "Питание", Icons.Filled.Restaurant),
    NavItem("affirmations", "Вдохновение", Icons.Filled.Star)
)

@Composable
fun BloomApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "journal",
            modifier = Modifier.padding(padding)
        ) {
            composable("journal") { JournalScreen() }
            composable("habits") { HabitsScreen() }
            composable("cycle") { CycleScreen() }
            composable("diet") { DietScreen() }
            composable("affirmations") { AffirmationsScreen() }
        }
    }
}
