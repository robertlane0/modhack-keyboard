package io.github.modhack.prefs

/**
 * Complete keyboard preferences data class matching all DataStore keys from §16.
 *
 * This is the single source of truth for all user-configurable settings.
 * The [PreferencesRepository] emits instances of this class as a [Flow].
 *
 * @property heightPortrait Keyboard height as fraction of screen (0.15–0.75).
 * @property heightLandscape Keyboard height in landscape.
 * @property modePortrait Layout mode in portrait: "qwerty", "compact", "full", "full_fn".
 * @property modeLandscape Layout mode in landscape.
 * @property theme Visual theme ID: "material_you", "material_dark", "material_light",
 *   "amoled_black", "neon_tribute", "ics_tribute".
 * @property labelScale Key label text scale multiplier.
 * @property vibrateOn Whether haptic feedback is enabled.
 * @property vibrateLen Vibration duration in milliseconds.
 * @property soundOn Whether sound feedback is enabled.
 * @property popupOn Whether key press preview popup is shown.
 * @property longPressDuration Long-press detection delay in milliseconds.
 * @property autoCap Whether auto-capitalization is enabled.
 * @property shiftLockModifiers Whether shift lock also locks Ctrl/Alt/Meta.
 * @property connectbotTabHack Whether to send Ctrl+I instead of Tab (ConnectBot compat).
 * @property swipeUp Action to perform on swipe up: "none", "hide", "next_language".
 * @property swipeDown Action to perform on swipe down: "none", "hide", "next_language".
 * @property quickFixes Whether quick-fix auto-corrections are enabled.
 * @property autoComplete Whether auto-complete suggestions are enabled.
 * @property recorrection Whether recorrection (tap to fix) is enabled.
 */
data class KeyboardPreferences(
    val heightPortrait: Float = 0.4f,
    val heightLandscape: Float = 0.4f,
    val modePortrait: String = "qwerty",
    val modeLandscape: String = "full",
    val theme: String = "material_you",
    val labelScale: Float = 1.0f,
    val vibrateOn: Boolean = false,
    val vibrateLen: Int = 50,
    val soundOn: Boolean = false,
    val popupOn: Boolean = true,
    val longPressDuration: Int = 400,
    val autoCap: Boolean = true,
    val shiftLockModifiers: Boolean = true,
    val connectbotTabHack: Boolean = true,
    val swipeUp: String = "none",
    val swipeDown: String = "hide",
    val quickFixes: Boolean = true,
    val autoComplete: Boolean = true,
    val recorrection: Boolean = false
)
