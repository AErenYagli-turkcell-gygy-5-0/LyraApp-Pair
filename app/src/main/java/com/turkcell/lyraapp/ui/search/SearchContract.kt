package com.turkcell.lyraapp.ui.search

import com.turkcell.lyraapp.data.search.Genre
import com.turkcell.lyraapp.data.search.SearchFilter

/**
 * Arama ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 *
 * Arama sorgusu bu iterasyonda yalnızca yerel state'te tutulur; ağ araması
 * backend hazır olduğunda ViewModel'e eklenecektir.
 */
data class SearchUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val selectedFilter: SearchFilter = SearchFilter.All,
    val genres: List<Genre> = emptyList(),
)

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data class FilterSelected(val filter: SearchFilter) : SearchIntent
}

sealed interface SearchEffect {
    data class ShowError(val message: String) : SearchEffect
}
