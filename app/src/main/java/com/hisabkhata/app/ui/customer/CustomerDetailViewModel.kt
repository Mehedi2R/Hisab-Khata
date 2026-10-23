package com.hisabkhata.app.ui.customer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkhata.app.data.local.entity.CustomerEntity
import com.hisabkhata.app.data.local.entity.CustomerWithBalance
import com.hisabkhata.app.data.local.entity.TransactionEntity
import com.hisabkhata.app.data.repository.HisabRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerDetailViewModel(
    application: Application,
    private val customerId: String
) : AndroidViewModel(application) {

    private val repository = HisabRepository(application)

    val customerBalance: StateFlow<CustomerWithBalance?> = repository.getCustomerBalanceFlow(customerId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val transactions: StateFlow<List<TransactionEntity>> = repository.getTransactionsForCustomerFlow(customerId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTransaction(
        type: String,
        amount: Double,
        note: String,
        date: Long,
        currency: String = TransactionEntity.CURRENCY_BDT,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                customerId = customerId,
                type = type,
                amount = amount,
                note = note,
                date = date,
                currency = currency
            )
            onComplete()
        }
    }

    fun updateTransaction(
        transactionId: String,
        type: String,
        amount: Double,
        note: String,
        date: Long,
        currency: String = TransactionEntity.CURRENCY_BDT,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateTransaction(
                transactionId = transactionId,
                customerId = customerId,
                type = type,
                amount = amount,
                note = note,
                date = date,
                currency = currency
            )
            onComplete()
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }

    fun updateCustomer(
        name: String,
        phone: String,
        address: String,
        type: String,
        onComplete: () -> Unit
    ) {
        val current = customerBalance.value?.customer ?: return
        val updated = current.copy(
            name = name,
            phone = phone,
            address = address,
            type = type,
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.updateCustomer(updated)
            onComplete()
        }
    }

    fun deleteCustomer(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
            onComplete()
        }
    }

    fun getStatementShareText(): String {
        val balance = customerBalance.value ?: return ""
        val txs = transactions.value
        return com.hisabkhata.app.ui.components.generateCustomerReportText(balance, txs)
    }
}
