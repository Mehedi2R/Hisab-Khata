package com.hisabkhata.app.data.local.entity

data class CustomerWithBalance(
    val customer: CustomerEntity,
    val totalDebitBdt: Double = 0.0,
    val totalCreditBdt: Double = 0.0,
    val totalDebitUsd: Double = 0.0,
    val totalCreditUsd: Double = 0.0
) {
    // Net balance: positive means customer owes merchant (পাবো), negative means merchant owes customer (দেবো)
    val netBalanceBdt: Double
        get() = totalDebitBdt - totalCreditBdt

    val netBalanceUsd: Double
        get() = totalDebitUsd - totalCreditUsd

    // BDT primary alias for backward UI compatibility
    val netBalance: Double
        get() = netBalanceBdt

    val hasUsd: Boolean
        get() = kotlin.math.abs(totalDebitUsd) > 0.001 || kotlin.math.abs(totalCreditUsd) > 0.001

    val isSettled: Boolean
        get() = kotlin.math.abs(netBalanceBdt) < 0.001 && (!hasUsd || kotlin.math.abs(netBalanceUsd) < 0.001)
}
