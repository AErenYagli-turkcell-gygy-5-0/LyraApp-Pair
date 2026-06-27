package com.turkcell.lyraapp.ui.auth.otp

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun OtpRoute(
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCompleteProfile: (phoneNumber: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequesters = remember { List(OtpUiState.DIGIT_COUNT) { FocusRequester() } }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OtpEffect.NavigateToHome -> onNavigateToHome()
                is OtpEffect.NavigateToCompleteProfile -> onNavigateToCompleteProfile(effect.phoneNumber)
                is OtpEffect.NavigateBack -> onNavigateBack()
                is OtpEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is OtpEffect.MoveFocus -> focusRequesters[effect.index].requestFocus()
            }
        }
    }

    OtpScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        focusRequesters = focusRequesters,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun OtpScreen(
    state: OtpUiState,
    onIntent: (OtpIntent) -> Unit,
    modifier: Modifier = Modifier,
    focusRequesters: List<FocusRequester> = List(OtpUiState.DIGIT_COUNT) { FocusRequester() },
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

            TopBar(onBackClick = { onIntent(OtpIntent.BackClicked) })
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Dogrulama kodu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "${formatPhoneForDisplay(state.phoneNumber)} numarasina gonderdigimiz 6 haneli kodu gir.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(28.dp))

            OtpDigitRow(
                digits = state.digits,
                focusRequesters = focusRequesters,
                onDigitChanged = { index, value -> onIntent(OtpIntent.DigitChanged(index, value)) },
            )
            Spacer(Modifier.height(16.dp))

            ResendRow(onResendClick = { onIntent(OtpIntent.ResendCode) })

            Spacer(Modifier.weight(1f))

            VerifyButton(
                enabled = state.isVerifyEnabled,
                isLoading = state.isLoading,
                onClick = { onIntent(OtpIntent.Submit) },
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
            text = "2 / 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OtpDigitRow(
    digits: List<String>,
    focusRequesters: List<FocusRequester>,
    onDigitChanged: (Int, String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        digits.forEachIndexed { index, digit ->
            OtpDigitBox(
                value = digit,
                focusRequester = focusRequesters[index],
                onValueChange = { onDigitChanged(index, it) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun OtpDigitBox(
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (value.isNotEmpty()) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = MaterialTheme.typography.headlineSmall.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier
            .focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .border(
                        width = 1.5.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                innerTextField()
            }
        },
    )
}

@Composable
private fun ResendRow(onResendClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Kodu almadın mı? ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Tekrar gönder",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onResendClick),
        )
    }
}

@Composable
private fun VerifyButton(
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
                text = "Doğrula",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = LyraIcons.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun formatPhoneForDisplay(phone: String): String {
    val digits = phone.removePrefix("+").filter { it.isDigit() }
    if (digits.length < 12) return phone
    return "+${digits.substring(0, 2)} ${digits.substring(2, 5)} ${digits.substring(5, 8)} ${digits.substring(8, 10)} ${digits.substring(10, 12)}"
}

