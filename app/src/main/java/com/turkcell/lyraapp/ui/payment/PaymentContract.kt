package com.turkcell.lyraapp.ui.payment

import com.turkcell.lyraapp.data.membership.MembershipPlan

data class PaymentUiState(
    val isLoadingPlan: Boolean = false,
    val isProcessing: Boolean = false,
    val plan: MembershipPlan? = null,
    val cardNumber: String = "",
    val holderName: String = "",
    val expiry: String = "",
    val cvc: String = "",
    val isFormValid: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PaymentIntent {
    data class CardNumberChanged(val value: String) : PaymentIntent
    data class HolderNameChanged(val value: String) : PaymentIntent
    data class ExpiryChanged(val value: String) : PaymentIntent
    data class CvcChanged(val value: String) : PaymentIntent
    data object PayClicked : PaymentIntent
}

sealed interface PaymentEffect {
    data class PaymentSuccess(val durationDays: Int) : PaymentEffect
    data class ShowError(val message: String) : PaymentEffect
}
