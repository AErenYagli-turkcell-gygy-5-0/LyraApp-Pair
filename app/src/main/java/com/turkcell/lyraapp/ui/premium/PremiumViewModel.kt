package com.turkcell.lyraapp.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class PremiumViewModel @Inject constructor(
    private val membershipRepository: MembershipRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PremiumEffect>(Channel.BUFFERED)
    val effect: Flow<PremiumEffect> = _effect.receiveAsFlow()

    init {
        loadPlans()
    }

    fun onIntent(intent: PremiumIntent) {
        when (intent) {
            is PremiumIntent.PlanSelected -> {
                _uiState.update { it.copy(selectedPlanType = intent.planType) }
            }
            is PremiumIntent.ContinueClicked -> {
                viewModelScope.launch {
                    _effect.send(PremiumEffect.NavigateToPayment(_uiState.value.selectedPlanType))
                }
            }
        }
    }

    private fun loadPlans() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            membershipRepository.getPlans()
                .onSuccess { plans ->
                    _uiState.update { it.copy(isLoading = false, plans = plans) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Planlar yüklenemedi.",
                        )
                    }
                }
        }
    }
}
