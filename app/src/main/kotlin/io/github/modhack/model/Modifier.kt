package io.github.modhack.model

/**
 * Keyboard modifier keys that can be held or toggled.
 *
 * Each modifier maps to an [android.view.KeyEvent] meta-state flag
 * for constructing modified key events via [InputConnection.sendKeyEvent].
 *
 * @property metaFlag The Android [android.view.KeyEvent] meta-state flag
 *   for this modifier.
 * @property label Human-readable label for display on the keyboard.
 */
enum class Modifier(val metaFlag: Int, val label: String) {

    /** Shift modifier — toggles uppercase / shifted symbols. */
    SHIFT(android.view.KeyEvent.META_SHIFT_ON, "Shift"),

    /** Ctrl modifier — for terminal and editor shortcuts. */
    CTRL(android.view.KeyEvent.META_CTRL_ON, "Ctrl"),

    /** Alt modifier — for alternate character input and shortcuts. */
    ALT(android.view.KeyEvent.META_ALT_ON, "Alt"),

    /** Meta / Super modifier — for system-level shortcuts. */
    META(android.view.KeyEvent.META_META_ON, "Meta"),

    /** Fn modifier — for function key remapping (arrows → Page Up/Down, Home/End). */
    FN(android.view.KeyEvent.META_FUNCTION_ON, "Fn");

    companion object {
        /**
         * Returns the [Modifier] corresponding to the given internal keycode,
         * or `null` if the keycode is not a modifier.
         */
        fun fromKeyCode(code: Int): Modifier? = when (code) {
            io.github.modhack.keycodes.KeyCodes.SHIFT -> SHIFT
            io.github.modhack.keycodes.KeyCodes.CTRL_LEFT -> CTRL
            io.github.modhack.keycodes.KeyCodes.ALT_LEFT -> ALT
            io.github.modhack.keycodes.KeyCodes.META_LEFT -> META
            io.github.modhack.keycodes.KeyCodes.FN -> FN
            else -> null
        }
    }
}
