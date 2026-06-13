package com.turkcell.lyraapp.data.search

/**
 * Arama ekranının besleme modeli.
 *
 * Kapak görselleri CDN olmadığından gradyan renk çiftiyle temsil edilir;
 * gerçek API geldiğinde URL alanlarıyla değiştirilir.
 */
data class SearchFeed(
    val genres: List<Genre>,
)

/** "Türlere göz at" grid'indeki her bir müzik türü kartı. */
data class Genre(
    val id: String,
    val name: String,
    val gradientStartColor: Long,
    val gradientEndColor: Long,
    /** Akustik/Indie/Klasik gibi kartlarda dekoratif yarı saydam daireler çizilir. */
    val hasDecorativeCircles: Boolean = false,
)

/** Arama filtre chip'leri için sabit seçenekler. */
enum class SearchFilter(val label: String) {
    All("Hepsi"),
    Pop("Pop"),
    Electronic("Elektronik"),
    Acoustic("Akustik"),
}
