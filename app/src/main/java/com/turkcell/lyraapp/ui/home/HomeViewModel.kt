package com.turkcell.lyraapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.home.HomeRepository
import com.turkcell.lyraapp.data.playback.PlaybackRepository
import com.turkcell.lyraapp.data.playback.Song
import com.turkcell.lyraapp.data.preferences.ThemePreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Home ekranının ViewModel'i (bkz. mvi-viewmodel-rules.md).
 *
 * Besleme, ekran açılışında bir kez yüklenir; başarısızlıkta [HomeIntent.Retry] ile
 * yeniden denenir. Selamlama metni günün saatinden türetilir (durum sahibi UI değil,
 * ViewModel'dir).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val themePreferenceRepository: ThemePreferenceRepository,
    private val playbackRepository: PlaybackRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(greeting = greetingForNow()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    init {
        loadFeed()
        viewModelScope.launch {
            themePreferenceRepository.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Retry -> loadFeed()
            is HomeIntent.ToggleTheme -> toggleTheme()
            is HomeIntent.QuickPickClicked -> playSong(intent)
        }
    }

    private fun playSong(intent: HomeIntent.QuickPickClicked) {
        viewModelScope.launch {
            playbackRepository.playSong(
                Song(
                    id = intent.id,
                    title = intent.title,
                    artist = intent.artist,
                    duration = formatDuration(intent.durationMs),
                    artworkStartColor = intent.artworkStartColor,
                    artworkEndColor = intent.artworkEndColor,
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
            _uiState.update { it.copy(isLoading = true) }
            val result = homeRepository.getHomeFeed()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { feed ->
                    _uiState.update {
                        it.copy(
                            userInitials = feed.userInitials,
                            quickPicks = feed.quickPicks,
                            recentlyPlayed = feed.recentlyPlayed,
                            playlistsForYou = feed.playlistsForYou,
                        )
                    }
                }
                .onFailure { error ->
                    _effect.send(HomeEffect.ShowError(error.message ?: "Ana sayfa yüklenemedi."))
                }
        }
    }

    // java.time yerine Calendar: minSdk 24'te desugaring gerektirmez.
    private fun greetingForNow(): String =
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Günaydın"
            in 12..17 -> "İyi günler"
            else -> "İyi akşamlar"
        }
}
