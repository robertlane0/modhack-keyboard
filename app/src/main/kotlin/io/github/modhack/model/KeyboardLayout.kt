package io.github.modhack.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * An immutable representation of a complete keyboard layout.
 *
 * Replaces the mutable `Keyboard.java` from AOSP with a
 * Kotlin data class that is safe for concurrent access.
 *
 * @property id Unique identifier for this layout (e.g., "qwerty_en_US_portrait").
 * @property rows Ordered list of key rows from top to bottom.
 * @property width Total layout width in abstract units.
 * @property height Total layout height in abstract units.
 * @property mode The input mode this layout is designed for (e.g., TEXT, SYMBOLS).
 *
 * @see Row
 * @see Key
 */
data class KeyboardLayout(
    val id: String,
    val rows: List<Row>,
    val width: Int,
    val height: Int,
    val mode: Int
)

/**
 * A single row of keys within a [KeyboardLayout].
 *
 * @property keys Ordered list of keys from left to right.
 * @property defaultHeight Default key height for this row (can be overridden per key).
 * @property defaultWidth Default key width for this row (can be overridden per key).
 * @property mode Row-specific mode flag (0 if the row is always visible).
 * @property isExtension Whether this row is an extension row (e.g., the top Fn/F-key row).
 */
data class Row(
    val keys: List<Key>,
    val defaultHeight: Int,
    val defaultWidth: Int,
    val mode: Int = 0,
    val isExtension: Boolean = false
)

/**
 * A single key within a [Row].
 *
 * @property codes Primary key code at index 0, followed by nearby proximity codes.
 * @property label Display label for the unshifted state.
 * @property shiftLabel Display label for the shifted state.
 * @property hint Optional hint text displayed in a corner (e.g., a number on an alpha key).
 * @property altHint Optional alternative hint for a secondary action.
 * @property icon Optional vector icon to display instead of a text label (e.g., backspace arrow).
 * @property width Key width in abstract units.
 * @property height Key height in abstract units.
 * @property x Horizontal position of the key's left edge.
 * @property y Vertical position of the key's top edge.
 * @property isModifier Whether this key is a modifier (Shift, Ctrl, Alt, Meta, Fn).
 * @property isRepeatable Whether this key supports repeat-on-hold (e.g., backspace, arrows).
 * @property isCursor Whether this key is a cursor/navigation key.
 * @property popupKeys Optional list of popup characters for long-press (e.g., accented variants).
 */
data class Key(
    val codes: List<Int>,
    val label: String,
    val shiftLabel: String = "",
    val hint: String? = null,
    val altHint: String? = null,
    val icon: ImageVector? = null,
    val width: Int,
    val height: Int,
    val x: Int = 0,
    val y: Int = 0,
    val isModifier: Boolean = false,
    val isRepeatable: Boolean = false,
    val isCursor: Boolean = false,
    val popupKeys: List<String>? = null
)
