package com.hisabkhata.app.ui.cashbook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkhata.app.data.local.entity.CashBookEntity
import com.hisabkhata.app.data.repository.HisabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CashSummaryState(
    val netCashBdt: Double = 0.0,
    val totalIncomeBdt: Double = 0.0,
    val totalExpenseBdt: Double = 0.0,
    val netCashUsd: Double = 0.0,
    val totalIncomeUsd: Double = 0.0,
    val totalExpenseUsd: Double = 0.0,
    val hasUsd: Boolean = false
)

class CashBookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HisabRepository(application)

    val allEntries: StateFlow<List<CashBookEntity>> = repository.getAllCashBookEntriesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cashSummary: StateFlow<CashSummaryState> = allEntries.map { list ->
        val bdt = list.filter { it.currency != "USD" }
        val usd = list.filter { it.currency == "USD" }
        val inBdt = bdt.filter { it.type == "INCOME" }.sumOf { it.amount }
        val outBdt = bdt.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val inUsd = usd.filter { it.type == "INCOME" }.sumOf { it.amount }
        val outUsd = usd.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        CashSummaryState(
            netCashBdt = inBdt - outBdt,
            totalIncomeBdt = inBdt,
            totalExpenseBdt = outBdt,
            netCashUsd = inUsd - outUsd,
            totalIncomeUsd = inUsd,
            totalExpenseUsd = outUsd,
            hasUsd = usd.isNotEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CashSummaryState()
    )

    private val _filterType = MutableStateFlow("ALL") // "ALL", "INCOME", "EXPENSE"
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    val filteredEntries: StateFlow<List<CashBookEntity>> = combine(allEntries, _filterType) { list, filter ->
        when (filter) {
            "INCOME" -> list.filter { it.type == "INCOME" }
            "EXPENSE" -> list.filter { it.type == "EXPENSE" }
            else -> list
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilterType(filter: String) {
        _filterType.value = filter
    }

    fun addEntry(
        type: String,
        amount: Double,
        note: String,
        date: Long,
        currency: String = "BDT",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addCashBookEntry(type, amount, "", "নগদ", note, date, currency)
            onComplete()
        }
    }

    fun updateEntry(
        id: String,
        type: String,
        amount: Double,
        note: String,
        date: Long,
        currency: String = "BDT",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateCashBookEntry(id, type, amount, note, date, currency)
            onComplete()
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            repository.deleteCashBookEntry(id)
        }
    }
}
