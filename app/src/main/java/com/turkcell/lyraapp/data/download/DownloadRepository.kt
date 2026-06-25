package com.turkcell.lyraapp.data.download

import com.turkcell.lyraapp.data.playback.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface DownloadStatus {
    data object NotDownloaded : DownloadStatus
    data class Downloading(val progress: Float) : DownloadStatus
    data object Downloaded : DownloadStatus
    data class Error(val message: String) : DownloadStatus
}

interface DownloadRepository {

    val activeDownloadStatus: StateFlow<Map<String, DownloadStatus>>

    suspend fun downloadSong(song: Song)

    suspend fun deleteDownload(songId: String)

    fun isDownloaded(songId: String): Flow<Boolean>

    suspend fun getLocalPath(songId: String): String?
}
