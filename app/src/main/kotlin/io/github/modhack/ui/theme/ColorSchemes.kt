package io.github.modhack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Custom color palette for the keyboard UI, separate from Material3 ColorScheme
 * to allow for more specific key styling.
 */
data class KeyboardColors(
    val keyBackground: Color,
    val keyForeground: Color,
    val modifierKeyBackground: Color,
    val modifierKeyForeground: Color,
    val keyboardBackground: Color,
    val keyPressedBackground: Color,
    val keyShadow: Color,
    val candidateBackground: Color,
    val candidateText: Color,
    val candidateDivider: Color
)

/**
 * Returns the [KeyboardColors] mapping for the given [themeId].
 */
@Composable
fun keyboardColorsFor(themeId: String, isDark: Boolean = isSystemInDarkTheme()): KeyboardColors {
    return when (themeId) {
        "material_you" -> {
            if (isDark) materialDarkKeyboardColors else materialLightKeyboardColors
        }
        "material_dark" -> materialDarkKeyboardColors
        "material_light" -> materialLightKeyboardColors
        "amoled_black" -> amoledBlackKeyboardColors
        "neon_tribute" -> neonTributeKeyboardColors
        "ics_tribute" -> icsTributeKeyboardColors
        else -> materialDarkKeyboardColors
    }
}

val materialDarkKeyboardColors = KeyboardColors(
    keyBackground = Color(0xFF333333),
    keyForeground = Color.White,
    modifierKeyBackground = Color(0xFF222222),
    modifierKeyForeground = Color.LightGray,
    keyboardBackground = Color(0xFF1E1E1E),
    keyPressedBackground = Color(0xFF555555),
    keyShadow = Color.Black,
    candidateBackground = Color(0xFF1E1E1E),
    candidateText = Color.White,
    candidateDivider = Color.DarkGray
)

val materialLightKeyboardColors = KeyboardColors(
    keyBackground = Color.White,
    keyForeground = Color.Black,
    modifierKeyBackground = Color(0xFFE0E0E0),
    modifierKeyForeground = Color.DarkGray,
    keyboardBackground = Color(0xFFF5F5F5),
    keyPressedBackground = Color(0xFFD0D0D0),
    keyShadow = Color.LightGray,
    candidateBackground = Color(0xFFF5F5F5),
    candidateText = Color.Black,
    candidateDivider = Color.LightGray
)

val amoledBlackKeyboardColors = KeyboardColors(
    keyBackground = Color(0xFF111111),
    keyForeground = Color.White,
    modifierKeyBackground = Color(0xFF000000),
    modifierKeyForeground = Color.White,
    keyboardBackground = Color(0xFF000000),
    keyPressedBackground = Color(0xFF333333),
    keyShadow = Color.Transparent,
    candidateBackground = Color(0xFF000000),
    candidateText = Color.White,
    candidateDivider = Color(0xFF222222)
)

val neonTributeKeyboardColors = KeyboardColors(
    keyBackground = Color(0xCC000000),
    keyForeground = Color(0xFF00FF00),
    modifierKeyBackground = Color(0xAA000000),
    modifierKeyForeground = Color(0xFF00FFFF),
    keyboardBackground = Color(0xFF0A0A0A),
    keyPressedBackground = Color(0xFF003300),
    keyShadow = Color(0xFF00FF00),
    candidateBackground = Color(0xFF0A0A0A),
    candidateText = Color(0xFFFF00FF),
    candidateDivider = Color(0xFF00FF00)
)

val icsTributeKeyboardColors = KeyboardColors(
    keyBackground = Color(0xFFE0E0E0),
    keyForeground = Color.Black,
    modifierKeyBackground = Color(0xFFCCCCCC),
    modifierKeyForeground = Color(0xFF33B5E5),
    keyboardBackground = Color(0xFFF0F0F0),
    keyPressedBackground = Color(0xFF33B5E5),
    keyShadow = Color(0xFF999999),
    candidateBackground = Color(0xFFE0E0E0),
    candidateText = Color(0xFF33B5E5),
    candidateDivider = Color(0xFF999999)
)
