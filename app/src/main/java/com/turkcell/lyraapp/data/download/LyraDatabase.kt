package com.turkcell.lyraapp.data.download

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DownloadedSongEntity::class], version = 1)
abstract class LyraDatabase : RoomDatabase() {
    abstract fun downloadedSongDao(): DownloadedSongDao
}
