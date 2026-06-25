package io.github.modhack.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** DataStore instance scoped to the application [Context]. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "modhack_preferences"
)

/**
 * Repository for reading and writing keyboard preferences via Jetpack DataStore.
 *
 * Exposes a [Flow] of [KeyboardPreferences] for reactive observation.
 * All writes are suspend functions executed on [kotlinx.coroutines.Dispatchers.IO].
 *
 * Replaces the legacy global `SharedPreferences` singleton pattern.
 *
 * @param context Application context used to access the DataStore instance.
 *
 * @see KeyboardPreferences
 */
class PreferencesRepository(private val context: Context) {

    // ── DataStore Keys ────────────────────────────────────────────────

    private object Keys {
        val HEIGHT_PORTRAIT = floatPreferencesKey("height_portrait")
        val HEIGHT_LANDSCAPE = floatPreferencesKey("height_landscape")
        val MODE_PORTRAIT = stringPreferencesKey("mode_portrait")
        val MODE_LANDSCAPE = stringPreferencesKey("mode_landscape")
        val THEME = stringPreferencesKey("theme")
        val LABEL_SCALE = floatPreferencesKey("label_scale")
        val VIBRATE_ON = booleanPreferencesKey("vibrate_on")
        val VIBRATE_LEN = intPreferencesKey("vibrate_len")
        val SOUND_ON = booleanPreferencesKey("sound_on")
        val POPUP_ON = booleanPreferencesKey("popup_on")
        val LONG_PRESS_DURATION = intPreferencesKey("long_press_duration")
        val AUTO_CAP = booleanPreferencesKey("auto_cap")
        val SHIFT_LOCK_MODIFIERS = booleanPreferencesKey("pref_shift_lock_modifiers")
        val CONNECTBOT_TAB_HACK = booleanPreferencesKey("connectbot_tab_hack")
        val SWIPE_UP = stringPreferencesKey("swipe_up")
        val SWIPE_DOWN = stringPreferencesKey("swipe_down")
        val QUICK_FIXES = booleanPreferencesKey("quick_fixes")
        val AUTO_COMPLETE = booleanPreferencesKey("auto_complete")
        val RECORRECTION = booleanPreferencesKey("recorrection")
    }

    // ── Read ──────────────────────────────────────────────────────────

    /**
     * A [Flow] that emits a new [KeyboardPreferences] whenever any
     * preference value changes. Uses defaults from [KeyboardPreferences]
     * for any unset keys.
     */
    val preferences: Flow<KeyboardPreferences> = context.dataStore.data.map { prefs ->
        val defaults = KeyboardPreferences()
        KeyboardPreferences(
            heightPortrait = prefs[Keys.HEIGHT_PORTRAIT] ?: defaults.heightPortrait,
            heightLandscape = prefs[Keys.HEIGHT_LANDSCAPE] ?: defaults.heightLandscape,
            modePortrait = prefs[Keys.MODE_PORTRAIT] ?: defaults.modePortrait,
            modeLandscape = prefs[Keys.MODE_LANDSCAPE] ?: defaults.modeLandscape,
            theme = prefs[Keys.THEME] ?: defaults.theme,
            labelScale = prefs[Keys.LABEL_SCALE] ?: defaults.labelScale,
            vibrateOn = prefs[Keys.VIBRATE_ON] ?: defaults.vibrateOn,
            vibrateLen = prefs[Keys.VIBRATE_LEN] ?: defaults.vibrateLen,
            soundOn = prefs[Keys.SOUND_ON] ?: defaults.soundOn,
            popupOn = prefs[Keys.POPUP_ON] ?: defaults.popupOn,
            longPressDuration = prefs[Keys.LONG_PRESS_DURATION] ?: defaults.longPressDuration,
            autoCap = prefs[Keys.AUTO_CAP] ?: defaults.autoCap,
            shiftLockModifiers = prefs[Keys.SHIFT_LOCK_MODIFIERS] ?: defaults.shiftLockModifiers,
            connectbotTabHack = prefs[Keys.CONNECTBOT_TAB_HACK] ?: defaults.connectbotTabHack,
            swipeUp = prefs[Keys.SWIPE_UP] ?: defaults.swipeUp,
            swipeDown = prefs[Keys.SWIPE_DOWN] ?: defaults.swipeDown,
            quickFixes = prefs[Keys.QUICK_FIXES] ?: defaults.quickFixes,
            autoComplete = prefs[Keys.AUTO_COMPLETE] ?: defaults.autoComplete,
            recorrection = prefs[Keys.RECORRECTION] ?: defaults.recorrection
        )
    }

    // ── Write ─────────────────────────────────────────────────────────

