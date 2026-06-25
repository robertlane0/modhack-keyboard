package io.github.modhack.dictionary

import io.github.modhack.model.Suggestion
import io.github.modhack.model.SuggestionSource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Aggregates suggestions from binary, user, and auto dictionaries.
 *
 * Merges results from multiple sources, deduplicates by word,
 * and returns a ranked list of suggestions. Supports bigram context
 * to improve prediction accuracy when a previous word is available.
 *
 * @property binaryDictionary The native or fallback binary dictionary.
 * @property userDictionaryDao DAO for user-added words.
 * @property autoDictionaryDao DAO for auto-learned words and bigrams.
 */
class SuggestionEngine(
    private val binaryDictionary: BinaryDictionary,
    private val userDictionaryDao: UserDictionaryDao,
    private val autoDictionaryDao: AutoDictionaryDao
) {
    /**
     * Gets suggestions for the currently composed word.
     *
     * When [prevWord] is provided, bigram predictions are included
     * and weighted higher than unigram-only matches.
     *
     * @param composedWord The partial word typed so far.
     * @param prevWord The word typed immediately before, or null.
     * @param locale The current input locale (e.g., "en").
     * @return A [Flow] emitting the current list of suggestions.
     */
    fun getSuggestions(composedWord: String, prevWord: String?, locale: String): Flow<List<Suggestion>> = flow {
        if (composedWord.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val suggestions = coroutineScope {
            val binaryDef = async { binaryDictionary.getSuggestions(composedWord) }
            val userDef = async { userDictionaryDao.findByPrefix(composedWord, locale) }
            val autoDef = async { autoDictionaryDao.findAutoByPrefix(composedWord, locale) }

            val bigramDef = if (!prevWord.isNullOrEmpty()) {
                async {
                    val bigrams = autoDictionaryDao.getBigrams(prevWord)
                    bigrams.filter { it.word2.startsWith(composedWord.lowercase()) }
                        .map { Suggestion(it.word2, it.frequency * 2, SuggestionSource.BIGRAM) }
                }
            } else {
                null
            }

            val binaryRes = binaryDef.await()
            val userRes = userDef.await().map { Suggestion(it.word, it.frequency, SuggestionSource.USER_DICTIONARY) }
            val autoRes = autoDef.await().map { Suggestion(it.word, it.frequency, SuggestionSource.AUTO_DICTIONARY) }
            val bigramRes = bigramDef?.await() ?: emptyList()

            val all = binaryRes + userRes + autoRes + bigramRes

            all.groupBy { it.word.lowercase() }
                .map { (_, list) ->
                    val best = list.maxByOrNull { it.frequency }!!
                    val totalFrequency = list.sumOf { it.frequency }
                    best.copy(frequency = totalFrequency)
                }
                .sortedByDescending { it.frequency }
                .take(10)
        }
        emit(suggestions)
    }

    /**
     * Learns a word or increments its frequency.
     *
     * If the word already exists in the user dictionary, its frequency
     * is incremented. Otherwise, it is inserted into the auto dictionary
     * with an initial frequency of 1.
     *
     * @param word The word to learn.
     * @param locale The locale this word belongs to.
     */
    suspend fun learnWord(word: String, locale: String) {
        val existing = userDictionaryDao.findExact(word, locale)
        if (existing != null) {
            userDictionaryDao.incrementFrequency(word, locale)
        } else {
            autoDictionaryDao.insertAutoWord(Unigram(word, 1, locale))
        }
    }

    /**
     * Learns a bigram (pair of consecutive words).
     *
     * If the bigram already exists, its frequency is incremented via
     * a replace-on-conflict insert.
     *
     * @param word1 The first word in the pair.
     * @param word2 The second word in the pair.
     */
    suspend fun learnBigram(word1: String, word2: String) {
        val existing = autoDictionaryDao.getBigrams(word1)
            .find { it.word2 == word2 }
        if (existing != null) {
            autoDictionaryDao.insertBigram(Bigram(word1, word2, existing.frequency + 1))
        } else {
            autoDictionaryDao.insertBigram(Bigram(word1, word2, 1))
        }
    }
}
