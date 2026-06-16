package com.turkcell.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.library.LibraryRepository
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
 * Kütüphane ekranının ViewModel'i (bkz. mvi-viewmodel-rules.md).
 *
 * Çalma listeleri ekran açılışında bir kez yüklenir. Sanatçılar ve Albümler sekmeleri
 * bu iterasyonda içerik göstermez; backend hazır olduğunda eklenir.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect: Flow<LibraryEffect> = _effect.receiveAsFlow()

    init {
        loadFeed()
    }

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.TabSelected ->
                _uiState.update { it.copy(selectedTab = intent.tab) }
            is LibraryIntent.OpenLikedSongs ->
                viewModelScope.launch { _effect.send(LibraryEffect.NavigateToLikedSongs) }
            is LibraryIntent.PlaylistClicked ->
                viewModelScope.launch { _effect.send(LibraryEffect.NavigateToPlaylistDetail(intent.playlistId)) }
            is LibraryIntent.CreatePlaylistClicked ->
                viewModelScope.launch { _effect.send(LibraryEffect.NavigateToCreatePlaylist) }
        }
    }

    private fun loadFeed() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = libraryRepository.getLibraryFeed()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { feed ->
                    _uiState.update { it.copy(playlists = feed.playlists) }
                }
                .onFailure { error ->
                    _effect.send(LibraryEffect.ShowError(error.message ?: "Kütüphane yüklenemedi."))
                }
        }
    }
}
