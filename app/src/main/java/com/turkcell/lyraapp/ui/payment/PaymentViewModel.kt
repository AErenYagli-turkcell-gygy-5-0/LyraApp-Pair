package com.turkcell.lyraapp.ui.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.UserSessionManager
import com.turkcell.lyraapp.data.membership.CheckoutCard
import com.turkcell.lyraapp.data.membership.MembershipRepository
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
class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val membershipRepository: MembershipRepository,
    private val userSessionManager: UserSessionManager,
) : ViewModel() {

    private val planType: String = savedStateHandle["planType"] ?: "recurring"

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PaymentEffect>(Channel.BUFFERED)
    val effect: Flow<PaymentEffect> = _effect.receiveAsFlow()

    init {
        loadPlan()
    }

    fun onIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.CardNumberChanged -> updateForm(cardNumber = intent.value)
            is PaymentIntent.HolderNameChanged -> updateForm(holderName = intent.value)
            is PaymentIntent.ExpiryChanged -> updateForm(expiry = intent.value)
            is PaymentIntent.CvcChanged -> updateForm(cvc = intent.value)
            is PaymentIntent.PayClicked -> processPayment()
        }
    }

    private fun updateForm(
        cardNumber: String = _uiState.value.cardNumber,
        holderName: String = _uiState.value.holderName,
        expiry: String = _uiState.value.expiry,
        cvc: String = _uiState.value.cvc,
    ) {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        val formattedCard = digitsOnly.take(16).chunked(4).joinToString(" ")
        val formattedExpiry = formatExpiry(expiry)
        val cvcClean = cvc.filter { it.isDigit() }.take(4)

        val isValid = digitsOnly.length == 16 &&
            holderName.isNotBlank() &&
            formattedExpiry.length == 5 &&
            cvcClean.length in 3..4

        _uiState.update {
            it.copy(
                cardNumber = formattedCard,
                holderName = holderName,
                expiry = formattedExpiry,
                cvc = cvcClean,
                isFormValid = isValid,
                errorMessage = null,
            )
        }
    }

    private fun formatExpiry(raw: String): String {
        val digits = raw.filter { it.isDigit() }.take(4)
        return if (digits.length > 2) {
            "${digits.substring(0, 2)}/${digits.substring(2)}"
        } else {
            digits
        }
    }

    private fun processPayment() {
        if (_uiState.value.isProcessing) return
        val state = _uiState.value
        val plan = state.plan ?: return

        val cardDigits = state.cardNumber.filter { it.isDigit() }
        val expiryParts = state.expiry.split("/")
        if (expiryParts.size != 2) return

        val expMonth = expiryParts[0].toIntOrNull() ?: return
        val expYear = (expiryParts[1].toIntOrNull() ?: return).let { y ->
            if (y < 100) 2000 + y else y
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            membershipRepository.checkout(
                planType = plan.type,
                card = CheckoutCard(
                    number = cardDigits,
                    expMonth = expMonth,
                    expYear = expYear,
                    cvc = state.cvc,
                    holderName = state.holderName.ifBlank { null },
                ),
            )
                .onSuccess { result ->
                    userSessionManager.updateMembership(result.membership)
                    _uiState.update { it.copy(isProcessing = false) }
                    _effect.send(PaymentEffect.PaymentSuccess(plan.durationDays))
                }
                .onFailure { error ->
                    val msg = error.message ?: "Ödeme işlemi başarısız."
                    _uiState.update { it.copy(isProcessing = false, errorMessage = msg) }
                    _effect.send(PaymentEffect.ShowError(msg))
                }
        }
    }

    private fun loadPlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPlan = true) }
            membershipRepository.getPlans()
                .onSuccess { plans ->
                    val matched = plans.find { it.type == planType }
                    _uiState.update { it.copy(isLoadingPlan = false, plan = matched) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingPlan = false) }
                }
        }
    }
}
