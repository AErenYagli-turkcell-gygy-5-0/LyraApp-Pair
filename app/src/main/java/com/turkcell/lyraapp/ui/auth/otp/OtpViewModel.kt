package com.turkcell.lyraapp.ui.auth.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.auth.AuthTokenManager
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
class OtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val authTokenManager: AuthTokenManager,
) : ViewModel() {

    private val phoneNumber: String = savedStateHandle["phoneNumber"] ?: ""
    private val firstTime: Boolean = savedStateHandle["firstTime"] ?: false

    private val _uiState = MutableStateFlow(
        OtpUiState(phoneNumber = phoneNumber, firstTime = firstTime),
    )
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<OtpEffect>(Channel.BUFFERED)
    val effect: Flow<OtpEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: OtpIntent) {
        when (intent) {
            is OtpIntent.DigitChanged -> handleDigitChanged(intent.index, intent.value)
            is OtpIntent.Submit -> submit()
            is OtpIntent.ResendCode -> resendCode()
            is OtpIntent.BackClicked -> viewModelScope.launch { _effect.send(OtpEffect.NavigateBack) }
        }
    }

    private fun handleDigitChanged(index: Int, value: String) {
        val digit = value.filter { it.isDigit() }.take(1)
        _uiState.update { current ->
            val newDigits = current.digits.toMutableList().apply { this[index] = digit }
            current.copy(
                digits = newDigits,
                isVerifyEnabled = newDigits.all { it.isNotEmpty() },
            )
        }
        if (digit.isNotEmpty() && index < OtpUiState.DIGIT_COUNT - 1) {
            viewModelScope.launch { _effect.send(OtpEffect.MoveFocus(index + 1)) }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isVerifyEnabled || state.isLoading) return

        val code = state.digits.joinToString("")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.verifyOtp(state.phoneNumber, code)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { verifyResult ->
                    authTokenManager.saveTokens(verifyResult.accessToken, verifyResult.refreshToken)
                    if (verifyResult.firstTime) {
                        _effect.send(OtpEffect.NavigateToCompleteProfile(state.phoneNumber))
                    } else {
                        _effect.send(OtpEffect.NavigateToHome)
                    }
                }
                .onFailure { error ->
                    _effect.send(OtpEffect.ShowError(error.message ?: "Dogrulama basarisiz."))
                }
        }
    }

    private fun resendCode() {
        viewModelScope.launch {
            authRepository.requestOtp(_uiState.value.phoneNumber)
        }
    }
}
