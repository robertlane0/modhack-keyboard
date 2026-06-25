package io.github.modhack.input

import java.text.Normalizer

/**
 * Dead key / accent combining engine.
 *
 * Implements dead key behavior where pressing a dead accent mark
 * (e.g., `, ^, ~, ¨, ´) does not immediately produce output.
 * Instead, it "waits" for the next keypress. If the next key is a
 * combining-compatible base character, the engine produces the
 * pre-composed Unicode character using NFC normalization.
 *
 * Example behavior:
 * - Press ^ (dead circumflex), then a → â (U+00E2)
 * - Press ~ (dead tilde), then n → ñ (U+00F1)
 * - Press ¨ (dead diaeresis), then a → ä (U+00E4)
 * - Press ^ (dead circumflex), then Space → ^ (fallback)
 *
 * This is distinct from [ComposeSequence] which uses explicit two-key
 * sequences. Dead keys are a more traditional input method where the
 * first key is always an accent mark.
 *
 * @see ComposeSequence for X11-style compose key sequences.
 */
class DeadAccentSequence {

    /**
     * The pending dead key accent, or `null` if no dead key is active.
     */
    private var pendingDeadKey: DeadKey? = null

    /**
     * Whether a dead key is currently active and awaiting a base character.
     */
    val isActive: Boolean
        get() = pendingDeadKey != null

    /**
     * The currently pending dead key, exposed for UI display.
     */
    val currentDeadKey: DeadKey?
        get() = pendingDeadKey

    /**
     * Presses a dead key, activating the combining state.
     *
     * @param deadKey The dead key being pressed.
     * @return Always returns `null` since dead keys never produce immediate output.
     */
    fun pressDeadKey(deadKey: DeadKey): Char? {
        pendingDeadKey = deadKey
        return null
    }

    /**
     * Processes the next character after a dead key has been pressed.
     *
     * If the character is a valid base character for combining, returns
     * the pre-composed NFC character. If the character cannot be combined
     * (e.g., another accent, a digit, or a space), returns the dead key
     * character itself followed by the new character.
     *
     * @param baseChar The character pressed after the dead key.
     * @return The combined character, or the dead key + base if combining fails.
     */
    fun onBaseKey(baseChar: Char): String {
        val deadKey = pendingDeadKey
        pendingDeadKey = null

        if (deadKey == null) {
            return baseChar.toString()
        }

        // Try to combine the dead key accent with the base character
        val combined = combineNFC(deadKey.combiningChar, baseChar)
        return if (combined != null) {
            combined.toString()
        } else {
            // Fallback: output the dead key's literal character + the base
            deadKey.literal.toString() + baseChar
        }
    }

    /**
     * Cancels the current dead key state and returns the dead key's literal character.
     *
     * @return The literal character of the pending dead key, or `null` if none.
     */
    fun cancel(): Char? {
        val deadKey = pendingDeadKey
        pendingDeadKey = null
        return deadKey?.literal
    }

    /**
     * Combines a Unicode combining character with a base character via NFC normalization.
     *
     * @param combiningChar The Unicode combining character (e.g., "\u0302" for circumflex).
     * @param baseChar The base character to combine with.
     * @return The pre-composed character, or `null` if NFC normalization did not produce one.
     */
    private fun combineNFC(combiningChar: String, baseChar: Char): Char? {
        val decomposed = baseChar.toString() + combiningChar
        val normalized = Normalizer.normalize(decomposed, Normalizer.Form.NFC)

        // NFC should produce a single pre-composed character
        if (normalized.length == 1 && normalized[0] != baseChar) {
            return normalized[0]
        }
        return null
    }

    /**
     * All supported dead keys with their combining characters and literals.
     *
     * @property literal The character the dead key produces if not combined.
     * @property combiningChar The Unicode combining character used for NFC.
     * @property label Human-readable label for the dead key.
     */
    enum class DeadKey(
        val literal: Char,
        val combiningChar: String,
        val label: String
    ) {
        /** Dead acute accent: ´ (e.g., ´ + e → é) */
        ACUTE('\'', "\u0301", "Dead Acute"),

        /** Dead grave accent: ` (e.g., ` + e → è) */
        GRAVE('`', "\u0300", "Dead Grave"),

        /** Dead circumflex: ^ (e.g., ^ + a → â) */
        CIRCUMFLEX('^', "\u0302", "Dead Circumflex"),

        /** Dead tilde: ~ (e.g., ~ + n → ñ) */
        TILDE('~', "\u0303", "Dead Tilde"),

        /** Dead diaeresis / umlaut: ¨ (e.g., ¨ + a → ä) */
        DIAERESIS('\u00a8', "\u0308", "Dead Diaeresis"),

        /** Dead cedilla: ¸ (e.g., ¸ + c → ç) */
        CEDILLA('\u00b8', "\u0327", "Dead Cedilla"),

        /** Dead caron / háček: ˇ (e.g., ˇ + c → č) */
        CARON('\u02c7', "\u030c", "Dead Caron"),

        /** Dead ogonek: ˛ (e.g., ˛ + a → ą) */
        OGONEK('\u02db', "\u0328", "Dead Ogonek"),

        /** Dead stroke: / (e.g., / + o → ø) */
        STROKE('/', "\u0338", "Dead Stroke");

        companion object {
            /**
             * Returns the [DeadKey] for the given literal character, or `null`.
             */
            fun fromChar(char: Char): DeadKey? = entries.find { it.literal == char }
        }
    }
}
