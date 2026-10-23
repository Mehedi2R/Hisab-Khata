package com.hisabkhata.app.data.repository

import android.content.Context
import com.hisabkhata.app.data.local.AppDatabase
import com.hisabkhata.app.data.local.entity.CashBookEntity
import com.hisabkhata.app.data.local.entity.CustomerEntity
import com.hisabkhata.app.data.local.entity.CustomerWithBalance
import com.hisabkhata.app.data.local.entity.FamilyExpenseEntity
import com.hisabkhata.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.UUID

data class DashboardSummary(
    val totalReceivableBdt: Double = 0.0, // মোট পাবো BDT
    val totalPayableBdt: Double = 0.0,    // মোট দেবো BDT
    val totalReceivableUsd: Double = 0.0, // মোট পাবো USD
    val totalPayableUsd: Double = 0.0,    // মোট দেবো USD
    val todayCashIn: Double = 0.0,        // আজকের মোট জমা
    val todayCashOut: Double = 0.0        // আজকের মোট খরচ / বাকি দেওয়া
) {
    // Backward compatibility aliases
    val totalReceivable: Double get() = totalReceivableBdt
    val totalPayable: Double get() = totalPayableBdt
    val hasUsd: Boolean get() = kotlin.math.abs(totalReceivableUsd) > 0.001 || kotlin.math.abs(totalPayableUsd) > 0.001
}