    /**
     * Persists the given [KeyboardPreferences] to DataStore.
     *
     * This is a suspend function and should be called from a coroutine
     * scope on [kotlinx.coroutines.Dispatchers.IO].
     */
    suspend fun updatePreferences(update: KeyboardPreferences) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HEIGHT_PORTRAIT] = update.heightPortrait
            prefs[Keys.HEIGHT_LANDSCAPE] = update.heightLandscape
            prefs[Keys.MODE_PORTRAIT] = update.modePortrait
            prefs[Keys.MODE_LANDSCAPE] = update.modeLandscape
            prefs[Keys.THEME] = update.theme
            prefs[Keys.LABEL_SCALE] = update.labelScale
            prefs[Keys.VIBRATE_ON] = update.vibrateOn
            prefs[Keys.VIBRATE_LEN] = update.vibrateLen
            prefs[Keys.SOUND_ON] = update.soundOn
            prefs[Keys.POPUP_ON] = update.popupOn
            prefs[Keys.LONG_PRESS_DURATION] = update.longPressDuration
            prefs[Keys.AUTO_CAP] = update.autoCap
            prefs[Keys.SHIFT_LOCK_MODIFIERS] = update.shiftLockModifiers
            prefs[Keys.CONNECTBOT_TAB_HACK] = update.connectbotTabHack
            prefs[Keys.SWIPE_UP] = update.swipeUp
            prefs[Keys.SWIPE_DOWN] = update.swipeDown
            prefs[Keys.QUICK_FIXES] = update.quickFixes
            prefs[Keys.AUTO_COMPLETE] = update.autoComplete
            prefs[Keys.RECORRECTION] = update.recorrection
        }
    }

    /**
     * Updates a single preference value by applying a transform function.
     *
     * @param transform A function that receives the current [KeyboardPreferences]
     *   and returns a modified copy.
     */
    suspend fun update(transform: (KeyboardPreferences) -> KeyboardPreferences) {
        // Read current, transform, write back
        context.dataStore.edit { prefs ->
            val defaults = KeyboardPreferences()
            val current = KeyboardPreferences(
                heightPortrait = prefs[Keys.HEIGHT_PORTRAIT] ?: defaults.heightPortrait,
                heightLandscape = prefs[Keys.HEIGHT_LANDSCAPE] ?: defaults.heightLandscape,
                modePortrait = prefs[Keys.MODE_PORTRAIT] ?: defaults.modePortrait,
                modeLandscape = prefs[Keys.MODE_LANDSCAPE] ?: defaults.modeLandscape,
                theme = prefs[Keys.THEME] ?: defaults.theme,
                labelScale = prefs[Keys.LABEL_SCALE] ?: defaults.labelScale,
                vibrateOn = prefs[Keys.VIBRATE_ON] ?: defaults.vibrateOn,
                vibrateLen = prefs[Keys.VIBRATE_LEN] ?: defaults.vibrateLen,
                soundOn = prefs[Keys.SOUND_ON] ?: defaults.soundOn,
                popupOn = prefs[Keys.POPUP_ON] ?: defaults.popupOn,
                longPressDuration = prefs[Keys.LONG_PRESS_DURATION] ?: defaults.longPressDuration,
                autoCap = prefs[Keys.AUTO_CAP] ?: defaults.autoCap,
                shiftLockModifiers = prefs[Keys.SHIFT_LOCK_MODIFIERS] ?: defaults.shiftLockModifiers,
                connectbotTabHack = prefs[Keys.CONNECTBOT_TAB_HACK] ?: defaults.connectbotTabHack,
                swipeUp = prefs[Keys.SWIPE_UP] ?: defaults.swipeUp,
                swipeDown = prefs[Keys.SWIPE_DOWN] ?: defaults.swipeDown,
                quickFixes = prefs[Keys.QUICK_FIXES] ?: defaults.quickFixes,
                autoComplete = prefs[Keys.AUTO_COMPLETE] ?: defaults.autoComplete,
                recorrection = prefs[Keys.RECORRECTION] ?: defaults.recorrection
            )
            val updated = transform(current)
            prefs[Keys.HEIGHT_PORTRAIT] = updated.heightPortrait
            prefs[Keys.HEIGHT_LANDSCAPE] = updated.heightLandscape
            prefs[Keys.MODE_PORTRAIT] = updated.modePortrait
            prefs[Keys.MODE_LANDSCAPE] = updated.modeLandscape
            prefs[Keys.THEME] = updated.theme
            prefs[Keys.LABEL_SCALE] = updated.labelScale
            prefs[Keys.VIBRATE_ON] = updated.vibrateOn
            prefs[Keys.VIBRATE_LEN] = updated.vibrateLen
            prefs[Keys.SOUND_ON] = updated.soundOn
            prefs[Keys.POPUP_ON] = updated.popupOn
            prefs[Keys.LONG_PRESS_DURATION] = updated.longPressDuration
            prefs[Keys.AUTO_CAP] = updated.autoCap
            prefs[Keys.SHIFT_LOCK_MODIFIERS] = updated.shiftLockModifiers
            prefs[Keys.CONNECTBOT_TAB_HACK] = updated.connectbotTabHack
            prefs[Keys.SWIPE_UP] = updated.swipeUp
            prefs[Keys.SWIPE_DOWN] = updated.swipeDown
            prefs[Keys.QUICK_FIXES] = updated.quickFixes
            prefs[Keys.AUTO_COMPLETE] = updated.autoComplete
            prefs[Keys.RECORRECTION] = updated.recorrection
        }
    }
}
