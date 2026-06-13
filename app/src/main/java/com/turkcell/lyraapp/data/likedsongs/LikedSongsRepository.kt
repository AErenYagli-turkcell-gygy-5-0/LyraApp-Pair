package com.turkcell.lyraapp.data.likedsongs

/**
 * Beğenilen Şarkılar ekranının veri kaynağı soyutlaması.
 *
 * Backend REST API'si hazır olmadığından geçici implementasyon [MockLikedSongsRepository]'dir;
 * gerçek API geldiğinde yalnızca implementasyon ve `di/LikedSongsModule.kt` bağlaması değişir.
 */
interface LikedSongsRepository {

    /** Beğenilen şarkıların tamamını ve özet bilgileri döndürür. */
    suspend fun getLikedSongs(): Result<LikedSongsFeed>
}
