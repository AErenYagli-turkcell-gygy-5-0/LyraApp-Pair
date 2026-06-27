package com.turkcell.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.preferences.ThemePreferenceRepository
import com.turkcell.lyraapp.data.profile.ProfileRepository
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
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val themePreferenceRepository: ThemePreferenceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileEffect> = _effect.receiveAsFlow()

    init {
        loadProfile()
        viewModelScope.launch {
            themePreferenceRepository.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.ThemeChanged -> applyTheme(intent.isDark)
            is ProfileIntent.SettingClicked -> Unit
            is ProfileIntent.PremiumCardClicked -> {
                viewModelScope.launch {
                    _effect.send(ProfileEffect.NavigateToPremium)
                }
            }
        }
    }

    private fun applyTheme(isDark: Boolean) {
        viewModelScope.launch {
            themePreferenceRepository.setTheme(isDark)
        }
    }

    private fun loadProfile() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = profileRepository.getProfileData()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            initials = data.initials,
                            fullName = data.fullName,
                            username = data.username,
                            isPremium = data.isPremium,
                            premiumDaysLeft = data.premiumDaysLeft,
                            membershipType = data.membershipType,
                            playlistCount = data.playlistCount,
                            followerCount = data.followerCount,
                            followingCount = data.followingCount,
                            settings = data.settings,
                        )
                    }
                }
                .onFailure { error ->
                    _effect.send(ProfileEffect.ShowError(error.message ?: "Profil yüklenemedi."))
                }
        }
    }
}
