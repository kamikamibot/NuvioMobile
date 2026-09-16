package com.nuvio.app.features.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Debug-only launcher target for reproducing ASS `\\move` animation cadence with the
 * same Android player surface used by Nuvio. The activity is exposed only by the
 * androidApp debug manifest and expects the repository fixture to be copied into
 * app-private storage before launch.
 */
class DebugAssMoveTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (packageName != DEBUG_APPLICATION_ID) {
            finish()
            return
        }

        PlayerSettingsStorage.initialize(applicationContext)
        PlayerSettingsRepository.setAndroidPlaybackEngine(AndroidPlaybackEngine.Libmpv)

        val fixture = File(filesDir, FIXTURE_RELATIVE_PATH)
        setContent {
            MaterialTheme {
                if (!fixture.isFile) {
                    MissingFixtureMessage(fixture)
                } else {
                    var errorMessage by remember { mutableStateOf<String?>(null) }
                    var controller by remember { mutableStateOf<PlayerEngineController?>(null) }

                    DisposableEffect(Unit) {
                        onDispose {
                            controller?.pause()
                            controller?.clearNowPlayingInfo()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                    ) {
                        PlatformPlayerSurface(
                            sourceUrl = fixture.toURI().toString(),
                            modifier = Modifier.fillMaxSize(),
                            playWhenReady = true,
                            onControllerReady = { controller = it },
                            onSnapshot = {},
                            onError = { errorMessage = it },
                        )

                        Text(
                            text = "24 fps local MKV • embedded ASS \\move • libmpv",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(12.dp),
                        )

                        errorMessage?.let { message ->
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val DEBUG_APPLICATION_ID = "com.nuviodebug.com"
        private const val FIXTURE_RELATIVE_PATH = "debug-fixtures/ass-move-24fps-test.mkv"
    }
}

@androidx.compose.runtime.Composable
private fun MissingFixtureMessage(fixture: File) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = buildString {
                appendLine("ASS move test fixture not found.")
                appendLine()
                appendLine("Copy ass-move-24fps-test.mkv to:")
                append(fixture.absolutePath)
            },
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
