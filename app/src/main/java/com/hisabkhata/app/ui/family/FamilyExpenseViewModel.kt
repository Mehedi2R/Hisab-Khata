package com.hisabkhata.app.ui.family

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkhata.app.data.local.entity.FamilyExpenseEntity
import com.hisabkhata.app.data.repository.HisabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HisabRepository(application)

    val allExpenses: StateFlow<List<FamilyExpenseEntity>> = repository.getAllFamilyExpensesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalExpense: StateFlow<Double?> = repository.getTotalFamilyExpenseFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    private val prefs = application.getSharedPreferences("family_expense_prefs", Application.MODE_PRIVATE)
    private val _monthlyBudget = MutableStateFlow(prefs.getFloat("monthly_budget", 0f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        prefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
    }

    fun resetMonthlyBudget() {
        setMonthlyBudget(0.0)
    }

    val expenses: StateFlow<List<FamilyExpenseEntity>> = allExpenses

    fun addExpense(
        amount: Double,
        note: String,
        date: Long,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addFamilyExpense("", amount, note, date)
            onComplete()
        }
    }

    // Overload for backward compatibility if needed
    fun addExpense(
        category: String,
        amount: Double,
        note: String,
        date: Long,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addFamilyExpense(category, amount, note, date)
            onComplete()
        }
    }

    fun updateExpense(
        id: String,
        amount: Double,
        note: String,
        date: Long,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateFamilyExpense(id, amount, note, date)
            onComplete()
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteFamilyExpense(id)
        }
    }
}
