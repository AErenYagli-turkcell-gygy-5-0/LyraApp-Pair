package com.turkcell.lyraapp.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turkcell.lyraapp.ui.auth.login.LoginRoute
import com.turkcell.lyraapp.ui.auth.register.RegisterRoute
import com.turkcell.lyraapp.ui.createplaylist.CreatePlaylistRoute
import com.turkcell.lyraapp.ui.home.HomeRoute
import com.turkcell.lyraapp.ui.library.LibraryRoute
import com.turkcell.lyraapp.ui.likedsongs.LikedSongsRoute
import com.turkcell.lyraapp.ui.nowplaying.NowPlayingRoute
import com.turkcell.lyraapp.ui.player.MiniPlayer
import com.turkcell.lyraapp.ui.player.PlayerEffect
import com.turkcell.lyraapp.ui.player.PlayerViewModel
import com.turkcell.lyraapp.ui.playlistdetail.PlaylistDetailRoute
import com.turkcell.lyraapp.ui.profile.ProfileRoute
import com.turkcell.lyraapp.ui.search.SearchRoute

@Composable
fun LyraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        playerViewModel.effect.collect { effect ->
            when (effect) {
                is PlayerEffect.OpenNowPlaying ->
                    navController.navigate(LyraDestination.NowPlaying.route)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isTopLevelRoute(currentRoute)) {
                Column {
                    if (playerUiState.currentSong != null) {
                        MiniPlayer(
                            state = playerUiState,
                            onIntent = playerViewModel::onIntent,
                        )
                    }
                    LyraBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = navController::navigateToTab,
                    )
                }
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

            composable(LyraDestination.Home.route) {
                HomeRoute(
                    onNavigateToProfile = { navController.navigateToTab(LyraDestination.Profile) },
                )
            }

            composable(LyraDestination.Search.route) { SearchRoute() }
            composable(LyraDestination.Library.route) {
                LibraryRoute(
                    onNavigateToLikedSongs = {
                        navController.navigateToTab(LyraDestination.Favorites)
                    },
                    onNavigateToPlaylistDetail = { playlistId ->
                        navController.navigate(playlistDetailRoute(playlistId))
                    },
                    onNavigateToCreatePlaylist = {
                        navController.navigate(LyraDestination.CreatePlaylist.route)
                    },
                )
            }
            composable(LyraDestination.Favorites.route) {
                LikedSongsRoute(
                    onBack = {navController.popBackStack()},
                    onNavigateToNowPlaying = {
                        navController.navigate(LyraDestination.NowPlaying.route)
                    },
                )
            }
            composable(LyraDestination.Profile.route) { ProfileRoute() }

            composable(
                route = "${LyraDestination.PlaylistDetail.route}/{playlistId}",
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
            ) {
                PlaylistDetailRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToNowPlaying = {
                        navController.navigate(LyraDestination.NowPlaying.route)
                    },
                )
            }

            composable(LyraDestination.NowPlaying.route) {
                NowPlayingRoute(
                    onCollapse = { navController.popBackStack() },
                )
            }

            composable(LyraDestination.CreatePlaylist.route) {
                CreatePlaylistRoute(
                    onDismiss = { navController.popBackStack() },
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(destination: LyraDestination) {
    navigate(destination.route) {
        popUpTo(LyraDestination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateToHomeClearingAuth() {
    navigate(LyraDestination.Home.route) {
        popUpTo(LyraDestination.Login.route) { inclusive = true }
        launchSingleTop = true
    }
}
