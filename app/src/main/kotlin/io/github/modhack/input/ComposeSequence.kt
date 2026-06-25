package io.github.modhack.input

/**
 * X11 compose key sequence engine.
 *
 * Implements the standard X11 compose key sequences for producing
 * accented and special characters. When the user presses the Compose
 * key followed by a two-character sequence, this engine resolves
 * the resulting Unicode character.
 *
 * Example sequences:
 * - Compose + ' + e → é
 * - Compose + " + a → ä
 * - Compose + ~ + n → ñ
 * - Compose + ^ + o → ô
 *
 * The engine maintains state as a finite automaton:
 * [IDLE] → [FIRST_KEY] → [SECOND_KEY → result].
 *
 * @see DeadAccentSequence for dead key combining via Unicode normalization.
 */
class ComposeSequence {

    /**
     * The current state of the compose sequence automaton.
     */
    private var firstKey: Char? = null

    /**
     * Whether the compose key has been pressed and we are waiting
     * for the first character of the sequence.
     */
    val isActive: Boolean
        get() = firstKey != null

    /**
     * Starts a new compose sequence by recording the first key.
     *
     * @param key The first key of the compose sequence (e.g., `'`, `"`, `~`).
     * @return Always returns `null` since the first key never produces output.
     */
    fun onFirstKey(key: Char): Char? {
        firstKey = key
        return null
    }

    /**
     * Processes the second key of the compose sequence and returns
     * the resolved Unicode character, or `null` if the sequence is invalid.
     *
     * @param key The second key of the compose sequence.
     * @return The resolved character, or `null` if the sequence was not recognized.
     */
    fun onSecondKey(key: Char): Char? {
        val first = firstKey
        firstKey = null

        if (first == null) return null

        return lookup(first, key)
    }

    /**
     * Cancels the current compose sequence without producing output.
     */
    fun cancel() {
        firstKey = null
    }

    /**
     * Resolves a two-character compose sequence to a Unicode character.
     *
     * This mapping covers the most common X11 compose sequences.
     * The full X11 Compose file contains thousands of entries; this
     * is a practical subset for everyday use.
     *
     * @param first The first key (typically a punctuation mark like ', ", ~, ^).
     * @param second The second key (typically a base letter).
     * @return The resulting character, or `null` if the sequence is unknown.
     */
    private fun lookup(first: Char, second: Char): Char? {
        return composeTable[first]?.get(second)
    }

