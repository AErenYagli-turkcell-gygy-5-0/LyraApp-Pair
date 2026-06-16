package com.turkcell.lyraapp.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.library.LibraryPlaylist
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Kütüphane akışının durumlu (stateful) giriş noktası.
 *
 * "Beğenilen Şarkılar" öğesine tıklanması [LibraryEffect.NavigateToLikedSongs] üretir;
 * bu Effect NavHost tarafından tüketilerek Favoriler sekmesine yönlendirir.
 */
@Composable
fun LibraryRoute(
    onNavigateToLikedSongs: () -> Unit,
    onNavigateToPlaylistDetail: (playlistId: String) -> Unit,
    onNavigateToCreatePlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryEffect.NavigateToLikedSongs -> onNavigateToLikedSongs()
                is LibraryEffect.NavigateToPlaylistDetail -> onNavigateToPlaylistDetail(effect.playlistId)
                is LibraryEffect.NavigateToCreatePlaylist -> onNavigateToCreatePlaylist()
                is LibraryEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LibraryScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Kütüphane ekranı: başlık + filtre sekmeleri + sıralama satırı + çalma listesi.
 *
 * Tamamen durumsuzdur; durumu [state] üzerinden alır, etkileşimleri [onIntent] ile yayımlar.
 */
@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LibraryHeader(onAddClick = { onIntent(LibraryIntent.CreatePlaylistClicked) })
            Spacer(Modifier.height(16.dp))
            LibraryTabRow(
                selected = state.selectedTab,
                onTabSelected = { onIntent(LibraryIntent.TabSelected(it)) },
            )
            Spacer(Modifier.height(16.dp))
            SortRow()
            Spacer(Modifier.height(8.dp))

            if (state.isLoading && state.playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(state.playlists, key = { it.id }) { playlist ->
                        PlaylistRow(
                            playlist = playlist,
                            onClick = {
                                when {
                                    playlist.isLikedSongs -> onIntent(LibraryIntent.OpenLikedSongs)
                                    else -> onIntent(LibraryIntent.PlaylistClicked(playlist.id))
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryHeader(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Kütüphane",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = LyraIcons.Search,
            contentDescription = "Ara",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(20.dp))
        Icon(
            imageVector = LyraIcons.Add,
            contentDescription = "Ekle",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onAddClick),
        )
    }
}

@Composable
private fun LibraryTabRow(
    selected: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(LibraryTab.entries) { tab ->
            LibraryFilterChip(
                label = tab.label,
                isSelected = selected == tab,
                onClick = { onTabSelected(tab) },
            )
        }
    }
}

@Composable
private fun LibraryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
        )
    }
}

@Composable
private fun SortRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LyraIcons.ArrowBack,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Son eklenenler",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = LyraIcons.LibraryMusic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PlaylistRow(
    playlist: LibraryPlaylist,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistArtwork(playlist = playlist)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Çalma listesi · ${playlist.songCount} şarkı",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (playlist.isPinned) {
            Icon(
                imageVector = LyraIcons.Favorite,
                contentDescription = "Sabitli",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Icon(
                imageVector = LyraIcons.ArrowForward,
                contentDescription = "Seçenekler",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun PlaylistArtwork(playlist: LibraryPlaylist) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(playlist.artworkStartColor),
                        Color(playlist.artworkEndColor),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (playlist.isLikedSongs) {
            Icon(
                imageVector = LyraIcons.Favorite,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

private val previewState = LibraryUiState(
    selectedTab = LibraryTab.Playlists,
    playlists = listOf(
        LibraryPlaylist("pl-liked", "Beğenilen Şarkılar", 5, 0xFFFFB1C8, 0xFFFF6B9D, isPinned = true, isLikedSongs = true),
        LibraryPlaylist("pl-1", "Gece Sürüşü", 6, 0xFF8B6FB8, 0xFF4A3D6B),
        LibraryPlaylist("pl-2", "Sabah Kahvesi", 5, 0xFF7C83D9, 0xFF3E4486),
        LibraryPlaylist("pl-3", "Odaklan", 5, 0xFF4AC2A8, 0xFF1F6E5C),
        LibraryPlaylist("pl-4", "Yaz Anıları", 5, 0xFF5AAFC9, 0xFF2A5F73),
        LibraryPlaylist("pl-5", "Akustik Akşam", 4, 0xFF4AC2A8, 0xFF1F6E5C),
    ),
)

@Preview(name = "Library - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun LibraryScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        LibraryScreen(state = previewState, onIntent = {})
    }
}

@Preview(name = "Library - Light", showBackground = true, showSystemUi = true)
@Composable
private fun LibraryScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        LibraryScreen(state = previewState, onIntent = {})
    }
}
