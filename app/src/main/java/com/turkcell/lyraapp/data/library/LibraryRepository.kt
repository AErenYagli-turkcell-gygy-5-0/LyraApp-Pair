package com.turkcell.lyraapp.data.library

/**
 * Kütüphane ekranı içeriğinin veri kaynağı soyutlaması.
 *
 * Backend REST API'si hazır olmadığından geçici implementasyon [MockLibraryRepository]'dir;
 * gerçek API geldiğinde yalnızca implementasyon ve `di/LibraryModule.kt` bağlaması değişir.
 */
interface LibraryRepository {

    /** Kullanıcının çalma listelerini döndürür. */
    suspend fun getLibraryFeed(): Result<LibraryFeed>
}
