package io.github.modhack.dictionary

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object for the user's personal dictionary.
 */
@Dao
interface UserDictionaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unigram: Unigram)

    @Query("SELECT * FROM unigrams WHERE word LIKE :prefix || '%' AND locale = :locale ORDER BY frequency DESC LIMIT :limit")
    suspend fun findByPrefix(prefix: String, locale: String, limit: Int = 10): List<Unigram>

    @Query("SELECT * FROM unigrams WHERE word = :word AND locale = :locale")
    suspend fun findExact(word: String, locale: String): Unigram?

    @Delete
    suspend fun delete(unigram: Unigram)

    @Query("UPDATE unigrams SET frequency = frequency + 1 WHERE word = :word AND locale = :locale")
    suspend fun incrementFrequency(word: String, locale: String)
}
