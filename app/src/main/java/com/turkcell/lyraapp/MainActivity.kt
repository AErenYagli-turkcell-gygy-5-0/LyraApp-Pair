package com.turkcell.lyraapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.turkcell.lyraapp.data.preferences.ThemePreferenceRepository
import com.turkcell.lyraapp.ui.navigation.LyraNavHost
import com.turkcell.lyraapp.ui.theme.LyraAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Uygulamanın tek Activity'si.
 *
 * [themePreferenceRepository] Singleton olduğundan [ProfileViewModel] ile aynı DataStore
 * akışını paylaşır: Profil ekranındaki tema toggle'ı [setTheme] çağırır → Flow güncellenir →
 * buradaki [isDark] state'i değişir → [LyraAppTheme] yeniden kompoze edilir → tüm uygulama
 * anında güncellenir.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferenceRepository: ThemePreferenceRepository

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Sonuc ne olursa olsun islem gerekmez */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent {
            val isDark by themePreferenceRepository.isDarkTheme
                .collectAsStateWithLifecycle(initialValue = false)
            LyraAppTheme(darkTheme = isDark) {
                LyraNavHost()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
