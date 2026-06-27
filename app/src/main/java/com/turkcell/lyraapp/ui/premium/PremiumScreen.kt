package com.turkcell.lyraapp.ui.premium

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.membership.MembershipPlan
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun PremiumRoute(
    onNavigateToPayment: (planType: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PremiumEffect.NavigateToPayment -> onNavigateToPayment(effect.planType)
                is PremiumEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    PremiumScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun PremiumScreen(
    state: PremiumUiState,
    onIntent: (PremiumIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (state.isLoading && state.plans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                ) {
                    Icon(
                        imageVector = LyraIcons.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.height(16.dp))

                PremiumHeader()

                Spacer(Modifier.height(32.dp))

                FeatureList()

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Planını seç",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                Spacer(Modifier.height(12.dp))

                state.plans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        isSelected = state.selectedPlanType == plan.type,
                        onSelect = { onIntent(PremiumIntent.PlanSelected(plan.type)) },
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { onIntent(PremiumIntent.ContinueClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    enabled = state.plans.isNotEmpty(),
                ) {
                    Text(
                        text = "Devam et",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = LyraIcons.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(Modifier.height(8.dp))

                val selectedPlan = state.plans.find { it.type == state.selectedPlanType }
                if (selectedPlan != null) {
                    val footerText = if (selectedPlan.autoRenew) {
                        "Aylık ${formatPrice(selectedPlan.priceLira)}. Dilediğin zaman iptal edebilirsin."
                    } else {
                        "${selectedPlan.durationDays} gün erişim. Otomatik yenileme yok."
                    }
                    Text(
                        text = footerText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PremiumHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.PremiumStar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "LyraApp Premium",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Reklamsız, sınırsız ve çevrimdışı müziğin keyfini çıkar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp),
        )
    }
}

private data class FeatureEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
)

@Composable
private fun FeatureList() {
    val features = listOf(
        FeatureEntry(LyraIcons.NoAds, "Reklamsız dinleme", "Kesintisiz, sınırsız müzik"),
        FeatureEntry(LyraIcons.SkipNext, "Sınırsız atlama", "İstediğin şarkıya geç"),
        FeatureEntry(LyraIcons.Download, "Çevrimdışı indirme", "İnternet olmadan dinle"),
        FeatureEntry(LyraIcons.Waveform, "Yüksek ses kalitesi", "320 kbps net ses"),
        FeatureEntry(LyraIcons.Devices, "Tüm cihazlarında", "Telefon, tablet ve masaüstü"),
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        features.forEach { feature ->
            FeatureRow(feature)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FeatureRow(feature: FeatureEntry) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = feature.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlanCard(
    plan: MembershipPlan,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onSelect)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (plan.autoRenew) "Aylık\nabonelik" else "Tek seferlik",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (plan.autoRenew) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.error)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "Popüler",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError,
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (plan.autoRenew) "İstediğin zaman iptal et"
                else "${plan.durationDays} gün erişim · otomatik yenileme yok",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatPrice(plan.priceLira),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (plan.autoRenew) {
                Text(
                    text = "/ ay",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatPrice(priceLira: Int): String = "₺${priceLira},99"

private val previewPlans = listOf(
    MembershipPlan(
        id = "recurring-monthly",
        type = "recurring",
        name = "Premium (Aylık Yenilenen)",
        description = "Aylık otomatik yenilenen premium abonelik",
        priceKurus = 5999,
        priceLira = 59,
        currency = "TRY",
        durationDays = 30,
        autoRenew = true,
    ),
    MembershipPlan(
        id = "one-time-30",
        type = "one-time",
        name = "Premium (Tek Seferlik)",
        description = "30 günlük tek seferlik premium erişim",
        priceKurus = 7999,
        priceLira = 79,
        currency = "TRY",
        durationDays = 30,
        autoRenew = false,
    ),
)

@Preview(name = "Premium - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun PremiumScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        PremiumScreen(
            state = PremiumUiState(plans = previewPlans),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Premium - Light", showBackground = true, showSystemUi = true)
@Composable
private fun PremiumScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        PremiumScreen(
            state = PremiumUiState(plans = previewPlans),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
