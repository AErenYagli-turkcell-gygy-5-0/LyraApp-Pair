package com.turkcell.lyraapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by themePreferenceRepository.isDarkTheme
                .collectAsStateWithLifecycle(initialValue = false)
            LyraAppTheme(darkTheme = isDark) {
                LyraNavHost()
            }
        }
    }
}
