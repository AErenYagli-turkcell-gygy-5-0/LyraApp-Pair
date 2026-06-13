package com.turkcell.lyraapp.data.library

/**
 * Kütüphane ekranının besleme modeli.
 */
data class LibraryFeed(
    val playlists: List<LibraryPlaylist>,
)

/**
 * Kütüphane listesindeki bir çalma listesi öğesi.
 *
 * [isLikedSongs] true olduğunda UI kalp ikonu ile pembe gradient kapak çizer;
 * [isPinned] true olduğunda sağ tarafta pin ikonu gösterilir, aksi halde 3 nokta menü.
 */
data class LibraryPlaylist(
    val id: String,
    val title: String,
    val songCount: Int,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
    val isPinned: Boolean = false,
    val isLikedSongs: Boolean = false,
)
