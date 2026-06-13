package com.turkcell.lyraapp.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.turkcell.lyraapp.ui.auth.login.LoginRoute
import com.turkcell.lyraapp.ui.auth.register.RegisterRoute
import com.turkcell.lyraapp.ui.home.HomeRoute
import com.turkcell.lyraapp.ui.library.LibraryRoute
import com.turkcell.lyraapp.ui.likedsongs.LikedSongsRoute
import com.turkcell.lyraapp.ui.profile.ProfileRoute
import com.turkcell.lyraapp.ui.search.SearchRoute

/**
 * Uygulamanın iskelet navigasyon yapısı.
 *
 * Tek [NavHost], Auth grafiği ile ana akış sekmelerini barındırır; başlangıç hedefi
 * [LyraDestination.Login]'dir. Dış [Scaffold]'ın `bottomBar` yuvasındaki [LyraBottomBar]
 * yalnızca üst düzey sekme rotalarında görünür; böylece çubuk her ana sayfanın altında
 * yer alır, Auth ekranlarında gizlenir.
 *
 * Her ekranın `Route` composable'ı, MVI Effect'lerini buradan sağlanan navigasyon
 * lambda'larına köprüler (ViewModel navigasyon API'si bilmez; bkz. mvi-viewmodel-rules §6).
 *
 * Dış Scaffold'ın `contentWindowInsets`'i sıfırlanır: sistem çubuğu boşluklarını her ekran
 * kendisi yönetir (Login/Register'da olduğu gibi); içerik dolgusu yalnızca alt çubuğun
 * yüksekliğini taşır.
 */
@Composable
fun LyraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isTopLevelRoute(currentRoute)) {
                LyraBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = navController::navigateToTab,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LyraDestination.Login.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(LyraDestination.Login.route) {
                LoginRoute(
                    onNavigateToHome = { navController.navigateToHomeClearingAuth() },
                    onNavigateToRegister = {
                        navController.navigate(LyraDestination.Register.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(LyraDestination.Register.route) {
                RegisterRoute(
                    onNavigateToHome = { navController.navigateToHomeClearingAuth() },
                    onNavigateToLogin = {
                        navController.navigate(LyraDestination.Login.route) {
                            popUpTo(LyraDestination.Login.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(LyraDestination.Home.route) { HomeRoute() }
            composable(LyraDestination.Search.route) { SearchRoute() }
            composable(LyraDestination.Library.route) {
                LibraryRoute(
                    onNavigateToLikedSongs = {
                        navController.navigateToTab(LyraDestination.Favorites)
                    },
                )
            }
            composable(LyraDestination.Favorites.route) {
                LikedSongsRoute(onBack = {})
            }
            composable(LyraDestination.Profile.route) { ProfileRoute() }
        }
    }
}

/**
 * Alt çubuk sekmesine standart desenle geçiş yapar: back stack'te sekme kopyası birikmez
 * (`launchSingleTop`), sekmeler arası geçişte durum saklanır/geri yüklenir
 * (`saveState`/`restoreState`) ve geri tuşu daima Home'a döner (`popUpTo(Home)`).
 */
private fun NavHostController.navigateToTab(destination: LyraDestination) {
    navigate(destination.route) {
        popUpTo(LyraDestination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Auth akışını back stack'ten temizleyerek Home'a geçer (geri tuşu Login'e dönmez). */
private fun NavHostController.navigateToHomeClearingAuth() {
    navigate(LyraDestination.Home.route) {
        popUpTo(LyraDestination.Login.route) { inclusive = true }
        launchSingleTop = true
    }
}

