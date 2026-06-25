package io.github.modhack.model

/**
 * A word suggestion from the dictionary / suggestion engine.
 *
 * @property word The suggested word text.
 * @property frequency Frequency / ranking score (higher = more likely).
 * @property source Which dictionary source produced this suggestion.
 */
data class Suggestion(
    val word: String,
    val frequency: Int,
    val source: SuggestionSource
)

/**
 * Identifies the origin of a [Suggestion].
 */
enum class SuggestionSource {
    /** From the pre-built binary dictionary (Rust engine). */
    BINARY_DICTIONARY,
    /** From the user's personal dictionary (Room). */
    USER_DICTIONARY,
    /** From the auto-learned dictionary (Room). */
    AUTO_DICTIONARY,
    /** From a bigram prediction. */
    BIGRAM,
    /** From an external dictionary plugin. */
    PLUGIN
}
