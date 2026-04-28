package com.petal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.petal.app.ui.theme.ThemeMode
import com.petal.app.navigation.PetalNavGraph
import com.petal.app.ui.theme.PetalTheme
import com.petal.app.ui.viewmodel.SettingsViewModel
import com.petal.app.data.repository.PushDeviceRegistrar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var dataStore: DataStore<Preferences>
    @Inject lateinit var pushRegistrar: PushDeviceRegistrar

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Best-effort: register the FCM token so the backend can push partner-message
        // notifications. Failure is silent — we'll retry on next launch.
        lifecycleScope.launch {
            runCatching { pushRegistrar.registerIfNeeded() }
        }

        setContent {
            val themeMode by dataStore.data
                .map { prefs -> ThemeMode.fromStorage(prefs[SettingsViewModel.PREF_THEME_MODE]) }
                .collectAsState(initial = ThemeMode.SYSTEM)

            PetalTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PetalNavGraph()
                }
            }
        }
    }
}
