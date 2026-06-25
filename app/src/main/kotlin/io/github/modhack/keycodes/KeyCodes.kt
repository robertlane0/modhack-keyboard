package io.github.modhack.keycodes

/**
 * All special key codes for ModHack Keyboard.
 *
 * Positive values map to standard character codes (e.g. ASCII).
 * Negative values are internal IME-specific routing codes that do not
 * correspond to [android.view.KeyEvent] constants directly.
 *
 * @see android.view.KeyEvent
 */
object KeyCodes {

    // ── Standard character codes ──────────────────────────────────────

    /** Horizontal tab (ASCII 9). */
    const val TAB = 9

    /** Enter / newline (ASCII 10). */
    const val ENTER = 10

    /** Space character (ASCII 32). */
    const val SPACE = 32

    // ── IME-internal negative codes ───────────────────────────────────

    /** Shift modifier toggle. */
    const val SHIFT = -1

    /** Switch to symbols layout. */
    const val SYMBOL = -2

    /** Backspace / delete. */
    const val DELETE = -5

    /** Open settings activity. */
    const val SETTINGS = -100

    /** Voice input trigger. */
    const val VOICE = -102

    /** F1 soft key (context-dependent action). */
    const val F1 = -103

    /** Switch to next language subtype. */
    const val NEXT_LANGUAGE = -104

    /** Switch to previous language subtype. */
    const val PREV_LANGUAGE = -105

    /** Compose key — starts X11 compose sequence. */
    const val COMPOSE = -10024

    // ── D-pad / navigation ────────────────────────────────────────────

    /** D-pad up. */
    const val DPAD_UP = -19

    /** D-pad down. */
    const val DPAD_DOWN = -20

    /** D-pad left. */
    const val DPAD_LEFT = -21

    /** D-pad right. */
    const val DPAD_RIGHT = -22

    // ── Special keys ──────────────────────────────────────────────────

    /** Escape key. */
    const val ESCAPE = -111

    /** Left Ctrl modifier. */
    const val CTRL_LEFT = -113

    /** Left Meta / Super modifier. */
    const val META_LEFT = -117

    /** Fn modifier. */
    const val FN = -119

    /** Home key (navigation). */
    const val HOME = -122

    /** End key (navigation). */
    const val END = -123

    /** Insert key. */
    const val INSERT = -124

    /** Page Up. */
    const val PAGE_UP = -92

    /** Page Down. */
    const val PAGE_DOWN = -93

    /** Left Alt modifier. */
    const val ALT_LEFT = -57

    // ── Function keys F1–F12 ──────────────────────────────────────────

    /** Function key F1. */
    const val FUNC_F1 = -131

    /** Function key F2. */
    const val FUNC_F2 = -132

    /** Function key F3. */
    const val FUNC_F3 = -133

    /** Function key F4. */
    const val FUNC_F4 = -134

    /** Function key F5. */
    const val FUNC_F5 = -135

    /** Function key F6. */
    const val FUNC_F6 = -136

    /** Function key F7. */
    const val FUNC_F7 = -137

    /** Function key F8. */
    const val FUNC_F8 = -138

    /** Function key F9. */
    const val FUNC_F9 = -139

    /** Function key F10. */
    const val FUNC_F10 = -140

    /** Function key F11. */
    const val FUNC_F11 = -141

    /** Function key F12. */
    const val FUNC_F12 = -142

    /**
     * Returns `true` if [code] is a modifier key code
     * (Shift, Ctrl, Alt, Meta, or Fn).
     */
    fun isModifier(code: Int): Boolean = code in setOf(
        SHIFT, CTRL_LEFT, ALT_LEFT, META_LEFT, FN
    )

    /**
     * Returns `true` if [code] is a navigation key
     * (arrow keys, Home, End, Page Up/Down).
     */
    fun isNavigation(code: Int): Boolean = code in setOf(
        DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT,
        HOME, END, PAGE_UP, PAGE_DOWN
    )

    /**
     * Returns `true` if [code] is a function key (F1–F12).
     */
    fun isFunctionKey(code: Int): Boolean = code in FUNC_F12..FUNC_F1

    /**
     * Maps an internal negative navigation code to its corresponding
     * [android.view.KeyEvent] keycode, or `null` if not a navigation key.
     */
    fun toAndroidKeyCode(code: Int): Int? = when (code) {
        DPAD_UP -> android.view.KeyEvent.KEYCODE_DPAD_UP
        DPAD_DOWN -> android.view.KeyEvent.KEYCODE_DPAD_DOWN
        DPAD_LEFT -> android.view.KeyEvent.KEYCODE_DPAD_LEFT
        DPAD_RIGHT -> android.view.KeyEvent.KEYCODE_DPAD_RIGHT
        TAB -> android.view.KeyEvent.KEYCODE_TAB
        ENTER -> android.view.KeyEvent.KEYCODE_ENTER
        ESCAPE -> android.view.KeyEvent.KEYCODE_ESCAPE
        HOME -> android.view.KeyEvent.KEYCODE_MOVE_HOME
        END -> android.view.KeyEvent.KEYCODE_MOVE_END
        PAGE_UP -> android.view.KeyEvent.KEYCODE_PAGE_UP
        PAGE_DOWN -> android.view.KeyEvent.KEYCODE_PAGE_DOWN
        INSERT -> android.view.KeyEvent.KEYCODE_INSERT
        FUNC_F1 -> android.view.KeyEvent.KEYCODE_F1
        FUNC_F2 -> android.view.KeyEvent.KEYCODE_F2
        FUNC_F3 -> android.view.KeyEvent.KEYCODE_F3
        FUNC_F4 -> android.view.KeyEvent.KEYCODE_F4
        FUNC_F5 -> android.view.KeyEvent.KEYCODE_F5
        FUNC_F6 -> android.view.KeyEvent.KEYCODE_F6
        FUNC_F7 -> android.view.KeyEvent.KEYCODE_F7
        FUNC_F8 -> android.view.KeyEvent.KEYCODE_F8
        FUNC_F9 -> android.view.KeyEvent.KEYCODE_F9
        FUNC_F10 -> android.view.KeyEvent.KEYCODE_F10
        FUNC_F11 -> android.view.KeyEvent.KEYCODE_F11
        FUNC_F12 -> android.view.KeyEvent.KEYCODE_F12
        else -> null
    }
}
