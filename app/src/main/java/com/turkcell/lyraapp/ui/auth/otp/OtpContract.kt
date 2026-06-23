package com.turkcell.lyraapp.ui.auth.otp

data class OtpUiState(
    val phoneNumber: String = "",
    val firstTime: Boolean = false,
    val digits: List<String> = List(DIGIT_COUNT) { "" },
    val focusedIndex: Int = 0,
    val isLoading: Boolean = false,
    val isVerifyEnabled: Boolean = false,
) {
    companion object {
        const val DIGIT_COUNT = 6
    }
}

sealed interface OtpIntent {
    data class DigitChanged(val index: Int, val value: String) : OtpIntent
    data object Submit : OtpIntent
    data object ResendCode : OtpIntent
    data object BackClicked : OtpIntent
}

sealed interface OtpEffect {
    data object NavigateToHome : OtpEffect
    data class NavigateToCompleteProfile(val phoneNumber: String) : OtpEffect
    data object NavigateBack : OtpEffect
    data class ShowError(val message: String) : OtpEffect
    data class MoveFocus(val index: Int) : OtpEffect
}
