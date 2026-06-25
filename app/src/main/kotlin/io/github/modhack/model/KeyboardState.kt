package io.github.modhack.model

/**
 * Represents the current state of the keyboard.
 *
 * This is the primary state object observed by the Compose UI layer.
 * It follows unidirectional data flow: the service and input processing
 * layers produce new [KeyboardState] instances, which flow down to the UI.
 *
 * @property layoutId Identifier of the currently active [KeyboardLayout].
 * @property mode Current input mode (see [InputMode]).
 * @property shiftState Current shift state.
 * @property activeModifiers Set of currently pressed/locked modifiers.
 * @property isComposing Whether the user is currently composing a word.
 * @property isCapsLock Whether caps lock is engaged (double-tap shift).
 */
data class KeyboardState(
    val layoutId: String = "",
    val mode: InputMode = InputMode.TEXT,
    val shiftState: ShiftState = ShiftState.OFF,
    val activeModifiers: Set<Modifier> = emptySet(),
    val isComposing: Boolean = false,
    val isCapsLock: Boolean = false
)

/**
 * Input mode determines which layout variant is displayed.
 *
 * @see KeyboardLayout.mode
 */
enum class InputMode {
    /** Standard text input (QWERTY / locale-specific alpha). */
    TEXT,
    /** Symbols and punctuation layout. */
    SYMBOLS,
    /** Phone dial pad layout. */
    PHONE,
    /** Optimized for URL input. */
    URL,
    /** Optimized for email input. */
    EMAIL,
    /** Optimized for instant messaging. */
    IM,
    /** Optimized for web search. */
    WEB
}

/**
 * Shift key state machine.
 */
enum class ShiftState {
    /** Shift is not active — lowercase. */
    OFF,
    /** Shift is active for one character — next key will be uppercase. */
    ON,
    /** Caps lock — all keys uppercase until toggled off. */
    LOCKED
}
