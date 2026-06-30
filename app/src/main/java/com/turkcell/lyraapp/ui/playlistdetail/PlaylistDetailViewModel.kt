package com.turkcell.lyraapp.ui.playlistdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.UserSessionManager
import com.turkcell.lyraapp.data.createplaylist.AvailableSong
import com.turkcell.lyraapp.data.home.artworkColorsFor
import com.turkcell.lyraapp.data.playback.PlaybackRepository
import com.turkcell.lyraapp.data.playback.Song
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
import com.turkcell.lyraapp.data.playlist.PlaylistTrack
import com.turkcell.lyraapp.data.playlist.PlaylistWithTracks
import com.turkcell.lyraapp.data.playlist.TrackAlreadyInPlaylistException
import com.turkcell.lyraapp.data.playlistdetail.PlaylistDetail
import com.turkcell.lyraapp.data.playlistdetail.PlaylistSong
import com.turkcell.lyraapp.data.remote.SongApiService
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
    private val playlistRepository: PlaylistRepository,
    private val playbackRepository: PlaybackRepository,
    private val songApiService: SongApiService,
    private val userSessionManager: UserSessionManager,
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
            is PlaylistDetailIntent.RemoveTrackClicked -> removeTrack(intent.songId)
            is PlaylistDetailIntent.AddSongClicked -> openSongPicker()
            is PlaylistDetailIntent.SongPickerSongSelected -> addTrack(intent.songId)
            is PlaylistDetailIntent.SongPickerDismissed ->
                _uiState.update { it.copy(isSongPickerVisible = false) }
            is PlaylistDetailIntent.DeletePlaylistClicked ->
                _uiState.update { it.copy(isDeleteConfirmVisible = true) }
            is PlaylistDetailIntent.DeleteDismissed ->
                _uiState.update { it.copy(isDeleteConfirmVisible = false) }
            is PlaylistDetailIntent.DeleteConfirmed -> deletePlaylist()
        }
    }

    private fun deletePlaylist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            playlistRepository.deletePlaylist(playlistId)
                .onSuccess {
                    _effect.send(PlaylistDetailEffect.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isDeleting = false, isDeleteConfirmVisible = false) }
                    _effect.send(PlaylistDetailEffect.ShowError(error.message ?: "Çalma listesi silinemedi."))
                }
        }
    }

    private fun loadPlaylist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            playlistRepository.getPlaylistWithTracks(playlistId)
                .onSuccess { detail ->
                    _uiState.update { it.copy(isLoading = false, playlist = detail.toUiModel()) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
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

    private fun removeTrack(songId: String) {
        viewModelScope.launch {
            playlistRepository.removeTrack(playlistId, songId)
                .onSuccess { loadPlaylist() }
                .onFailure { error ->
                    _effect.send(PlaylistDetailEffect.ShowError(error.message ?: "Şarkı çıkarılamadı."))
                }
        }
    }

    private fun openSongPicker() {
        _uiState.update { it.copy(isSongPickerVisible = true, isSongPickerLoading = true) }
        viewModelScope.launch {
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
                    _uiState.update { it.copy(isSongPickerLoading = false, availableSongs = songs) }
                }
                .onFailure {
                    _uiState.update { it.copy(isSongPickerLoading = false) }
                    _effect.send(PlaylistDetailEffect.ShowError("Şarkı kataloğu yüklenemedi."))
                }
        }
    }

    private fun addTrack(songId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSongPickerVisible = false) }
            playlistRepository.addTrack(playlistId, songId)
                .onSuccess { loadPlaylist() }
                .onFailure { error ->
                    val message = if (error is TrackAlreadyInPlaylistException) {
                        "Bu şarkı zaten çalma listesinde."
                    } else {
                        error.message ?: "Şarkı eklenemedi."
                    }
                    _effect.send(PlaylistDetailEffect.ShowError(message))
                }
        }
    }

    private fun PlaylistWithTracks.toUiModel(): PlaylistDetail {
        val (startColor, endColor) = artworkColorsFor(id)
        val totalMs = tracks.sumOf { it.durationMs }
        return PlaylistDetail(
            id = id,
            title = name,
            description = description,
            isOwner = ownerId != null && ownerId == userSessionManager.user.value?.id,
            songCount = tracks.size,
            totalDuration = formatTotalDuration(totalMs),
            artworkStartColor = startColor,
            artworkEndColor = endColor,
            songs = tracks.map { it.toUiModel() },
        )
    }

    private fun PlaylistTrack.toUiModel() = PlaylistSong(
        id = id,
        title = title,
        artist = artist,
        duration = formatDuration(durationMs),
        artworkStartColor = artworkStartColor,
        artworkEndColor = artworkEndColor,
    )

    private fun PlaylistSong.toSong() = Song(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        artworkStartColor = artworkStartColor,
        artworkEndColor = artworkEndColor,
    )

    private fun formatDuration(durationMs: Int): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    private fun formatTotalDuration(totalMs: Int): String {
        val totalMinutes = totalMs / 1000 / 60
        return if (totalMinutes < 60) {
            "$totalMinutes dk"
        } else {
            "${totalMinutes / 60} sa ${totalMinutes % 60} dk"
        }
    }

    private companion object {
        const val CATALOG_PAGE_SIZE = 50
    }
}
