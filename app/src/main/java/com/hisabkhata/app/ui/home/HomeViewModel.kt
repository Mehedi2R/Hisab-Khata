package com.hisabkhata.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkhata.app.data.local.entity.CustomerEntity
import com.hisabkhata.app.data.local.entity.CustomerWithBalance
import com.hisabkhata.app.data.local.entity.TransactionEntity
import com.hisabkhata.app.data.repository.DashboardSummary
import com.hisabkhata.app.data.repository.HisabRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    val repository = HisabRepository(application)

    private val _selectedTab = MutableStateFlow(CustomerEntity.TYPE_CUSTOMER)
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    val dashboardSummary: StateFlow<DashboardSummary> = repository.getDashboardSummaryFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardSummary()
        )

    val payableProfilesCount: StateFlow<Int> = repository.getCustomersWithBalanceFlow(CustomerEntity.TYPE_CUSTOMER)
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val receivableProfilesCount: StateFlow<Int> = repository.getCustomersWithBalanceFlow(CustomerEntity.TYPE_SUPPLIER)
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val customersWithBalance: StateFlow<List<CustomerWithBalance>> = _selectedTab
        .flatMapLatest { tab -> repository.getCustomersWithBalanceFlow(tab) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCustomerId = MutableStateFlow<String?>(null)
    val selectedCustomerId: StateFlow<String?> = _selectedCustomerId.asStateFlow()

    val selectedCustomerTransactions: StateFlow<List<com.hisabkhata.app.data.local.entity.TransactionEntity>> = _selectedCustomerId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else repository.getTransactionsForCustomerFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCustomer(id: String?) {
        _selectedCustomerId.value = id
    }

    fun setTab(type: String) {
        _selectedTab.value = type
    }

    fun addCustomer(name: String, phone: String, address: String, type: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addCustomer(name, phone, address, type)
            onComplete()
        }
    }

    fun addTransactionForCustomer(
        customerId: String,
        type: String,
        amount: Double,
        note: String,
        date: Long,
        currency: String = TransactionEntity.CURRENCY_BDT,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addTransaction(customerId, type, amount, note, date, currency)
            onComplete()
        }
    }
}
