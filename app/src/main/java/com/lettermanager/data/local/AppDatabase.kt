package com.lettermanager.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.lettermanager.data.local.dao.FinancialReceiptDao
import com.lettermanager.data.local.dao.LetterDao
import com.lettermanager.data.local.dao.SystemCounterDao
import com.lettermanager.data.local.entity.FinancialReceiptEntity
import com.lettermanager.data.local.entity.LetterEntity
import com.lettermanager.data.local.entity.SystemCounterEntity

@Database(
    entities = [
        LetterEntity::class,
        SystemCounterEntity::class,
        FinancialReceiptEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun letterDao(): LetterDao
    abstract fun systemCounterDao(): SystemCounterDao
    abstract fun financialReceiptDao(): FinancialReceiptDao

    companion object {
        const val DATABASE_NAME = "letter_manager.db"
    }
}
