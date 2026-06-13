package com.turkcell.lyraapp.ui.likedsongs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.likedsongs.LikedSongsRepository
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

/**
 * Beğenilen Şarkılar ekranının ViewModel'i (bkz. mvi-viewmodel-rules.md).
 *
 * Şarkılar ekran açılışında bir kez yüklenir. Şarkıya tıklama mevcut çalan öğeyi günceller;
 * geri tuşu [LikedSongsEffect.NavigateBack] üretir.
 */
@HiltViewModel
class LikedSongsViewModel @Inject constructor(
    private val likedSongsRepository: LikedSongsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LikedSongsUiState())
    val uiState: StateFlow<LikedSongsUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LikedSongsEffect>(Channel.BUFFERED)
    val effect: Flow<LikedSongsEffect> = _effect.receiveAsFlow()

    init {
        loadSongs()
    }

    fun onIntent(intent: LikedSongsIntent) {
        when (intent) {
            is LikedSongsIntent.BackClicked ->
                viewModelScope.launch { _effect.send(LikedSongsEffect.NavigateBack) }
            is LikedSongsIntent.SongClicked ->
                _uiState.update { it.copy(currentlyPlayingId = intent.songId) }
        }
    }

    private fun loadSongs() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = likedSongsRepository.getLikedSongs()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { feed ->
                    val playingId = feed.songs.firstOrNull { it.isPlaying }?.id
                    _uiState.update {
                        it.copy(
                            songCount = feed.songCount,
                            totalDuration = feed.totalDuration,
                            songs = feed.songs,
                            currentlyPlayingId = playingId,
                        )
                    }
                }
                .onFailure { error ->
                    _effect.send(
                        LikedSongsEffect.NavigateBack.also {
                            // Yükleme başarısız olursa geri dönülür; hata kaydedilebilir.
                        }
                    )
                }
        }
    }
}
