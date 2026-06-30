package com.turkcell.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.home.artworkColorsFor
import com.turkcell.lyraapp.data.library.LibraryPlaylist
import com.turkcell.lyraapp.data.playlist.Playlist
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
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

/**
 * Kütüphane ekranının ViewModel'i (bkz. mvi-viewmodel-rules.md).
 *
 * Çalma listeleri [PlaylistRepository.playlists] reaktif Flow'undan beslenir; oluşturma/silme
 * sonrası Kütüphane otomatik güncellenir, manuel refetch gerekmez. Sanatçılar ve Albümler
 * sekmeleri bu iterasyonda içerik göstermez; backend hazır olduğunda eklenir.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect: Flow<LibraryEffect> = _effect.receiveAsFlow()

    init {
        observePlaylists()
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
            is LibraryIntent.Retry -> loadFeed()
            is LibraryIntent.DeletePlaylistClicked ->
                _uiState.update { it.copy(playlistPendingDelete = intent.playlist) }
            is LibraryIntent.DeleteDismissed ->
                _uiState.update { it.copy(playlistPendingDelete = null) }
            is LibraryIntent.DeleteConfirmed -> deletePlaylist()
        }
    }

    private fun observePlaylists() {
        playlistRepository.playlists
            .onEach { playlists ->
                _uiState.update { it.copy(playlists = buildLibraryPlaylists(playlists)) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadFeed() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            playlistRepository.refreshPlaylists()
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Kütüphane yüklenemedi.",
                        )
                    }
                }
        }
    }

    private fun deletePlaylist() {
        val playlist = _uiState.value.playlistPendingDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            playlistRepository.deletePlaylist(playlist.id)
                .onSuccess {
                    _uiState.update { it.copy(isDeleting = false, playlistPendingDelete = null) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isDeleting = false, playlistPendingDelete = null) }
                    _effect.send(LibraryEffect.ShowError(error.message ?: "Çalma listesi silinemedi."))
                }
        }
    }

    private fun buildLibraryPlaylists(playlists: List<Playlist>): List<LibraryPlaylist> {
        val likedSongsRow = LibraryPlaylist(
            id = LIKED_SONGS_ID,
            title = "Beğenilen Şarkılar",
            artworkStartColor = 0xFFFFB1C8,
            artworkEndColor = 0xFFFF6B9D,
            isPinned = true,
            isLikedSongs = true,
        )
        val apiRows = playlists.map { playlist ->
            val (startColor, endColor) = artworkColorsFor(playlist.id)
            LibraryPlaylist(
                id = playlist.id,
                title = playlist.name,
                artworkStartColor = startColor,
                artworkEndColor = endColor,
            )
        }
        return listOf(likedSongsRow) + apiRows
    }

    private companion object {
        const val LIKED_SONGS_ID = "liked-songs"
    }
}
