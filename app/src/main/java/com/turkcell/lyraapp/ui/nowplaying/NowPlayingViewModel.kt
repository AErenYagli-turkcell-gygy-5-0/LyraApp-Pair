package com.turkcell.lyraapp.ui.nowplaying

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
class NowPlayingViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<NowPlayingEffect>(Channel.BUFFERED)
    val effect: Flow<NowPlayingEffect> = _effect.receiveAsFlow()

    init {
        playbackRepository.playbackState
            .onEach { state ->
                _uiState.update {
                    it.copy(
                        currentSong = state.currentSong,
                        isPlaying = state.isPlaying,
                        isLiked = state.isLiked,
                        isShuffle = state.isShuffle,
                        isRepeat = state.isRepeat,
                        progress = state.progress,
                        currentPositionLabel = state.currentPositionLabel,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: NowPlayingIntent) {
        when (intent) {
            is NowPlayingIntent.PlayPauseClicked -> togglePlayPause()
            is NowPlayingIntent.NextClicked -> viewModelScope.launch { playbackRepository.next() }
            is NowPlayingIntent.PreviousClicked -> viewModelScope.launch { playbackRepository.previous() }
            is NowPlayingIntent.LikeClicked -> viewModelScope.launch { playbackRepository.toggleLike() }
            is NowPlayingIntent.ShuffleClicked -> viewModelScope.launch { playbackRepository.toggleShuffle() }
            is NowPlayingIntent.RepeatClicked -> viewModelScope.launch { playbackRepository.toggleRepeat() }
            is NowPlayingIntent.SeekTo -> viewModelScope.launch { playbackRepository.seekTo(intent.progress) }
            is NowPlayingIntent.CollapseClicked -> viewModelScope.launch { _effect.send(NowPlayingEffect.Collapse) }
        }
    }

    private fun togglePlayPause() {
        viewModelScope.launch {
            if (_uiState.value.isPlaying) playbackRepository.pause()
            else playbackRepository.resume()
        }
    }
}
