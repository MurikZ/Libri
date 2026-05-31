package com.libri.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.libri.app.presentation.theme.OnBackground
import com.libri.app.presentation.theme.Primary
import com.libri.app.presentation.theme.Surface

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Catalog : BottomNavItem("catalog", Icons.Default.MenuBook, "Каталог")
    object MyBooks : BottomNavItem("my_books", Icons.Default.Book, "Мои книги")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Профиль")
    object Librarian : BottomNavItem("librarian", Icons.Default.ManageAccounts, "Управление")
}

@Composable
fun BottomNavBar(navController: NavController, items: List<BottomNavItem>) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    NavigationBar(containerColor = Surface) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = Surface,
                    unselectedIconColor = OnBackground.copy(alpha = 0.6f),
                    unselectedTextColor = OnBackground.copy(alpha = 0.6f)
                )
            )
        }
    }
}