    /**
     * Compose sequence table mapping (first_key, second_key) → result.
     *
     * Organized by the first key for efficient lookup. Covers:
     * - Acute accent (')
     * - Diaeresis/Umlaut (")
     * - Grave accent (`)
     * - Tilde (~)
     * - Circumflex (^)
     * - Cedilla (,)
     * - Stroke (/)
     * - Ring (°)
     * - Caron (v)
     * - Ogonek (;\)
     * - Double acute (")
     * - Dot above (.)
     * - Special combinations
     */
    private val composeTable: Map<Char, Map<Char, Char>> = mapOf(
        // Acute accent (')
        '\'' to mapOf(
            'a' to '\u00e1', 'e' to '\u00e9', 'i' to '\u00ed',
            'o' to '\u00f3', 'u' to '\u00fa', 'y' to '\u00fd',
            'A' to '\u00c1', 'E' to '\u00c9', 'I' to '\u00cd',
            'O' to '\u00d3', 'U' to '\u00da', 'Y' to '\u00dd',
            'c' to '\u0107', 'C' to '\u0106',
            'n' to '\u0144', 'N' to '\u0143',
            'l' to '\u013a', 'L' to '\u0139',
            'r' to '\u0155', 'R' to '\u0154',
            's' to '\u015b', 'S' to '\u015a',
            'z' to '\u017a', 'Z' to '\u0179'
        ),

        // Diaeresis / Umlaut (")
        '"' to mapOf(
            'a' to '\u00e4', 'e' to '\u00e8', 'i' to '\u00ef',
            'o' to '\u00f6', 'u' to '\u00fc', 'y' to '\u00ff',
            'A' to '\u00c4', 'E' to '\u00c8', 'I' to '\u00cf',
            'O' to '\u00d6', 'U' to '\u00dc', 'Y' to '\u0178'
        ),

        // Grave accent (`)
        '`' to mapOf(
            'a' to '\u00e0', 'e' to '\u00e8', 'i' to '\u00ec',
            'o' to '\u00f2', 'u' to '\u00f9',
            'A' to '\u00c0', 'E' to '\u00c8', 'I' to '\u00cc',
            'O' to '\u00d2', 'U' to '\u00d9'
        ),

        // Tilde (~)
        '~' to mapOf(
            'a' to '\u00e3', 'n' to '\u00f1', 'o' to '\u00f5',
            'A' to '\u00c3', 'N' to '\u00d1', 'O' to '\u00d5'
        ),

        // Circumflex (^)
        '^' to mapOf(
            'a' to '\u00e2', 'e' to '\u00ea', 'i' to '\u00ee',
            'o' to '\u00f4', 'u' to '\u00fb',
            'A' to '\u00c2', 'E' to '\u00ca', 'I' to '\u00ce',
            'O' to '\u00d4', 'U' to '\u00db'
        ),

        // Cedilla (,)
        ',' to mapOf(
            'c' to '\u00e7', 'C' to '\u00c7',
            'g' to '\u0123', 'G' to '\u0122',
            'k' to '\u0137', 'K' to '\u0136',
            'l' to '\u013c', 'L' to '\u013b',
            'n' to '\u0146', 'N' to '\u0145',
            'r' to '\u0157', 'R' to '\u0156',
            's' to '\u015f', 'S' to '\u015e',
            't' to '\u0163', 'T' to '\u0162'
        ),

        // Stroke (/)
        '/' to mapOf(
            'o' to '\u00f8', 'O' to '\u00d8',
            'l' to '\u0142', 'L' to '\u0141',
            'd' to '\u0111', 'D' to '\u0110'
        ),

        // Ring above (°)
        '°' to mapOf(
            'a' to '\u00e5', 'A' to '\u00c5',
            'u' to '\u016f', 'U' to '\u016e'
        ),

        // Caron / háček (v)
        'v' to mapOf(
            'c' to '\u010d', 'C' to '\u010c',
            'd' to '\u010f', 'D' to '\u010e',
            'e' to '\u011b', 'E' to '\u011a',
            'l' to '\u013e', 'L' to '\u013d',
            'n' to '\u0148', 'N' to '\u0147',
            'r' to '\u0159', 'R' to '\u0158',
            's' to '\u0161', 'S' to '\u0160',
            'z' to '\u017e', 'Z' to '\u017d'
        ),

        // Ogonek (;)
        ';' to mapOf(
            'a' to '\u0105', 'A' to '\u0104',
            'e' to '\u0119', 'E' to '\u0118',
            'i' to '\u012f', 'I' to '\u012e',
            'o' to '\u014d', 'O' to '\u014c',
            'u' to '\u0173', 'U' to '\u0172'
        ),

        // Double acute (˝)
        '˝' to mapOf(
            'o' to '\u0151', 'O' to '\u0150',
            'u' to '\u0171', 'U' to '\u0170'
        ),

        // Dot above (.)
        '.' to mapOf(
            'c' to '\u010b', 'C' to '\u010a',
            'e' to '\u0117', 'E' to '\u0116',
            'g' to '\u0121', 'G' to '\u0120',
            'z' to '\u017c', 'Z' to '\u017b'
        ),

        // Macron (¯)
        '¯' to mapOf(
            'a' to '\u0101', 'A' to '\u0100',
            'e' to '\u0113', 'E' to '\u0112',
            'i' to '\u012b', 'I' to '\u012a',
            'o' to '\u014d', 'O' to '\u014c',
            'u' to '\u016b', 'U' to '\u016a'
        ),

        // Special combinations for common symbols
        '<' to mapOf(
            '<' to '\u00ab',  // «
            '3' to '\u2665'   // ♥
        ),
        '>' to mapOf(
            '>' to '\u00bb',  // »
            '3' to '\u2666'   // ♦
        ),
        'o' to mapOf(
            'o' to '\u2665'   // ♥ (oo → heart)
        ),
        '-' to mapOf(
            '-' to '\u2014',  // —
            'd' to '\u2013'   // –
        ),
        '.' to mapOf(
            '.' to '\u2026'   // …
        ),
        '!' to mapOf(
            '!' to '\u00a1',  // ¡
            '?' to '\u00bf'   // ¿
        ),
        '?' to mapOf(
            '!' to '\u00bf',  // ¿
            '?' to '\u00a1'   // ¡
        ),
        't' to mapOf(
            'm' to '\u2122'   // ™
        ),
        'c' to mapOf(
            'o' to '\u00a9',  // ©
            'r' to '\u00ae'   // ®
        ),
        'R' to mapOf(
            'S' to '\u00ae'   // ®
        )
    )
}
