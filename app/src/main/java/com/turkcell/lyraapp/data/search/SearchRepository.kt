package com.turkcell.lyraapp.data.search

/**
 * Arama ekranı içeriğinin veri kaynağı soyutlaması.
 *
 * Backend REST API'si hazır olmadığından geçici implementasyon [MockSearchRepository]'dir;
 * gerçek API geldiğinde yalnızca implementasyon ve `di/SearchModule.kt` bağlaması değişir.
 */
interface SearchRepository {

    /** Tür grid'i için beslemeyi döndürür. */
    suspend fun getSearchFeed(): Result<SearchFeed>
}
