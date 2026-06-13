package com.turkcell.lyraapp.data.likedsongs

/**
 * Beğenilen Şarkılar ekranının besleme modeli.
 */
data class LikedSongsFeed(
    val songCount: Int,
    val totalDuration: String,
    val songs: List<LikedSong>,
)

/**
 * Beğenilen Şarkılar listesindeki tek bir şarkı öğesi.
 *
 * [isPlaying] true olduğunda satır vurgulanır ve başlık rengi farklılaşır.
 * [isLiked] kalp ikonunun dolu/boş gösterimini belirler.
 */
data class LikedSong(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
    val isLiked: Boolean = true,
    val isPlaying: Boolean = false,
)
