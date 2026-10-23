package com.hisabkhata.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hisabkhata.app.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers WHERE isDeleted = 0 AND type = :type ORDER BY updatedAt DESC")
    fun getCustomersByTypeFlow(type: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllActiveCustomersFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerByIdFlow(id: String): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteCustomer(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM customers WHERE isDeleted = 0")
    fun getCustomerCountFlow(): Flow<Int>
}
