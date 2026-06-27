package com.turkcell.lyraapp.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.playback.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PlayerEffect>(Channel.BUFFERED)
    val effect: Flow<PlayerEffect> = _effect.receiveAsFlow()

    init {
        playbackRepository.playbackState
            .onEach { state ->
                _uiState.update {
                    it.copy(
                        currentSong = state.currentSong,
                        isPlaying = state.isPlaying,
                        isLiked = state.isLiked,
                        progress = state.progress,
                        isPlayingAd = state.isPlayingAd,
                        adTitle = state.adTitle,
                        adAdvertiser = state.adAdvertiser,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.PlayPauseClicked -> togglePlayPause()
            is PlayerIntent.NextClicked -> viewModelScope.launch { playbackRepository.next() }
            is PlayerIntent.LikeClicked -> viewModelScope.launch { playbackRepository.toggleLike() }
            is PlayerIntent.ExpandClicked -> viewModelScope.launch { _effect.send(PlayerEffect.OpenNowPlaying) }
        }
    }

    private fun togglePlayPause() {
        viewModelScope.launch {
            if (_uiState.value.isPlaying) playbackRepository.pause()
            else playbackRepository.resume()
        }
    }
}
