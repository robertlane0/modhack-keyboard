package io.github.modhack.dictionary

import io.github.modhack.model.Suggestion

/**
 * Stub JNI bridge for the Rust native dictionary.
 * Currently returns empty results.
 */
class BinaryDictionary {
    /*
    init {
        System.loadLibrary("mhdict")
    }
    */

    suspend fun getSuggestions(composedWord: String): List<Suggestion> {
        return emptyList() // Stub
    }

    suspend fun getBigrams(prevWord: String): List<Suggestion> {
        return emptyList() // Stub
    }

    fun isLoaded(): Boolean {
        return false // Stub
    }
}