class HisabRepository(
    context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context.applicationContext)
) {
    private val customerDao = database.customerDao()
    private val transactionDao = database.transactionDao()
    private val familyExpenseDao = database.familyExpenseDao()
    private val cashBookDao = database.cashBookDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                transactionDao.cleanOrphanedTransactions()
            } catch (_: Exception) {}
        }
    }

    fun getCustomersWithBalanceFlow(type: String): Flow<List<CustomerWithBalance>> {
        val customersFlow = customerDao.getCustomersByTypeFlow(type)
        val balancesFlow = transactionDao.getAllCustomerCurrencyBalancesFlow()

        return combine(customersFlow, balancesFlow) { customers, balances ->
            val balanceMap = mutableMapOf<String, MutableMap<String, Pair<Double, Double>>>()
            for (row in balances) {
                val currMap = balanceMap.getOrPut(row.customerId) { mutableMapOf() }
                currMap[row.currency.uppercase()] = Pair(row.totalDebit, row.totalCredit)
            }

            customers.map { customer ->
                val currMap = balanceMap[customer.id]
                val bdt = currMap?.get("BDT") ?: Pair(0.0, 0.0)
                val usd = currMap?.get("USD") ?: Pair(0.0, 0.0)

                CustomerWithBalance(
                    customer = customer,
                    totalDebitBdt = bdt.first,
                    totalCreditBdt = bdt.second,
                    totalDebitUsd = usd.first,
                    totalCreditUsd = usd.second
                )
            }
        }
    }

    fun getCustomerByIdFlow(customerId: String): Flow<CustomerEntity?> {
        return customerDao.getCustomerByIdFlow(customerId)
    }

    fun getTransactionsForCustomerFlow(customerId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForCustomerFlow(customerId)
    }

    suspend fun getTransactionsForCustomer(customerId: String): List<TransactionEntity> {
        return transactionDao.getTransactionsForCustomer(customerId)
    }

    fun getCustomerBalanceFlow(customerId: String): Flow<CustomerWithBalance?> {
        val customerFlow = customerDao.getCustomerByIdFlow(customerId)
        val balanceFlow = transactionDao.getCustomerCurrencyBalancesFlow(customerId)

        return combine(customerFlow, balanceFlow) { customer, currencyRows ->
            if (customer == null) null
            else {
                var debitBdt = 0.0
                var creditBdt = 0.0
                var debitUsd = 0.0
                var creditUsd = 0.0

                for (row in currencyRows) {
                    if (row.currency.equals("USD", ignoreCase = true)) {
                        debitUsd += row.totalDebit
                        creditUsd += row.totalCredit
                    } else {
                        debitBdt += row.totalDebit
                        creditBdt += row.totalCredit
                    }
                }

                CustomerWithBalance(
                    customer = customer,
                    totalDebitBdt = debitBdt,
                    totalCreditBdt = creditBdt,
                    totalDebitUsd = debitUsd,
                    totalCreditUsd = creditUsd
                )
            }
        }
    }

    fun getDashboardSummaryFlow(): Flow<DashboardSummary> {
        val balancesFlow = transactionDao.getAllCustomerCurrencyBalancesFlow()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        val todayDebitFlow = transactionDao.getTodayAmountByTypeFlow(TransactionEntity.TYPE_DEBIT, startOfDay, endOfDay)
        val todayCreditFlow = transactionDao.getTodayAmountByTypeFlow(TransactionEntity.TYPE_CREDIT, startOfDay, endOfDay)

        return combine(balancesFlow, todayDebitFlow, todayCreditFlow) { balances, todayDebit, todayCredit ->
            var receivableBdt = 0.0
            var payableBdt = 0.0
            var receivableUsd = 0.0
            var payableUsd = 0.0

            for (row in balances) {
                val net = row.totalDebit - row.totalCredit
                if (row.currency.equals("USD", ignoreCase = true)) {
                    if (net > 0) {
                        receivableUsd += net
                    } else if (net < 0) {
                        payableUsd += -net
                    }
                } else {
                    if (net > 0) {
                        receivableBdt += net
                    } else if (net < 0) {
                        payableBdt += -net
                    }
                }
            }

            DashboardSummary(
                totalReceivableBdt = receivableBdt,
                totalPayableBdt = payableBdt,
                totalReceivableUsd = receivableUsd,
                totalPayableUsd = payableUsd,
                todayCashIn = todayCredit,
                todayCashOut = todayDebit
            )
        }
    }

    suspend fun addCustomer(
        name: String,
        phone: String = "",
        address: String = "",
        type: String = CustomerEntity.TYPE_CUSTOMER
    ): CustomerEntity {
        val customer = CustomerEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            phone = phone.trim(),
            address = address.trim(),
            type = type,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = CustomerEntity.SYNC_PENDING
        )
        customerDao.insertCustomer(customer)
        return customer
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        val updated = customer.copy(
            updatedAt = System.currentTimeMillis()
        )
        customerDao.updateCustomer(updated)
    }

    suspend fun deleteCustomer(customerId: String) {
        val now = System.currentTimeMillis()
        customerDao.softDeleteCustomer(customerId, now)
        transactionDao.softDeleteTransactionsForCustomer(customerId, now)
    }

    suspend fun addTransaction(
        customerId: String,
        type: String, // DEBIT (দিলাম / বাকি) or CREDIT (পেলাম / জমা)
        amount: Double,
        note: String = "",
        date: Long = System.currentTimeMillis(),
        currency: String = TransactionEntity.CURRENCY_BDT
    ): TransactionEntity {
        val tx = TransactionEntity(
            id = UUID.randomUUID().toString(),
            customerId = customerId,
            type = type,
            amount = amount,
            note = note.trim(),
            date = date,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            currency = currency
        )
        transactionDao.insertTransaction(tx)
        return tx
    }

    suspend fun updateTransaction(
        transactionId: String,
        customerId: String,
        type: String,
        amount: Double,
        note: String = "",
        date: Long = System.currentTimeMillis(),
        currency: String = TransactionEntity.CURRENCY_BDT
    ) {
        val tx = TransactionEntity(
            id = transactionId,
            customerId = customerId,
            type = type,
            amount = amount,
            note = note.trim(),
            date = date,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            currency = currency
        )
        transactionDao.updateTransaction(tx)
    }

    suspend fun deleteTransaction(transactionId: String) {
        transactionDao.softDeleteTransaction(transactionId)
    }

    // Family Expense Operations
    fun getAllFamilyExpensesFlow(): Flow<List<FamilyExpenseEntity>> = familyExpenseDao.getAllExpensesFlow()
    fun getTotalFamilyExpenseFlow(): Flow<Double?> = familyExpenseDao.getTotalExpenseFlow()

    suspend fun addFamilyExpense(
        category: String,
        amount: Double,
        note: String = "",
        date: Long = System.currentTimeMillis()
    ): FamilyExpenseEntity {
        val expense = FamilyExpenseEntity(
            id = UUID.randomUUID().toString(),
            category = category,
            amount = amount,
            note = note.trim(),
            date = date,
            createdAt = System.currentTimeMillis()
        )
        familyExpenseDao.insertExpense(expense)
        return expense
    }

    suspend fun updateFamilyExpense(
        id: String,
        amount: Double,
        note: String = "",
        date: Long = System.currentTimeMillis()
    ) {
        val expense = FamilyExpenseEntity(
            id = id,
            category = "খরচ",
            amount = amount,
            note = note.trim(),
            date = date,
            createdAt = System.currentTimeMillis()
        )
        familyExpenseDao.insertExpense(expense)
    }

    suspend fun deleteFamilyExpense(id: String) {
        familyExpenseDao.deleteExpense(id)
    }

    // Cash Book Operations (Income & Expense)
    fun getAllCashBookEntriesFlow(): Flow<List<CashBookEntity>> = cashBookDao.getAllEntriesFlow()
    fun getTotalCashIncomeFlow(): Flow<Double?> = cashBookDao.getTotalIncomeFlow()
    fun getTotalCashExpenseFlow(): Flow<Double?> = cashBookDao.getTotalExpenseFlow()

    suspend fun addCashBookEntry(
        type: String,
        amount: Double,
        category: String,
        paymentMethod: String = "নগদ",
        note: String = "",
        date: Long = System.currentTimeMillis(),
        currency: String = "BDT"
    ): CashBookEntity {
        val entry = CashBookEntity(
            id = UUID.randomUUID().toString(),
            type = type,
            amount = amount,
            category = category,
            paymentMethod = paymentMethod,
            note = note.trim(),
            date = date,
            createdAt = System.currentTimeMillis(),
            currency = currency
        )
        cashBookDao.insertEntry(entry)
        return entry
    }

    suspend fun updateCashBookEntry(
        id: String,
        type: String,
        amount: Double,
        note: String = "",
        date: Long = System.currentTimeMillis(),
        currency: String = "BDT"
    ) {
        val entry = CashBookEntity(
            id = id,
            type = type,
            amount = amount,
            category = "",
            paymentMethod = "নগদ",
            note = note.trim(),
            date = date,
            createdAt = System.currentTimeMillis(),
            currency = currency
        )
        cashBookDao.insertEntry(entry)
    }

    suspend fun deleteCashBookEntry(id: String) {
        cashBookDao.deleteEntry(id)
    }
}
