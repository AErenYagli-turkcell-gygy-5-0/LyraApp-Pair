package com.turkcell.lyraapp.ui.auth.completeprofile

data class CompleteProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthYear: String = "",
    val isLoading: Boolean = false,
    val isSubmitEnabled: Boolean = false,
)

sealed interface CompleteProfileIntent {
    data class FirstNameChanged(val value: String) : CompleteProfileIntent
    data class LastNameChanged(val value: String) : CompleteProfileIntent
    data class BirthDayChanged(val value: String) : CompleteProfileIntent
    data class BirthMonthChanged(val value: String) : CompleteProfileIntent
    data class BirthYearChanged(val value: String) : CompleteProfileIntent
    data object Submit : CompleteProfileIntent
    data object BackClicked : CompleteProfileIntent
}

sealed interface CompleteProfileEffect {
    data object NavigateToHome : CompleteProfileEffect
    data object NavigateBack : CompleteProfileEffect
    data class ShowError(val message: String) : CompleteProfileEffect
}
