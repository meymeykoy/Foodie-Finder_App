package com.example.afinal.Food


import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text

@Composable
fun MyFoodSoApp() {
    val navController = rememberNavController()
    val viewModel: TheFoodSoViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "Home",
            Modifier.padding(innerPadding)
        ) {
            composable("Home") { MainScreenUI(navController, viewModel) }
            composable("Search") { SearchScreenUI(navController, viewModel) }
            composable("Favorite") { FavoriteScreen(navController, viewModel) }
            composable("AboutUs") { AboutUs(navController, viewModel) }
            composable("MealDetail/{mealId}") { backStackEntry ->
                val mealId = backStackEntry.arguments?.getString("mealId") ?: return@composable
                MealDetailScreen(mealId = mealId, navController = navController)
            }
        }
    }
}


@Composable
fun BottomNavBar(nc: NavHostController) {
    val currentRoute = nc.currentBackStackEntryAsState()?.value?.destination?.route

    BottomNavigation(
        backgroundColor = Color(0xFFDC3D74)
    ) {
        val items = listOf("Home", "Search", "Favorite", "AboutUs")

        items.forEach { screen ->
            val isSelected = currentRoute == screen

            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = getIcon(screen),
                        tint = if (isSelected) Color.Black else Color.White, // Black when selected, white otherwise
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = screen,
                        fontFamily = FontFamily.Serif,
                        color = if (isSelected) Color.Black else Color.White // Black text when selected
                    )
                },
                selected = isSelected,
                onClick = {
                    nc.navigate(screen) {
                        // Avoid multiple copies of the same destination
                        popUpTo(nc.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid reselecting the same item
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}



@Composable
fun getIcon(screen: String): ImageVector {
    return when (screen) {
        "Home" -> Icons.Filled.Home
        "Search" -> Icons.Filled.Search
        "Favorite" -> Icons.Filled.Favorite
        "AboutUs" -> Icons.Filled.Info
        else -> Icons.Filled.Home
    }
}
