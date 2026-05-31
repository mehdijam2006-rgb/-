package com.lettermanager.data.local.dao

import androidx.room.*
import com.lettermanager.data.local.entity.SystemCounterEntity

@Dao
interface SystemCounterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(counter: SystemCounterEntity)

    @Query("SELECT * FROM systemCounters WHERE year = :year")
    suspend fun getCounter(year: Int): SystemCounterEntity?

    @Query("UPDATE systemCounters SET currentNumber = :number WHERE year = :year")
    suspend fun updateCounter(year: Int, number: Int)
}
