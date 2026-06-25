package io.github.modhack.dictionary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object for auto-learned words and bigrams.
 */
@Dao
interface AutoDictionaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutoWord(unigram: Unigram)

    @Query("SELECT * FROM unigrams WHERE word LIKE :prefix || '%' AND locale = :locale ORDER BY frequency DESC LIMIT :limit")
    suspend fun findAutoByPrefix(prefix: String, locale: String, limit: Int = 10): List<Unigram>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBigram(bigram: Bigram)

    @Query("SELECT * FROM bigrams WHERE word1 = :prevWord ORDER BY frequency DESC LIMIT :limit")
    suspend fun getBigrams(prevWord: String, limit: Int = 5): List<Bigram>
}
