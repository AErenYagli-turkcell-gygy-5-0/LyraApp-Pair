package com.turkcell.lyraapp.ui.auth.completeprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CompleteProfileEffect>(Channel.BUFFERED)
    val effect: Flow<CompleteProfileEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: CompleteProfileIntent) {
        when (intent) {
            is CompleteProfileIntent.FirstNameChanged -> updateForm { it.copy(firstName = intent.value) }
            is CompleteProfileIntent.LastNameChanged -> updateForm { it.copy(lastName = intent.value) }
            is CompleteProfileIntent.BirthDayChanged -> updateForm { it.copy(birthDay = intent.value.filter { c -> c.isDigit() }.take(2)) }
            is CompleteProfileIntent.BirthMonthChanged -> updateForm { it.copy(birthMonth = intent.value.filter { c -> c.isDigit() }.take(2)) }
            is CompleteProfileIntent.BirthYearChanged -> updateForm { it.copy(birthYear = intent.value.filter { c -> c.isDigit() }.take(4)) }
            is CompleteProfileIntent.Submit -> submit()
            is CompleteProfileIntent.BackClicked -> viewModelScope.launch { _effect.send(CompleteProfileEffect.NavigateBack) }
        }
    }

    private fun updateForm(transform: (CompleteProfileUiState) -> CompleteProfileUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isSubmitEnabled = updated.isFormValid())
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled || state.isLoading) return

        val birthDate = "${state.birthYear}-${state.birthMonth.padStart(2, '0')}-${state.birthDay.padStart(2, '0')}"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.completeProfile(state.firstName, state.lastName, birthDate)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { _effect.send(CompleteProfileEffect.NavigateToHome) }
                .onFailure { error ->
                    _effect.send(CompleteProfileEffect.ShowError(error.message ?: "Profil kaydedilemedi."))
                }
        }
    }
}

private fun CompleteProfileUiState.isFormValid(): Boolean =
    firstName.isNotBlank() &&
        lastName.isNotBlank() &&
        birthDay.isNotBlank() &&
        birthMonth.isNotBlank() &&
        birthYear.length == 4
