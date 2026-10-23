package com.hisabkhata.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hisabkhata.app.data.local.entity.FamilyExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyExpenseDao {

    @Query("SELECT * FROM family_expenses ORDER BY date DESC")
    fun getAllExpensesFlow(): Flow<List<FamilyExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: FamilyExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<FamilyExpenseEntity>)

    @Query("DELETE FROM family_expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)

    @Query("SELECT SUM(amount) FROM family_expenses")
    fun getTotalExpenseFlow(): Flow<Double?>
}
