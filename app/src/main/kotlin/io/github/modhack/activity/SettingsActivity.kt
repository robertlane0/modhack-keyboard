package io.github.modhack.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.modhack.ui.settings.SettingsScreen
import io.github.modhack.ui.theme.MHTheme

/**
 * Settings activity — hosts the [SettingsScreen] composable
 * wrapped in the [MHTheme] Material 3 theme.
 *
 * This activity is declared in the manifest with the custom intent
 * filter `io.github.modhack.SETTINGS` and is referenced from
 * `res/xml/method.xml` as the IME settings activity.
 */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MHTheme(themeId = "material_you") {
                SettingsScreen(onBack = { finish() })
            }
        }
    }
}
