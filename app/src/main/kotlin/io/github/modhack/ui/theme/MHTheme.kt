package io.github.modhack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * CompositionLocal for providing [KeyboardColors] down the tree.
 */
val LocalKeyboardColors = staticCompositionLocalOf<KeyboardColors> {
    error("No KeyboardColors provided")
}

/**
 * Material 3 theme wrapper for ModHack Keyboard.
 *
 * @param themeId The ID of the theme selected in preferences.
 * @param darkTheme Whether the system is in dark mode.
 * @param content The composable content.
 */
@Composable
fun MHTheme(
    themeId: String,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = when (themeId) {
        "material_you" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) darkColorScheme() else lightColorScheme()
            }
        }
        "material_light" -> lightColorScheme()
        "amoled_black" -> darkColorScheme(background = Color.Black, surface = Color.Black)
        "neon_tribute" -> darkColorScheme()
        "ics_tribute" -> lightColorScheme()
        "material_dark" -> darkColorScheme()
        else -> darkColorScheme()
    }
    
    val keyboardColors = keyboardColorsFor(themeId, darkTheme)
    
    CompositionLocalProvider(LocalKeyboardColors provides keyboardColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
