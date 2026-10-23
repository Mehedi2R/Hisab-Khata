package com.hisabkhata.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "family_expenses")
data class FamilyExpenseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val category: String,      // বাজার/মুদি, বাসা ভাড়া, বিদ্যুৎ/গ্যাস বিল, চিকিৎসা, শিক্ষা, যাতায়াত, অন্যান্য
    val amount: Double,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
