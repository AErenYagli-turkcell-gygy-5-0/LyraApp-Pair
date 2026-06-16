package com.turkcell.lyraapp.ui.navigation

/**
 * Uygulamadaki navigasyon hedeflerinin tek doğruluk kaynağı.
 *
 * Her hedef benzersiz bir [route] string'iyle temsil edilir; [LyraNavHost] bu route'lar
 * üzerinden composable'ları bağlar. Yeni bir ekran eklendiğinde buraya bir hedef eklenir.
 */
enum class LyraDestination(val route: String) {
    Login("login"),
    Register("register"),
    Home("home"),
    Search("search"),
    Library("library"),
    Favorites("favorites"),
    Profile("profile"),
    PlaylistDetail("playlistdetail"),
    NowPlaying("nowplaying"),
    CreatePlaylist("createplaylist"),
}

fun playlistDetailRoute(playlistId: String) = "playlistdetail/$playlistId"
