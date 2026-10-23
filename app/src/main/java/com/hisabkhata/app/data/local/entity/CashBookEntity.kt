package com.hisabkhata.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "cash_book_entries")
data class CashBookEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: String,          // "INCOME" (আয়/জমা) or "EXPENSE" (ব্যয়/খরচ)
    val amount: Double,
    val category: String,      // বিক্রয়, ঋণ গ্রহণ, কমিশন, দোকান ভাড়া, পাইকারি ক্রয়, কর্মচারীর বেতন, ইত্যাদি
    val paymentMethod: String = "নগদ", // নগদ, বিকাশ, নগদ (Nagad), ব্যাংক
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val currency: String = "BDT" // BDT (৳) or USD ($)
)
