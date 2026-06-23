package com.turkcell.lyraapp.ui.auth.completeprofile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun CompleteProfileRoute(
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompleteProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CompleteProfileEffect.NavigateToHome -> onNavigateToHome()
                CompleteProfileEffect.NavigateBack -> onNavigateBack()
                is CompleteProfileEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CompleteProfileScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun CompleteProfileScreen(
    state: CompleteProfileUiState,
    onIntent: (CompleteProfileIntent) -> Unit,
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
                .padding(innerPadding)
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            TopBar(onBackClick = { onIntent(CompleteProfileIntent.BackClicked) })
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Bilgilerini tamamla",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Hos geldin! Profilini olusturmak icin birkac bilgiye ihtiyacimiz var.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(28.dp))

            NameFields(
                firstName = state.firstName,
                lastName = state.lastName,
                onFirstNameChange = { onIntent(CompleteProfileIntent.FirstNameChanged(it)) },
                onLastNameChange = { onIntent(CompleteProfileIntent.LastNameChanged(it)) },
            )
            Spacer(Modifier.height(20.dp))

            Text(
                text = "Dogum tarihi",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))

            BirthDateFields(
                day = state.birthDay,
                month = state.birthMonth,
                year = state.birthYear,
                onDayChange = { onIntent(CompleteProfileIntent.BirthDayChanged(it)) },
                onMonthChange = { onIntent(CompleteProfileIntent.BirthMonthChanged(it)) },
                onYearChange = { onIntent(CompleteProfileIntent.BirthYearChanged(it)) },
            )

            Spacer(Modifier.weight(1f))

            SubmitButton(
                enabled = state.isSubmitEnabled,
                isLoading = state.isLoading,
                onClick = { onIntent(CompleteProfileIntent.Submit) },
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = LyraIcons.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = "3 / 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NameFields(
    firstName: String,
    lastName: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            label = { Text("Ad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            label = { Text("Soyad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
    }
}

@Composable
private fun BirthDateFields(
    day: String,
    month: String,
    year: String,
    onDayChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = day,
            onValueChange = onDayChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("GG") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        OutlinedTextField(
            value = month,
            onValueChange = onMonthChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("AA") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        OutlinedTextField(
            value = year,
            onValueChange = onYearChange,
            singleLine = true,
            modifier = Modifier.weight(1.5f),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("YYYY") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
private fun SubmitButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        } else {
            Text(
                text = "Tamamla",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = LyraIcons.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(name = "CompleteProfile - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun CompleteProfileScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        CompleteProfileScreen(
            state = CompleteProfileUiState(
                firstName = "Zeynep",
                lastName = "Kaya",
                birthDay = "14",
                birthMonth = "06",
                birthYear = "1998",
                isSubmitEnabled = true,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "CompleteProfile - Empty", showBackground = true, showSystemUi = true)
@Composable
private fun CompleteProfileScreenEmptyPreview() {
    LyraAppTheme(darkTheme = true) {
        CompleteProfileScreen(
            state = CompleteProfileUiState(),
            onIntent = {},
        )
    }
}
