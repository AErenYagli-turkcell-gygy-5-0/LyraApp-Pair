package com.turkcell.lyraapp.ui.createplaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.createplaylist.AvailableSong
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun CreatePlaylistRoute(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatePlaylistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreatePlaylistEffect.Dismiss -> onDismiss()
                is CreatePlaylistEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CreatePlaylistScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun CreatePlaylistScreen(
    state: CreatePlaylistUiState,
    onIntent: (CreatePlaylistIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item {
                TopBar(
                    isSaveEnabled = state.isSaveEnabled,
                    isSaving = state.isSaving,
                    onClose = { onIntent(CreatePlaylistIntent.CloseClicked) },
                    onSave = { onIntent(CreatePlaylistIntent.SaveClicked) },
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                CoverPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                NameField(
                    name = state.name,
                    onNameChange = { onIntent(CreatePlaylistIntent.NameChanged(it)) },
                )
                DescriptionField(
                    description = state.description,
                    onDescriptionChange = { onIntent(CreatePlaylistIntent.DescriptionChanged(it)) },
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                PublicToggleRow(
                    isPublic = state.isPublic,
                    onToggle = { onIntent(CreatePlaylistIntent.PublicToggled) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                SongSectionHeader(selectedCount = state.selectedSongIds.size)
                Spacer(Modifier.height(8.dp))
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(state.availableSongs, key = { it.id }) { song ->
                    SelectableSongRow(
                        song = song,
                        isSelected = song.id in state.selectedSongIds,
                        onClick = { onIntent(CreatePlaylistIntent.SongToggled(song.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    isSaveEnabled: Boolean,
    isSaving: Boolean,
    onClose: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = CloseIcon,
                contentDescription = "Kapat",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.weight(1f))
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Text(
                text = "Kaydet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSaveEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = isSaveEnabled, onClick = onSave)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun CoverPreview(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF8B6FB8), Color(0xFF4A3D6B)),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 80.dp, bottom = 4.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = EditIcon,
                contentDescription = "Düzenle",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun NameField(
    name: String,
    onNameChange: (String) -> Unit,
) {
    TextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        placeholder = {
            Text(
                text = "Çalma listesi adı",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
private fun DescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit,
) {
    TextField(
        value = description,
        onValueChange = onDescriptionChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        placeholder = {
            Text(
                text = "Açıklama ekle",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
private fun PublicToggleRow(
    isPublic: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Herkese açık",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Profilinde gorünür",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = isPublic,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        )
    }
}

@Composable
private fun SongSectionHeader(selectedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Şarkı ekle",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$selectedCount seçili",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SelectableSongRow(
    song: AvailableSong,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(song.artworkStartColor),
                            Color(song.artworkEndColor),
                        ),
                    ),
                ),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        SelectionCircle(isSelected = isSelected)
    }
}

@Composable
private fun SelectionCircle(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier
                        .background(Color.Transparent)
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = CheckIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

private fun buildIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(Color.Black),
    ).build()

private val CloseIcon: ImageVector by lazy {
    buildIcon("Close", "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z")
}

private val EditIcon: ImageVector by lazy {
    buildIcon("Edit", "M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z")
}

private val CheckIcon: ImageVector by lazy {
    buildIcon("Check", "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z")
}

private val previewSongs = listOf(
    AvailableSong("as-1", "Gece Yarısı", "Mavi Deniz", 0xFF4AC2A8, 0xFF1F6E5C),
    AvailableSong("as-2", "Sessiz Şehir", "Ela Tuna", 0xFF9B7FC4, 0xFF5A4480),
    AvailableSong("as-3", "Yıldız Tozu", "Polaris", 0xFF5AAFC9, 0xFF2A5F73),
    AvailableSong("as-4", "Sahil Yolu", "Kumsal", 0xFF4AC2A8, 0xFF1F6E5C),
)

@Preview(name = "CreatePlaylist - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun CreatePlaylistDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        CreatePlaylistScreen(
            state = CreatePlaylistUiState(
                availableSongs = previewSongs,
                selectedSongIds = setOf("as-1", "as-3"),
                name = "Gece Karışımı",
                isPublic = true,
                isSaveEnabled = true,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "CreatePlaylist - Light", showBackground = true, showSystemUi = true)
@Composable
private fun CreatePlaylistLightPreview() {
    LyraAppTheme(darkTheme = false) {
        CreatePlaylistScreen(
            state = CreatePlaylistUiState(availableSongs = previewSongs),
            onIntent = {},
        )
    }
}
