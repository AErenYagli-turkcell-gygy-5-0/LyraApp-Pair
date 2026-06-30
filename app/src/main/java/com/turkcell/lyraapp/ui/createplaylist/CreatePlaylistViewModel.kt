package com.turkcell.lyraapp.ui.createplaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.createplaylist.AvailableSong
import com.turkcell.lyraapp.data.home.artworkColorsFor
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
import com.turkcell.lyraapp.data.playlist.TrackAlreadyInPlaylistException
import com.turkcell.lyraapp.data.remote.SongApiService
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
class CreatePlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val songApiService: SongApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CreatePlaylistEffect>(Channel.BUFFERED)
    val effect: Flow<CreatePlaylistEffect> = _effect.receiveAsFlow()

    init {
        loadSongs()
    }

    fun onIntent(intent: CreatePlaylistIntent) {
        when (intent) {
            is CreatePlaylistIntent.NameChanged -> updateForm { it.copy(name = intent.name) }
            is CreatePlaylistIntent.DescriptionChanged -> updateForm { it.copy(description = intent.description) }
            is CreatePlaylistIntent.PublicToggled -> updateForm { it.copy(isPublic = !it.isPublic) }
            is CreatePlaylistIntent.SongToggled -> toggleSong(intent.songId)
            is CreatePlaylistIntent.SaveClicked -> savePlaylist()
            is CreatePlaylistIntent.CloseClicked ->
                viewModelScope.launch { _effect.send(CreatePlaylistEffect.Dismiss) }
        }
    }

    private fun loadSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { songApiService.getSongs(limit = CATALOG_PAGE_SIZE) }
                .onSuccess { response ->
                    val songs = response.data.map { dto ->
                        val (startColor, endColor) = artworkColorsFor(dto.id)
                        AvailableSong(
                            id = dto.id,
                            title = dto.title,
                            artist = dto.artist,
                            artworkStartColor = startColor,
                            artworkEndColor = endColor,
                        )
                    }
                    _uiState.update { it.copy(isLoading = false, availableSongs = songs) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(CreatePlaylistEffect.ShowError("Şarkı kataloğu yüklenemedi."))
                }
        }
    }

    private fun toggleSong(songId: String) {
        updateForm { current ->
            val newIds = if (songId in current.selectedSongIds) {
                current.selectedSongIds - songId
            } else {
                current.selectedSongIds + songId
            }
            current.copy(selectedSongIds = newIds)
        }
    }

    private fun savePlaylist() {
        if (!_uiState.value.isSaveEnabled || _uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val description = state.description.trim().ifBlank { null }

            playlistRepository.createPlaylist(name = state.name.trim(), description = description)
                .onSuccess { playlist ->
                    val failedCount = addSelectedTracks(playlist.id, state.selectedSongIds)
                    _uiState.update { it.copy(isSaving = false) }
                    if (failedCount > 0) {
                        _effect.send(
                            CreatePlaylistEffect.ShowError(
                                "Çalma listesi oluşturuldu ama $failedCount şarkı eklenemedi.",
                            ),
                        )
                    }
                    _effect.send(CreatePlaylistEffect.Dismiss)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _effect.send(CreatePlaylistEffect.ShowError(error.message ?: "Çalma listesi kaydedilemedi."))
                }
        }
    }

    /** Seçili şarkıları sırayla ekler; 409 (zaten ekli) sessizce atlanır, diğer hatalar sayılır. */
    private suspend fun addSelectedTracks(playlistId: String, songIds: Set<String>): Int {
        var failedCount = 0
        for (songId in songIds) {
            playlistRepository.addTrack(playlistId, songId).onFailure { error ->
                if (error !is TrackAlreadyInPlaylistException) failedCount++
            }
        }
        return failedCount
    }

    private fun updateForm(transform: (CreatePlaylistUiState) -> CreatePlaylistUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isSaveEnabled = updated.name.isNotBlank())
        }
    }

    private companion object {
        const val CATALOG_PAGE_SIZE = 50
    }
}
