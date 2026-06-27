package com.turkcell.lyraapp.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.download.DownloadRepository
import com.turkcell.lyraapp.data.home.HomeRepository
import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.data.membership.MembershipRepository
import com.turkcell.lyraapp.data.playback.PlaybackRepository
import com.turkcell.lyraapp.data.playback.Song
import com.turkcell.lyraapp.data.preferences.ThemePreferenceRepository
import com.turkcell.lyraapp.data.remote.AuthApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val themePreferenceRepository: ThemePreferenceRepository,
    private val playbackRepository: PlaybackRepository,
    private val downloadRepository: DownloadRepository,
    private val authApiService: AuthApiService,
    private val membershipRepository: MembershipRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(greeting = greetingForNow()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    private val prefs by lazy {
        context.getSharedPreferences("lyra_premium_warning", Context.MODE_PRIVATE)
    }

    init {
        loadFeed()
        viewModelScope.launch {
            themePreferenceRepository.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        viewModelScope.launch {
            downloadRepository.getDownloadedSongs().collect { entities ->
                val songs = entities.map { entity ->
                    HomeSong.fromDownloaded(entity)
                }
                _uiState.update { it.copy(downloadedSongs = songs) }
            }
        }
        checkPremiumExpiry()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ScreenResumed -> loadFeed()
            is HomeIntent.Retry -> loadFeed()
            is HomeIntent.ToggleTheme -> toggleTheme()
            is HomeIntent.SongClicked -> playSong(intent.song)
            is HomeIntent.DismissPremiumWarning -> {
                _uiState.update { it.copy(showPremiumWarning = false) }
                markWarningShownToday()
            }
            is HomeIntent.PremiumWarningAction -> {
                _uiState.update { it.copy(showPremiumWarning = false) }
                markWarningShownToday()
                viewModelScope.launch {
                    _effect.send(HomeEffect.NavigateToPayment(intent.planType))
                }
            }
        }
    }

    private fun playSong(homeSong: HomeSong) {
        viewModelScope.launch {
            playbackRepository.playSong(
                Song(
                    id = homeSong.id,
                    title = homeSong.title,
                    artist = homeSong.artist,
                    duration = formatDuration(homeSong.durationMs),
                    artworkStartColor = homeSong.artworkStartColor,
                    artworkEndColor = homeSong.artworkEndColor,
                ),
            )
            _effect.send(HomeEffect.NavigateToNowPlaying)
        }
    }

    private fun formatDuration(durationMs: Int): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    private fun toggleTheme() {
        viewModelScope.launch {
            themePreferenceRepository.setTheme(!_uiState.value.isDarkTheme)
        }
    }

    private fun loadFeed() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            homeRepository.getHomeFeed()
                .onSuccess { feed ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            forYouSongs = feed.forYouSongs,
                            recentlyPlayedSongs = feed.recentlyPlayedSongs,
                            recommendationSongs = feed.recommendationSongs,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Ana sayfa yuklenemedi.",
                        )
                    }
                }
        }
    }

    private fun checkPremiumExpiry() {
        viewModelScope.launch {
            try {
                val response = authApiService.getMe()
                if (!response.isSuccessful) return@launch
                val user = response.body()?.data ?: return@launch
                val membership = user.membership ?: return@launch
                if (membership.status != "active") return@launch

                val daysLeft = calculateDaysLeft(membership.expiresAt)
                if (daysLeft > 3) return@launch
                if (wasWarningShownToday()) return@launch

                membershipRepository.getPlans()
                    .onSuccess { plans ->
                        _uiState.update {
                            it.copy(
                                showPremiumWarning = true,
                                premiumDaysLeft = daysLeft.coerceAtLeast(0).toInt(),
                                premiumPlans = plans,
                            )
                        }
                    }
            } catch (_: Exception) { }
        }
    }

    private fun calculateDaysLeft(expiresAt: String): Long {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val dateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            val expiryDate = try {
                val cleaned = expiresAt.replace("Z", "").substringBefore(".")
                isoFormat.parse(cleaned)
            } catch (_: Exception) {
                dateOnly.parse(expiresAt)
            }

            if (expiryDate == null) return 0L
            val diffMs = expiryDate.time - System.currentTimeMillis()
            TimeUnit.MILLISECONDS.toDays(diffMs)
        } catch (_: Exception) {
            0L
        }
    }

    private fun todayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
    }

    private fun wasWarningShownToday(): Boolean {
        val lastShown = prefs.getString("last_shown_date", null) ?: return false
        return lastShown == todayString()
    }

    private fun markWarningShownToday() {
        prefs.edit().putString("last_shown_date", todayString()).apply()
    }

    private fun greetingForNow(): String =
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Günaydın"
            in 12..17 -> "İyi günler"
            else -> "İyi akşamlar"
        }
}
