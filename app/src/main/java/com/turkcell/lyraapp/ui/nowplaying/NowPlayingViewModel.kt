package com.turkcell.lyraapp.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.download.DownloadRepository
import com.turkcell.lyraapp.data.download.DownloadStatus
import com.turkcell.lyraapp.data.playback.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val downloadRepository: DownloadRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<NowPlayingEffect>(Channel.BUFFERED)
    val effect: Flow<NowPlayingEffect> = _effect.receiveAsFlow()

    private var downloadStatusJob: Job? = null
    private var isDownloadedJob: Job? = null

    init {
        playbackRepository.playbackState
            .onEach { state ->
                val songChanged = _uiState.value.currentSong?.id != state.currentSong?.id
                _uiState.update {
                    it.copy(
                        currentSong = state.currentSong,
                        isPlaying = state.isPlaying,
                        isLiked = state.isLiked,
                        isShuffle = state.isShuffle,
                        isRepeat = state.isRepeat,
                        progress = state.progress,
                        currentPositionLabel = state.currentPositionLabel,
                        isPlayingAd = state.isPlayingAd,
                        adTitle = state.adTitle,
                        adAdvertiser = state.adAdvertiser,
                    )
                }
                if (songChanged) {
                    observeDownloadStatus(state.currentSong?.id)
                }
            }
            .launchIn(viewModelScope)

        downloadRepository.activeDownloadStatus
            .onEach { statusMap ->
                val songId = _uiState.value.currentSong?.id ?: return@onEach
                val status = statusMap[songId]
                if (status != null) {
                    _uiState.update { it.copy(downloadStatus = status) }
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
            is NowPlayingIntent.DownloadClicked -> startDownload()
            is NowPlayingIntent.RemoveDownloadClicked -> removeDownload()
        }
    }

    private fun togglePlayPause() {
        viewModelScope.launch {
            if (_uiState.value.isPlaying) playbackRepository.pause()
            else playbackRepository.resume()
        }
    }

    private fun startDownload() {
        val song = _uiState.value.currentSong ?: return
        viewModelScope.launch {
            downloadRepository.downloadSong(song)
            val status = downloadRepository.activeDownloadStatus.value[song.id]
            if (status is DownloadStatus.Downloaded) {
                _effect.send(NowPlayingEffect.ShowSnackbar("Indirme tamamlandi"))
            } else if (status is DownloadStatus.Error) {
                _effect.send(NowPlayingEffect.ShowSnackbar("Indirme basarisiz: ${status.message}"))
            }
        }
    }

    private fun removeDownload() {
        val songId = _uiState.value.currentSong?.id ?: return
        viewModelScope.launch {
            downloadRepository.deleteDownload(songId)
            _uiState.update { it.copy(downloadStatus = DownloadStatus.NotDownloaded) }
            _effect.send(NowPlayingEffect.ShowSnackbar("Indirme kaldirildi"))
        }
    }

    private fun observeDownloadStatus(songId: String?) {
        isDownloadedJob?.cancel()
        if (songId == null) {
            _uiState.update { it.copy(downloadStatus = DownloadStatus.NotDownloaded) }
            return
        }
        val activeStatus = downloadRepository.activeDownloadStatus.value[songId]
        if (activeStatus is DownloadStatus.Downloading) {
            _uiState.update { it.copy(downloadStatus = activeStatus) }
            return
        }
        isDownloadedJob = downloadRepository.isDownloaded(songId)
            .onEach { downloaded ->
                val currentActiveStatus = downloadRepository.activeDownloadStatus.value[songId]
                if (currentActiveStatus is DownloadStatus.Downloading) return@onEach
                _uiState.update {
                    it.copy(
                        downloadStatus = if (downloaded) DownloadStatus.Downloaded
                        else DownloadStatus.NotDownloaded,
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
