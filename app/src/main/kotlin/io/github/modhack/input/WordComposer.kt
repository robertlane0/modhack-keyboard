package io.github.modhack.input

/**
 * Manages the composition of a word currently being typed.
 *
 * This class accumulates character codes. It uses a data class copy semantic
 * to safely provide immutable snapshots if needed, although it internally
 * mutates a string builder for performance.
 */
class WordComposer {
    private val codes = mutableListOf<Int>()
    private var composedWord = StringBuilder()

    /**
     * Appends the given character [code] to the composing word.
     */
    fun addKeyCode(code: Int) {
        codes.add(code)
        composedWord.append(code.toChar())
    }

    /**
     * Deletes the last character from the composing word.
     */
    fun deleteLastCode() {
        if (codes.isNotEmpty()) {
            codes.removeAt(codes.size - 1)
            composedWord.deleteCharAt(composedWord.length - 1)
        }
    }

    /**
     * Returns the currently composed word as a string.
     */
    fun getTypedWord(): String {
        return composedWord.toString()
    }

    /**
     * Resets the composer, clearing all characters.
     */
    fun reset() {
        codes.clear()
        composedWord.clear()
    }

    /**
     * Returns true if there are no characters currently being composed.
     */
    fun isEmpty(): Boolean {
        return codes.isEmpty()
    }

    /**
     * The number of characters currently composed.
     */
    val size: Int
        get() = codes.size
}
