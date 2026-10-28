package net.rubinny.willowbrook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.rubinny.willowbrook.data.settings.Settings
import net.rubinny.willowbrook.ui.navigation.AppNavHost
import net.rubinny.willowbrook.ui.theme.WillowbrookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as WillowbrookApp).container

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = Settings())
            WillowbrookTheme(
                textScale = settings.textScale,
                highContrast = settings.highContrast,
                reduceMotion = settings.reduceMotion,
            ) {
                AppNavHost(container = container, onExitApp = { finish() })
            }
        }
    }
}
