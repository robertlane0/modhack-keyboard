package io.github.modhack.dictionary

import android.content.Context
import io.github.modhack.model.Suggestion
import io.github.modhack.model.SuggestionSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JNI bridge for the Rust native dictionary, with a pure Kotlin fallback.
 *
 * When the native library (`libmhdict.so`) is available, it delegates to
 * the Rust engine for fast binary dictionary lookups. Otherwise, it falls
 * back to loading a plain-text word list bundled as an asset.
 *
 * The native library can be loaded by calling [loadNative] or by ensuring
 * the `.so` is packaged in the APK's `lib/` directory.
 */
class BinaryDictionary(private val context: Context? = null) {

    private var nativeLoaded = false
    private var fallbackWords: List<String> = emptyList()
    private var fallbackBigrams: Map<String, List<String>> = emptyMap()

    /**
     * Attempts to load the native Rust library.
     *
     * @return `true` if the native library was loaded successfully.
     */
    fun loadNative(): Boolean {
        return try {
            System.loadLibrary("mhdict")
            nativeLoaded = true
            true
        } catch (_: UnsatisfiedLinkError) {
            nativeLoaded = false
            false
        }
    }

    /**
     * Loads a plain-text word list from assets as a fallback dictionary.
     *
     * Expected format: one word per line, optionally followed by a tab
     * and an integer frequency score.
     *
     * @param assetPath Path to the word list in assets (e.g., "dictionaries/en_words.txt").
     */
    suspend fun loadFallback(assetPath: String = "dictionaries/en_words.txt") = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext
        try {
            val words = mutableListOf<String>()

            ctx.assets.open(assetPath).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        val parts = trimmed.split("\t")
                        val word = parts[0].lowercase()
                        words.add(word)
                    }
                }
            }

            fallbackWords = words.sorted()
        } catch (_: Exception) {
            fallbackWords = emptyList()
        }
    }

    /**
     * Returns `true` if the native library or fallback data is loaded.
     */
    fun isLoaded(): Boolean {
        return nativeLoaded || fallbackWords.isNotEmpty()
    }

    /**
     * Gets word suggestions for the currently composed word.
     *
     * @param composedWord The partial word typed so far.
     * @return A list of matching [Suggestion]s sorted by relevance.
     */
    suspend fun getSuggestions(composedWord: String): List<Suggestion> {
        if (composedWord.isEmpty()) return emptyList()

        if (nativeLoaded) {
            return try {
                nativeGetSuggestions(composedWord)
            } catch (_: Exception) {
                emptyList()
            }
        }

        val prefix = composedWord.lowercase()
        return withContext(Dispatchers.IO) {
            fallbackWords
                .filter { it.startsWith(prefix) && it != prefix }
                .take(10)
                .map { Suggestion(it, it.length * 10, SuggestionSource.BINARY_DICTIONARY) }
        }
    }

    /**
     * Gets bigram predictions based on the previous word.
     *
     * @param prevWord The previously committed word.
     * @return A list of likely following words as [Suggestion]s.
     */
    suspend fun getBigrams(prevWord: String): List<Suggestion> {
        if (prevWord.isEmpty()) return emptyList()

        if (nativeLoaded) {
            return try {
                nativeGetBigrams(prevWord)
            } catch (_: Exception) {
                emptyList()
            }
        }

        return withContext(Dispatchers.IO) {
            fallbackBigrams[prevWord.lowercase()]
                ?.take(5)
                ?.map { Suggestion(it, 100, SuggestionSource.BIGRAM) }
                ?: emptyList()
        }
    }

    /**
     * Native JNI call to the Rust dictionary engine for word suggestions.
     */
    private external fun nativeGetSuggestions(composedWord: String): List<Suggestion>

    /**
     * Native JNI call to the Rust dictionary engine for bigram predictions.
     */
    private external fun nativeGetBigrams(prevWord: String): List<Suggestion>
}
