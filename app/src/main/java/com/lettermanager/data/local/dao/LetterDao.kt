package com.lettermanager.data.local.dao

import androidx.room.*
import com.lettermanager.data.local.entity.LetterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LetterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(letter: LetterEntity): Long

    @Update
    suspend fun update(letter: LetterEntity)

    @Delete
    suspend fun delete(letter: LetterEntity)

    @Query("SELECT * FROM letters WHERE status = 'active' ORDER BY deadlineMiladi ASC")
    fun getActiveLetters(): Flow<List<LetterEntity>>

    @Query("SELECT * FROM letters WHERE status = 'archived' ORDER BY letterDateMiladi DESC")
    fun getArchivedLetters(): Flow<List<LetterEntity>>

    @Query("SELECT * FROM letters WHERE id = :id")
    suspend fun getLetterById(id: Int): LetterEntity?

    @Query("SELECT * FROM letters WHERE id = :id")
    fun getLetterByIdFlow(id: Int): Flow<LetterEntity?>

    @Query("""
        SELECT * FROM letters WHERE 
        (:query = '' OR autoNumber LIKE '%' || :query || '%') OR
        (:query = '' OR manualLetterNumber LIKE '%' || :query || '%') OR
        (:query = '' OR autoResponseNumber LIKE '%' || :query || '%') OR
        (:query = '' OR manualResponseNumber LIKE '%' || :query || '%') OR
        (:query = '' OR applicantName LIKE '%' || :query || '%') OR
        (:query = '' OR sender LIKE '%' || :query || '%')
        ORDER BY letterDateMiladi DESC
    """)
    fun searchLetters(query: String): Flow<List<LetterEntity>>

    @Query("""
        SELECT * FROM letters WHERE 
        (:status = '' OR status = :status) AND
        (:fromDate = 0 OR letterDateMiladi >= :fromDate) AND
        (:toDate = 0 OR letterDateMiladi <= :toDate) AND
        (
            :query = '' OR 
            autoNumber LIKE '%' || :query || '%' OR
            manualLetterNumber LIKE '%' || :query || '%' OR
            autoResponseNumber LIKE '%' || :query || '%' OR
            manualResponseNumber LIKE '%' || :query || '%' OR
            applicantName LIKE '%' || :query || '%' OR
            sender LIKE '%' || :query || '%'
        )
        ORDER BY letterDateMiladi DESC
    """)
    fun searchLettersFiltered(
        query: String,
        status: String,
        fromDate: Long,
        toDate: Long
    ): Flow<List<LetterEntity>>

    @Query("SELECT * FROM letters WHERE synced = 0")
    suspend fun getUnsyncedLetters(): List<LetterEntity>

    @Query("UPDATE letters SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)

    @Query("SELECT * FROM letters WHERE deadlineMiladi BETWEEN :from AND :to AND status = 'active'")
    suspend fun getLettersWithDeadlineBetween(from: Long, to: Long): List<LetterEntity>

    @Query("SELECT * FROM letters WHERE deadlineMiladi < :now AND status = 'active'")
    fun getOverdueLetters(now: Long): Flow<List<LetterEntity>>

    @Query("SELECT * FROM letters ORDER BY letterDateMiladi DESC")
    suspend fun getAllLettersDirect(): List<LetterEntity>
}
