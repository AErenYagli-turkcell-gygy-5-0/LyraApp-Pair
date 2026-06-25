package com.turkcell.lyraapp.data.download

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadedSongEntity)

    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    suspend fun deleteBySongId(songId: String)

    @Query("SELECT * FROM downloaded_songs WHERE songId = :songId LIMIT 1")
    suspend fun getBySongId(songId: String): DownloadedSongEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE songId = :songId)")
    fun existsBySongId(songId: String): Flow<Boolean>

    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getAll(): Flow<List<DownloadedSongEntity>>
}
