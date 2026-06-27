package com.turkcell.lyraapp.ui.premium

import com.turkcell.lyraapp.data.membership.MembershipPlan

data class PremiumUiState(
    val isLoading: Boolean = false,
    val plans: List<MembershipPlan> = emptyList(),
    val selectedPlanType: String = "recurring",
    val errorMessage: String? = null,
)

sealed interface PremiumIntent {
    data class PlanSelected(val planType: String) : PremiumIntent
    data object ContinueClicked : PremiumIntent
}

sealed interface PremiumEffect {
    data class NavigateToPayment(val planType: String) : PremiumEffect
    data class ShowError(val message: String) : PremiumEffect
}
