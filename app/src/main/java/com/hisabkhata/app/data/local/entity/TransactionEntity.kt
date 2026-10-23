package com.hisabkhata.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["date"]),
        Index(value = ["syncStatus"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val type: String, // DEBIT (দিলাম / বাকি) or CREDIT (পেলাম / জমা)
    val amount: Double,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: String = SYNC_PENDING,
    val currency: String = CURRENCY_BDT // BDT (৳) or USD ($)
) {
    companion object {
        const val TYPE_DEBIT = "DEBIT"   // দিলাম (বাকি বৃদ্ধি)
        const val TYPE_CREDIT = "CREDIT" // পেলাম (বাকি হ্রাস / জমা)

        const val CURRENCY_BDT = "BDT"
        const val CURRENCY_USD = "USD"

        const val SYNC_PENDING = "PENDING"
        const val SYNC_SYNCED = "SYNCED"
    }
}
