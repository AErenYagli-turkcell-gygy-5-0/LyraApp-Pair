package com.turkcell.lyraapp.data.download

import android.content.Context
import com.turkcell.lyraapp.data.playback.Song
import com.turkcell.lyraapp.data.remote.SongApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealDownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songApiService: SongApiService,
    private val downloadedSongDao: DownloadedSongDao,
    private val okHttpClient: OkHttpClient,
) : DownloadRepository {

    private val _activeDownloadStatus = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    override val activeDownloadStatus: StateFlow<Map<String, DownloadStatus>> =
        _activeDownloadStatus.asStateFlow()

    override suspend fun downloadSong(song: Song) {
        _activeDownloadStatus.update { it + (song.id to DownloadStatus.Downloading(0f)) }
        try {
            val streamResponse = withContext(Dispatchers.IO) {
                songApiService.getStreamUrl(song.id)
            }
            val downloadUrl = streamResponse.data.url
            val mimeType = streamResponse.data.mimeType

            val extension = when {
                mimeType.contains("wav") -> "wav"
                mimeType.contains("mp3") || mimeType.contains("mpeg") -> "mp3"
                mimeType.contains("ogg") -> "ogg"
                mimeType.contains("flac") -> "flac"
                else -> "audio"
            }

            val downloadsDir = File(context.filesDir, "downloads")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val targetFile = File(downloadsDir, "${song.id}.$extension")
            val tempFile = File(downloadsDir, "${song.id}.$extension.tmp")

            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(downloadUrl).build()
                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Indirme basarisiz: HTTP ${response.code}")
                }

                val body = response.body ?: throw Exception("Bos yanit")
                val contentLength = body.contentLength()
                var bytesDownloaded = 0L

                body.byteStream().use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            bytesDownloaded += bytesRead
                            if (contentLength > 0) {
                                val progress = bytesDownloaded.toFloat() / contentLength
                                _activeDownloadStatus.update {
                                    it + (song.id to DownloadStatus.Downloading(progress.coerceIn(0f, 1f)))
                                }
                            }
                        }
                    }
                }

                tempFile.renameTo(targetFile)
            }

            downloadedSongDao.insert(
                DownloadedSongEntity(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    localPath = targetFile.absolutePath,
                    mimeType = mimeType,
                    fileSize = targetFile.length(),
                    downloadedAt = System.currentTimeMillis(),
                ),
            )

            _activeDownloadStatus.update { it + (song.id to DownloadStatus.Downloaded) }
        } catch (e: Exception) {
            val downloadsDir = File(context.filesDir, "downloads")
            File(downloadsDir, "${song.id}.tmp").delete()
            _activeDownloadStatus.update {
                it + (song.id to DownloadStatus.Error(e.message ?: "Bilinmeyen hata"))
            }
        }
    }

    override suspend fun deleteDownload(songId: String) {
        val entity = downloadedSongDao.getBySongId(songId) ?: return
        withContext(Dispatchers.IO) {
            File(entity.localPath).delete()
        }
        downloadedSongDao.deleteBySongId(songId)
        _activeDownloadStatus.update { it - songId }
    }

    override fun getDownloadedSongs(): Flow<List<DownloadedSongEntity>> =
        downloadedSongDao.getAll().map { entities ->
            val (valid, stale) = withContext(Dispatchers.IO) {
                entities.partition { File(it.localPath).exists() }
            }
            for (entity in stale) {
                downloadedSongDao.deleteBySongId(entity.songId)
            }
            valid
        }

    override fun isDownloaded(songId: String): Flow<Boolean> =
        downloadedSongDao.existsBySongId(songId)

    override suspend fun getLocalPath(songId: String): String? {
        val entity = downloadedSongDao.getBySongId(songId) ?: return null
        val file = File(entity.localPath)
        if (!file.exists()) {
            downloadedSongDao.deleteBySongId(songId)
            return null
        }
        return entity.localPath
    }
}
