package com.turkcell.lyraapp.ui.playlistdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.playback.PlaybackRepository
import com.turkcell.lyraapp.data.playback.Song
import com.turkcell.lyraapp.data.playlistdetail.PlaylistDetailRepository
import com.turkcell.lyraapp.data.playlistdetail.PlaylistSong
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
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistDetailRepository: PlaylistDetailRepository,
    private val playbackRepository: PlaybackRepository,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PlaylistDetailEffect>(Channel.BUFFERED)
    val effect: Flow<PlaylistDetailEffect> = _effect.receiveAsFlow()

    init {
        loadPlaylist()
        observePlayback()
    }

    fun onIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.BackClicked ->
                viewModelScope.launch { _effect.send(PlaylistDetailEffect.NavigateBack) }
            is PlaylistDetailIntent.SongClicked -> playSong(intent.songId)
            is PlaylistDetailIntent.PlayAllClicked -> playFirstSong()
            is PlaylistDetailIntent.ShuffleClicked -> playFirstSong()
            is PlaylistDetailIntent.LikePlaylistClicked -> Unit
            is PlaylistDetailIntent.DownloadClicked -> Unit
        }
    }

    private fun loadPlaylist() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = playlistDetailRepository.getPlaylistDetail(playlistId)
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { detail ->
                    _uiState.update { it.copy(playlist = detail) }
                }
                .onFailure { error ->
                    _effect.send(PlaylistDetailEffect.ShowError(error.message ?: "Çalma listesi yüklenemedi."))
                }
        }
    }

    private fun observePlayback() {
        playbackRepository.playbackState
            .onEach { state ->
                _uiState.update { it.copy(currentlyPlayingId = state.currentSong?.id) }
            }
            .launchIn(viewModelScope)
    }

    private fun playSong(songId: String) {
        val song = _uiState.value.playlist?.songs?.firstOrNull { it.id == songId } ?: return
        viewModelScope.launch {
            playbackRepository.playSong(song.toSong())
            _effect.send(PlaylistDetailEffect.NavigateToNowPlaying)
        }
    }

    private fun playFirstSong() {
        val firstSong = _uiState.value.playlist?.songs?.firstOrNull() ?: return
        viewModelScope.launch {
            playbackRepository.playSong(firstSong.toSong())
            _effect.send(PlaylistDetailEffect.NavigateToNowPlaying)
        }
    }

    private fun PlaylistSong.toSong() = Song(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        artworkStartColor = artworkStartColor,
        artworkEndColor = artworkEndColor,
    )
}
