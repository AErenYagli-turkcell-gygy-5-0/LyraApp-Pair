package com.turkcell.lyraapp.ui.auth.login

data class LoginUiState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isContinueEnabled: Boolean = false,
)

sealed interface LoginIntent {
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data object Submit : LoginIntent
}

sealed interface LoginEffect {
    data class NavigateToOtp(val phoneNumber: String, val firstTime: Boolean) : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}
