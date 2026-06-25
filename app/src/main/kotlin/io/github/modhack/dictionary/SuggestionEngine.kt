package io.github.modhack.dictionary

import io.github.modhack.model.Suggestion
import io.github.modhack.model.SuggestionSource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Aggregates suggestions from binary, user, and auto dictionaries.
 */
class SuggestionEngine(
    private val binaryDictionary: BinaryDictionary,
    private val userDictionaryDao: UserDictionaryDao,
    private val autoDictionaryDao: AutoDictionaryDao
) {
    /**
     * Gets suggestions for the currently composed word.
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
            
            val binaryRes = binaryDef.await()
            val userRes = userDef.await().map { Suggestion(it.word, it.frequency, SuggestionSource.USER_DICTIONARY) }
            val autoRes = autoDef.await().map { Suggestion(it.word, it.frequency, SuggestionSource.AUTO_DICTIONARY) }
            
            val all = binaryRes + userRes + autoRes
            
            // Deduplicate and sort
            all.groupBy { it.word }
                .map { (_, list) -> list.maxByOrNull { it.frequency }!! }
                .sortedByDescending { it.frequency }
                .take(10)
        }
        emit(suggestions)
    }

    /**
     * Learns a word or increments its frequency.
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
     * Learns a bigram.
     */
    suspend fun learnBigram(word1: String, word2: String) {
        autoDictionaryDao.insertBigram(Bigram(word1, word2, 1))
    }
}
