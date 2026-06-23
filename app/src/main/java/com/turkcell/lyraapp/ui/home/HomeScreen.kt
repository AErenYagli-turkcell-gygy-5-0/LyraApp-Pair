package com.turkcell.lyraapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun HomeRoute(
    onNavigateToProfile: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onIntent(HomeIntent.ScreenResumed)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowError -> {
                    snackbarHostState.showSnackbar(message = effect.message)
                }
                is HomeEffect.NavigateToNowPlaying -> onNavigateToNowPlaying()
            }
        }
    }

    HomeScreen(
        state = uiState,
        onNavigateToProfile = onNavigateToProfile,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val hasData = state.forYouSongs.isNotEmpty() ||
            state.recentlyPlayedSongs.isNotEmpty() ||
            state.recommendationSongs.isNotEmpty()

        when {
            state.isLoading && !hasData -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null && !hasData -> {
                ErrorContent(
                    message = state.errorMessage,
                    onRetry = { onIntent(HomeIntent.Retry) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        HomeHeader(
                            greeting = state.greeting,
                            userInitials = state.userInitials,
                            isDarkTheme = state.isDarkTheme,
                            onAvatarClick = onNavigateToProfile,
                            onThemeToggle = { onIntent(HomeIntent.ToggleTheme) },
                        )
                    }
                    if (state.forYouSongs.isNotEmpty()) {
                        item { ForYouGrid(songs = state.forYouSongs, onIntent = onIntent) }
                    }
                    item { SectionHeader(title = "Son çalınanlar", trailingText = "Tümü") }
                    if (state.recentlyPlayedSongs.isNotEmpty()) {
                        item { RecentlyPlayedRow(songs = state.recentlyPlayedSongs, onIntent = onIntent) }
                    } else if (!state.isLoading) {
                        item { EmptySection(message = "Henüz çalınan şarkı yok.") }
                    }
                    item { SectionHeader(title = "Senin için öneriler") }
                    if (state.recommendationSongs.isNotEmpty()) {
                        item { RecommendationsRow(songs = state.recommendationSongs, onIntent = onIntent) }
                    } else if (!state.isLoading) {
                        item { EmptySection(message = "Henüz öneri yok.") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = "Tekrar dene")
        }
    }
}

@Composable
private fun EmptySection(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp),
    )
}

@Composable
private fun HomeHeader(
    greeting: String,
    userInitials: String,
    isDarkTheme: Boolean,
    onAvatarClick: () -> Unit,
    onThemeToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ne dinlemek istersin?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Icon(
            imageVector = if (isDarkTheme) LyraIcons.Moon else LyraIcons.LightMode,
            contentDescription = if (isDarkTheme) "Aydınlık temaya geç" else "Karanlık temaya geç",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onThemeToggle),
        )
        Spacer(Modifier.width(16.dp))
        UserAvatar(initials = userInitials, onClick = onAvatarClick)
    }
}

@Composable
private fun UserAvatar(initials: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ForYouGrid(songs: List<HomeSong>, onIntent: (HomeIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        songs.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { song ->
                    ForYouCard(song = song, onIntent = onIntent, modifier = Modifier.weight(1f))
                }
                if (rowItems.size < 2) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ForYouCard(
    song: HomeSong,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onIntent(HomeIntent.SongClicked(song)) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(
            startColor = song.artworkStartColor,
            endColor = song.artworkEndColor,
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight(),
        )
        Text(
            text = song.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailingText: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun RecentlyPlayedRow(songs: List<HomeSong>, onIntent: (HomeIntent) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(songs, key = { it.id }) { song ->
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .clickable { onIntent(HomeIntent.SongClicked(song)) },
            ) {
                Artwork(
                    startColor = song.artworkStartColor,
                    endColor = song.artworkEndColor,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RecommendationsRow(songs: List<HomeSong>, onIntent: (HomeIntent) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(songs, key = { it.id }) { song ->
            Column(
                modifier = Modifier
                    .width(170.dp)
                    .clickable { onIntent(HomeIntent.SongClicked(song)) },
            ) {
                Artwork(
                    startColor = song.artworkStartColor,
                    endColor = song.artworkEndColor,
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun Artwork(
    startColor: Long,
    endColor: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Brush.linearGradient(listOf(Color(startColor), Color(endColor))))
            .background(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                ),
            ),
    )
}

private val previewState = HomeUiState(
    greeting = "İyi akşamlar",
    userInitials = "ZK",
    isDarkTheme = false,
    forYouSongs = listOf(
        HomeSong("s_neon-tide", "Neon Tide", "Aurora Drift", "City Lights", 32000, 0xFF8B6FB8, 0xFF4A3D6B),
        HomeSong("s_solar-flare", "Solar Flare", "Neon Pulse", "Cosmic Rays", 28000, 0xFF7C83D9, 0xFF3E4486),
        HomeSong("s_kervan", "Kervan", "City Echo", "Yolculuk", 45000, 0xFFD98E4A, 0xFF8A5526),
        HomeSong("s_midnight-run", "Midnight Run", "Deep Wave", "Night Shift", 38000, 0xFF4AC2A8, 0xFF1F6E5C),
        HomeSong("s_ocean-drive", "Ocean Drive", "Solar Flare", "Summer Nights", 41000, 0xFF6FBF5A, 0xFF356B2A),
        HomeSong("s_starlight", "Starlight", "Cosmo Beat", "Galaxy", 35000, 0xFF5AAFC9, 0xFF2A5F73),
    ),
    recentlyPlayedSongs = listOf(
        HomeSong("s_neon-tide", "Neon Tide", "Aurora Drift", "City Lights", 32000, 0xFFD98E4A, 0xFF8A5526),
        HomeSong("s_ocean-drive", "Ocean Drive", "Solar Flare", "Summer Nights", 41000, 0xFF6FBF5A, 0xFF356B2A),
        HomeSong("s_starlight", "Starlight", "Cosmo Beat", "Galaxy", 35000, 0xFF3D5A80, 0xFF1B2A45),
    ),
    recommendationSongs = listOf(
        HomeSong("s_midnight-run", "Midnight Run", "Deep Wave", "Night Shift", 38000, 0xFF9B7FC4, 0xFF5A4480),
        HomeSong("s_solar-flare", "Solar Flare", "Neon Pulse", "Cosmic Rays", 28000, 0xFF6B5FB8, 0xFF3A3270),
        HomeSong("s_kervan", "Kervan", "City Echo", "Yolculuk", 45000, 0xFF3FAE9C, 0xFF1E5D52),
    ),
)

@Preview(name = "Home - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        HomeScreen(state = previewState.copy(isDarkTheme = true), onIntent = {}, onNavigateToProfile = {})
    }
}

@Preview(name = "Home - Light", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        HomeScreen(state = previewState, onIntent = {}, onNavigateToProfile = {})
    }
}

@Preview(name = "Home - Loading", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenLoadingPreview() {
    LyraAppTheme(darkTheme = true) {
        HomeScreen(state = HomeUiState(isLoading = true, isDarkTheme = true), onIntent = {}, onNavigateToProfile = {})
    }
}

@Preview(name = "Home - Error", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenErrorPreview() {
    LyraAppTheme(darkTheme = true) {
        HomeScreen(
            state = HomeUiState(errorMessage = "Baglanti hatasi. Lutfen tekrar deneyin.", isDarkTheme = true),
            onIntent = {},
            onNavigateToProfile = {},
        )
    }
}
