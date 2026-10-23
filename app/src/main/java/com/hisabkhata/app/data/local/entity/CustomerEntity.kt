package com.hisabkhata.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String = "",
    val address: String = "",
    val type: String = TYPE_CUSTOMER, // CUSTOMER or SUPPLIER
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: String = SYNC_PENDING
) {
    companion object {
        const val TYPE_CUSTOMER = "CUSTOMER"
        const val TYPE_SUPPLIER = "SUPPLIER"

        const val SYNC_PENDING = "PENDING"
        const val SYNC_SYNCED = "SYNCED"
    }
}
