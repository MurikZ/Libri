package com.libri.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.libri.app.data.entity.UserRole
import com.libri.app.presentation.auth.LoginScreen
import com.libri.app.presentation.auth.RegisterScreen
import com.libri.app.presentation.catalog.BookDetailScreen
import com.libri.app.presentation.catalog.CatalogScreen
import com.libri.app.presentation.librarian.LibrarianScreen
import com.libri.app.presentation.loans.MyBooksScreen
import com.libri.app.presentation.profile.ProfileScreen
import com.libri.app.presentation.theme.Primary

sealed class SessionState {
    object Loading : SessionState()
    object LoggedOut : SessionState()
    data class LoggedIn(val userId: Long, val role: UserRole) : SessionState()
}

@Composable
fun LibriApp(mainViewModel: MainViewModel = hiltViewModel()) {
    val sessionState by mainViewModel.sessionState.collectAsState()

    when (val state = sessionState) {
        SessionState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        }
        SessionState.LoggedOut -> AuthNavHost()
        is SessionState.LoggedIn -> MainNavHost(
            userId = state.userId,
            userRole = state.role,
            onLogout = mainViewModel::logout
        )
    }
}

@Composable
fun AuthNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
    }
}

@Composable
fun MainNavHost(userId: Long, userRole: UserRole, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val items = buildList {
        add(BottomNavItem.Catalog)
        add(BottomNavItem.MyBooks)
        add(BottomNavItem.Profile)
        if (userRole != UserRole.READER) add(BottomNavItem.Librarian)
    }

    val showBottomBar = currentRoute != "book_detail/{bookId}" &&
            !currentRoute.orEmpty().startsWith("book_detail/")

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController, items = items)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "catalog",
            modifier = Modifier.padding(padding)
        ) {
            composable("catalog") {
                CatalogScreen(navController = navController, userId = userId, userRole = userRole)
            }
            composable("book_detail/{bookId}") { backStack ->
                val bookId = backStack.arguments?.getString("bookId")?.toLongOrNull() ?: 0L
                BookDetailScreen(
                    bookId = bookId,
                    userId = userId,
                    userRole = userRole,
                    navController = navController
                )
            }
            composable("my_books") {
                MyBooksScreen(userId = userId)
            }
            composable("profile") {
                ProfileScreen(userId = userId, onLogout = onLogout)
            }
            if (userRole != UserRole.READER) {
                composable("librarian") {
                    LibrarianScreen(currentUserId = userId)
                }
            }
        }
    }
}
