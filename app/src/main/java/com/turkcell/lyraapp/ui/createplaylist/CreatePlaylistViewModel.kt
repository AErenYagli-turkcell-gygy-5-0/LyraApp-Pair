package com.turkcell.lyraapp.ui.createplaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.createplaylist.CreatePlaylistInput
import com.turkcell.lyraapp.data.createplaylist.CreatePlaylistRepository
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
    private val createPlaylistRepository: CreatePlaylistRepository,
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
            createPlaylistRepository.getAvailableSongs()
                .onSuccess { songs ->
                    _uiState.update { it.copy(isLoading = false, availableSongs = songs) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
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
            val input = CreatePlaylistInput(
                name = state.name,
                description = state.description,
                isPublic = state.isPublic,
                selectedSongIds = state.selectedSongIds.toList(),
            )
            createPlaylistRepository.createPlaylist(input)
                .onSuccess { _effect.send(CreatePlaylistEffect.Dismiss) }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _effect.send(CreatePlaylistEffect.ShowError(error.message ?: "Çalma listesi kaydedilemedi."))
                }
        }
    }

    private fun updateForm(transform: (CreatePlaylistUiState) -> CreatePlaylistUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isSaveEnabled = updated.name.isNotBlank() || updated.selectedSongIds.isNotEmpty())
        }
    }
}
