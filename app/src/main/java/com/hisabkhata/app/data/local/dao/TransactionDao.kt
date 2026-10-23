package com.hisabkhata.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hisabkhata.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class CustomerCurrencyBalanceRow(
    val customerId: String,
    val currency: String,
    val totalDebit: Double,
    val totalCredit: Double
)

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE customerId = :customerId AND isDeleted = 0 ORDER BY date DESC, createdAt DESC")
    fun getTransactionsForCustomerFlow(customerId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId AND isDeleted = 0 ORDER BY date DESC, createdAt DESC")
    suspend fun getTransactionsForCustomer(customerId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteTransaction(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt WHERE customerId = :customerId")
    suspend fun softDeleteTransactionsForCustomer(customerId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt WHERE customerId IN (SELECT id FROM customers WHERE isDeleted = 1)")
    suspend fun cleanOrphanedTransactions(updatedAt: Long = System.currentTimeMillis())

    @Query("""
        SELECT 
            t.customerId,
            COALESCE(t.currency, 'BDT') AS currency,
            COALESCE(SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), 0.0) AS totalDebit,
            COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0.0) AS totalCredit
        FROM transactions t
        INNER JOIN customers c ON t.customerId = c.id
        WHERE t.isDeleted = 0 AND c.isDeleted = 0
        GROUP BY t.customerId, t.currency
    """)
    fun getAllCustomerCurrencyBalancesFlow(): Flow<List<CustomerCurrencyBalanceRow>>

    @Query("""
        SELECT 
            t.customerId,
            COALESCE(t.currency, 'BDT') AS currency,
            COALESCE(SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), 0.0) AS totalDebit,
            COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0.0) AS totalCredit
        FROM transactions t
        INNER JOIN customers c ON t.customerId = c.id
        WHERE t.customerId = :customerId AND t.isDeleted = 0 AND c.isDeleted = 0
        GROUP BY t.customerId, t.currency
    """)
    fun getCustomerCurrencyBalancesFlow(customerId: String): Flow<List<CustomerCurrencyBalanceRow>>

    // Total today's credit (জমা) and debit (দিলাম/খরচ)
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0.0) 
        FROM transactions t
        INNER JOIN customers c ON t.customerId = c.id
        WHERE t.isDeleted = 0 AND c.isDeleted = 0 AND t.type = :type AND t.date >= :startOfDay AND t.date <= :endOfDay
    """)
    fun getTodayAmountByTypeFlow(type: String, startOfDay: Long, endOfDay: Long): Flow<Double>
}
