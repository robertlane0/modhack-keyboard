package io.github.modhack.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.modhack.prefs.KeyboardPreferences
import io.github.modhack.prefs.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen.
 *
 * Bridges the [PreferencesRepository] to the Compose UI layer,
 * exposing a [StateFlow] of [KeyboardPreferences] and providing
 * suspend functions for updating individual preferences.
 *
 * @param application The application context, used to access DataStore.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository(application)

    /** Current keyboard preferences, emitted as a [StateFlow] for Compose collection. */
    val preferences: StateFlow<KeyboardPreferences> = repository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = KeyboardPreferences()
        )

    /**
     * Updates the theme preference.
     *
     * @param theme The theme ID string (e.g., "material_you", "amoled_black").
     */
    fun updateTheme(theme: String) = update { it.copy(theme = theme) }

    /**
     * Updates the key label scale.
     *
     * @param scale The label scale multiplier (0.5–2.0).
     */
    fun updateLabelScale(scale: Float) = update { it.copy(labelScale = scale) }

    /**
     * Updates the portrait keyboard height.
     *
     * @param height Fraction of screen height (0.15–0.75).
     */
    fun updateHeightPortrait(height: Float) = update { it.copy(heightPortrait = height) }

    /**
     * Updates the landscape keyboard height.
     *
     * @param height Fraction of screen height (0.15–0.75).
     */
    fun updateHeightLandscape(height: Float) = update { it.copy(heightLandscape = height) }

    /**
     * Updates the portrait layout mode.
     *
     * @param mode Layout mode string ("qwerty", "compact", "full", "full_fn").
     */
    fun updateModePortrait(mode: String) = update { it.copy(modePortrait = mode) }

    /**
     * Updates the landscape layout mode.
     *
     * @param mode Layout mode string.
     */
    fun updateModeLandscape(mode: String) = update { it.copy(modeLandscape = mode) }

    /**
     * Toggles auto-capitalization.
     */
    fun updateAutoCap(enabled: Boolean) = update { it.copy(autoCap = enabled) }

    /**
     * Toggles shift lock modifiers.
     */
    fun updateShiftLockModifiers(enabled: Boolean) = update { it.copy(shiftLockModifiers = enabled) }

    /**
     * Toggles ConnectBot tab hack.
     */
    fun updateConnectbotTabHack(enabled: Boolean) = update { it.copy(connectbotTabHack = enabled) }

    /**
     * Updates the swipe up action.
     *
     * @param action Action string ("none", "hide", "next_language").
     */
    fun updateSwipeUp(action: String) = update { it.copy(swipeUp = action) }

    /**
     * Updates the swipe down action.
     *
     * @param action Action string ("none", "hide", "next_language").
     */
    fun updateSwipeDown(action: String) = update { it.copy(swipeDown = action) }

    /**
     * Updates the long press duration.
     *
     * @param durationMs Duration in milliseconds.
     */
    fun updateLongPressDuration(durationMs: Int) = update { it.copy(longPressDuration = durationMs) }

    /**
     * Toggles haptic feedback.
     */
    fun updateVibrateOn(enabled: Boolean) = update { it.copy(vibrateOn = enabled) }

    /**
     * Updates vibration duration.
     *
     * @param durationMs Duration in milliseconds.
     */
    fun updateVibrateLen(durationMs: Int) = update { it.copy(vibrateLen = durationMs) }

    /**
     * Toggles sound on keypress.
     */
    fun updateSoundOn(enabled: Boolean) = update { it.copy(soundOn = enabled) }

    /**
     * Toggles key preview popup.
     */
    fun updatePopupOn(enabled: Boolean) = update { it.copy(popupOn = enabled) }

    /**
     * Toggles quick fixes.
     */
    fun updateQuickFixes(enabled: Boolean) = update { it.copy(quickFixes = enabled) }

    /**
     * Toggles auto-complete suggestions.
     */
    fun updateAutoComplete(enabled: Boolean) = update { it.copy(autoComplete = enabled) }

    /**
     * Toggles recorrection.
     */
    fun updateRecorrection(enabled: Boolean) = update { it.copy(recorrection = enabled) }

    private fun update(transform: (KeyboardPreferences) -> KeyboardPreferences) {
        viewModelScope.launch {
            repository.update(transform)
        }
    }
}
