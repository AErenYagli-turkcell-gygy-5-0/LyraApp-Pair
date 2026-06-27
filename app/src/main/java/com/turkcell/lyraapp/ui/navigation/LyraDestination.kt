package com.turkcell.lyraapp.ui.navigation

enum class LyraDestination(val route: String) {
    Login("login"),
    Otp("otp"),
    CompleteProfile("completeprofile"),
    Home("home"),
    Search("search"),
    Library("library"),
    Favorites("favorites"),
    Profile("profile"),
    PlaylistDetail("playlistdetail"),
    NowPlaying("nowplaying"),
    CreatePlaylist("createplaylist"),
    Premium("premium"),
    Payment("payment"),
    PremiumSuccess("premiumsuccess"),
}

fun playlistDetailRoute(playlistId: String) = "playlistdetail/$playlistId"

fun otpRoute(phoneNumber: String, firstTime: Boolean) =
    "otp?phoneNumber=$phoneNumber&firstTime=$firstTime"

fun completeProfileRoute(phoneNumber: String) =
    "completeprofile?phoneNumber=$phoneNumber"

fun paymentRoute(planType: String) = "payment?planType=$planType"

fun premiumSuccessRoute(durationDays: Int) = "premiumsuccess?durationDays=$durationDays"
