package com.turkcell.lyraapp.data.library

/**
 * Kütüphane listesindeki bir çalma listesi öğesi.
 *
 * [isLikedSongs] true olduğunda UI kalp ikonu ile pembe gradient kapak çizer;
 * [isPinned] true olduğunda sağ tarafta pin ikonu gösterilir, aksi halde 3 nokta menü.
 * Şarkı sayısı API'nin Playlist şemasında yer almadığı için bu modelde tutulmaz.
 */
data class LibraryPlaylist(
    val id: String,
    val title: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
    val isPinned: Boolean = false,
    val isLikedSongs: Boolean = false,
)
