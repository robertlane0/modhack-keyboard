package io.github.modhack.dictionary

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single word in the user or auto dictionary.
 */
@Entity(tableName = "unigrams")
data class Unigram(
    @PrimaryKey val word: String,
    val frequency: Int,
    val locale: String
)

/**
 * Represents a pair of consecutive words.
 */
@Entity(tableName = "bigrams", primaryKeys = ["word1", "word2"])
data class Bigram(
    val word1: String,
    val word2: String,
    val frequency: Int
)
