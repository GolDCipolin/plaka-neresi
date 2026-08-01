package com.plakaneresi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.plakaneresi.app.settings.SettingsStore
import com.plakaneresi.app.ui.HomeScreen
import com.plakaneresi.app.ui.theme.PlakaNeresiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Off the main thread: initialize() does disk and network I/O and will jank
        // the first frame if it runs inline.
        lifecycleScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@MainActivity)
        }

        val settings = SettingsStore(this)

        setContent {
            // Seeded from disk on every activity create, so a config change reads the
            // stored value back rather than needing its own saved-state entry.
            var themeMode by remember { mutableStateOf(settings.themeMode) }

            PlakaNeresiTheme(themeMode = themeMode) {
                HomeScreen(
                    themeMode = themeMode,
                    onThemeModeChange = { mode ->
                        themeMode = mode
                        settings.themeMode = mode
                    },
                )
            }
        }
    }
}
