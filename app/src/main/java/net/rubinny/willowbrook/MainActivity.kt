package net.rubinny.willowbrook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import net.rubinny.willowbrook.ui.components.InfoDialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.rubinny.willowbrook.data.settings.Settings
import net.rubinny.willowbrook.ui.navigation.AppNavHost
import net.rubinny.willowbrook.ui.theme.WillowbrookTheme

class MainActivity : ComponentActivity() {
    private val container get() = (application as WillowbrookApp).container

    override fun onStart() {
        super.onStart()
        container.musicPlayer.onForeground()
    }

    /** Leaving the app (Home button, another app) silences music and speech. */
    override fun onStop() {
        container.musicPlayer.onBackground()
        container.narrator.stop()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stoppedUnexpectedly = savedInstanceState == null && CrashGuard.consumeStoppedUnexpectedly(this)
        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = Settings())
            LaunchedEffect(settings.backgroundMusicVolume) { container.musicPlayer.setVolume(settings.backgroundMusicVolume) }
            var showRecovery by rememberSaveable { mutableStateOf(stoppedUnexpectedly) }
            WillowbrookTheme(
                textScale = settings.textScale,
                highContrast = settings.highContrast,
                reduceMotion = settings.reduceMotion,
            ) {
                AppNavHost(container = container, onExitApp = { finish() })
                if (showRecovery) {
                    InfoDialog(
                        title = "Welcome back",
                        paragraphs = listOf("Something went wrong last time, so the game closed.", "Don't worry: your progress is saved."),
                        buttonText = "Carry on",
                        onDismiss = { showRecovery = false },
                    )
                }
            }
        }
    }
}
