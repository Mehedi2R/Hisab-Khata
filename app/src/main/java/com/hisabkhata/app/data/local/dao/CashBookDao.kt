package com.hisabkhata.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hisabkhata.app.data.local.entity.CashBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashBookDao {

    @Query("SELECT * FROM cash_book_entries ORDER BY date DESC")
    fun getAllEntriesFlow(): Flow<List<CashBookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CashBookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<CashBookEntity>)

    @Query("DELETE FROM cash_book_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("SELECT SUM(amount) FROM cash_book_entries WHERE type = 'INCOME'")
    fun getTotalIncomeFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM cash_book_entries WHERE type = 'EXPENSE'")
    fun getTotalExpenseFlow(): Flow<Double?>
}
