package com.example.ariana.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ariana.data.FamilyFundRepository
import com.example.ariana.model.*
import com.example.ariana.util.JalaliCalendar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class AccountBalance(
    val income: Long = 0,
    val expense: Long = 0,
    val balance: Long = 0
)

data class CategoryExpense(
    val category: String,
    val amount: Long,
    val percentage: Float
)

class FundViewModel(application: Application) : AndroidViewModel(application) {

    val repository = FamilyFundRepository(application)
    val fundData: StateFlow<FamilyFundData> = repository.fundData
    val syncStatus: StateFlow<String> = repository.syncStatus

    val currentTab = MutableStateFlow("home")
    val isBalanceHidden = MutableStateFlow(false)
    val isFabOpen = MutableStateFlow(false)
    val showSettings = MutableStateFlow(false)
    val showAdmin = MutableStateFlow(false)

    // Transaction filter states
    val txSearchQuery = MutableStateFlow("")
    val txTypeFilter = MutableStateFlow("all") // "all", "income", "expense"
    val txPersonFilter = MutableStateFlow("all")
    val txAccountFilter = MutableStateFlow("all")

    // Cheques filter state
    val chequeFilter = MutableStateFlow("all") // "all", "payable", "receivable", "pending"

    val totalIncome: StateFlow<Long> = fundData.map { data ->
        data.transactions.filter { it.type == "income" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val totalExpense: StateFlow<Long> = fundData.map { data ->
        data.transactions.filter { it.type == "expense" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val totalBalance: StateFlow<Long> = combine(totalIncome, totalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val accountBalances: StateFlow<Map<String, AccountBalance>> = fundData.map { data ->
        val owners = data.people.filter { it.canLogin }
        val map = owners.associate { it.id to AccountBalance() }.toMutableMap()

        data.transactions.forEach { tx ->
            val accId = tx.accountId ?: tx.enteredById ?: tx.personId
            if (map.containsKey(accId)) {
                val current = map[accId] ?: AccountBalance()
                if (tx.type == "income") {
                    map[accId] = current.copy(income = current.income + tx.amount, balance = current.balance + tx.amount)
                } else {
                    map[accId] = current.copy(expense = current.expense + tx.amount, balance = current.balance - tx.amount)
                }
            }
        }

        // Apply person transfers (shifts balance only)
        data.personTransfers.forEach { pt ->
            val from = map[pt.fromPersonId]
            if (from != null) {
                map[pt.fromPersonId] = from.copy(balance = from.balance - pt.amount)
            }
            val to = map[pt.toPersonId]
            if (to != null) {
                map[pt.toPersonId] = to.copy(balance = to.balance + pt.amount)
            }
        }

        map
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        fundData, txSearchQuery, txTypeFilter, txPersonFilter, txAccountFilter
    ) { data, query, typeFilter, personFilter, accountFilter ->
        data.transactions.filter { tx ->
            val matchesQuery = query.isBlank() || tx.desc.contains(query, ignoreCase = true) || tx.category.contains(query, ignoreCase = true)
            val matchesType = when (typeFilter) {
                "income" -> tx.type == "income"
                "expense" -> tx.type == "expense"
                else -> true
            }
            val matchesPerson = personFilter == "all" || tx.personId == personFilter
            val matchesAccount = accountFilter == "all" || (tx.accountId ?: tx.personId) == accountFilter
            matchesQuery && matchesType && matchesPerson && matchesAccount
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredCheques: StateFlow<List<Cheque>> = combine(fundData, chequeFilter) { data, filter ->
        data.cheques.filter { ch ->
            when (filter) {
                "payable" -> ch.isPayable
                "receivable" -> !ch.isPayable
                "pending" -> ch.status == "pending"
                else -> true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dueChequesAlerts: StateFlow<List<Cheque>> = fundData.map { data ->
        data.cheques.filter { it.status == "pending" }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categoryExpenses: StateFlow<List<CategoryExpense>> = fundData.map { data ->
        val expenseTx = data.transactions.filter { it.type == "expense" }
        val total = expenseTx.sumOf { it.amount }
        if (total == 0L) return@map emptyList()

        val grouped = mutableMapOf<String, Long>()
        expenseTx.forEach { tx ->
            grouped[tx.category] = (grouped[tx.category] ?: 0L) + tx.amount
        }

        grouped.entries.sortedByDescending { it.value }.map { entry ->
            CategoryExpense(
                category = entry.key,
                amount = entry.value,
                percentage = (entry.value.toFloat() / total.toFloat()) * 100f
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun refreshData() {
        repository.fetchFromSupabase()
    }

    fun toggleBalanceVisibility() {
        isBalanceHidden.value = !isBalanceHidden.value
    }

    fun toggleFab() {
        isFabOpen.value = !isFabOpen.value
    }

    fun addTransaction(
        type: String,
        personId: String,
        accountId: String,
        category: String,
        desc: String,
        amount: Long,
        dateIso: String
    ) {
        viewModelScope.launch {
            val tx = Transaction(
                id = "tm_" + UUID.randomUUID().toString().take(10),
                type = type,
                personId = personId,
                accountId = accountId,
                category = category,
                desc = desc,
                amount = amount,
                date = dateIso,
                walletId = fundData.value.wallets.firstOrNull()?.id ?: "w_main"
            )
            repository.addTransaction(tx)
        }
    }

    fun editTransaction(tx: Transaction) {
        viewModelScope.launch {
            repository.editTransaction(tx)
        }
    }

    fun deleteTransaction(txId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(txId)
        }
    }

    fun addCheque(
        type: String,
        personName: String,
        nationalId: String,
        amount: Long,
        dueDateIso: String,
        number: String,
        bank: String,
        desc: String
    ) {
        viewModelScope.launch {
            val cheque = Cheque(
                id = "ch_" + UUID.randomUUID().toString().take(10),
                personName = personName,
                nationalId = nationalId,
                amount = amount,
                dueDate = dueDateIso,
                number = number,
                bank = bank,
                desc = desc,
                status = "pending",
                direction = type,
                type = type
            )
            repository.addCheque(cheque)
        }
    }

    fun updateChequeStatus(chequeId: String, status: String) {
        viewModelScope.launch {
            repository.updateChequeStatus(chequeId, status)
        }
    }

    fun deleteCheque(chequeId: String) {
        viewModelScope.launch {
            repository.deleteCheque(chequeId)
        }
    }

    fun addGoal(name: String, target: Long, emoji: String) {
        viewModelScope.launch {
            val goal = Goal(
                id = "g_" + UUID.randomUUID().toString().take(8),
                name = name,
                target = target,
                saved = 0L,
                emoji = if (emoji.isBlank()) "🎯" else emoji
            )
            repository.addGoal(goal)
        }
    }

    fun adjustGoal(goalId: String, delta: Long) {
        viewModelScope.launch {
            repository.adjustGoalSavings(goalId, delta)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun addLoan(name: String, total: Long, installment: Long, count: Int, startIso: String, dueDay: Int) {
        viewModelScope.launch {
            val loan = Loan(
                id = "ln_" + UUID.randomUUID().toString().take(8),
                name = name,
                totalAmount = total,
                installmentAmount = installment,
                installmentCount = count,
                paidCount = 0,
                startDate = startIso,
                dueDayOfMonth = dueDay
            )
            repository.addLoan(loan)
        }
    }

    fun payLoan(loanId: String) {
        viewModelScope.launch {
            repository.payLoanInstallment(loanId)
        }
    }

    fun deleteLoan(loanId: String) {
        viewModelScope.launch {
            repository.deleteLoan(loanId)
        }
    }

    fun addLease(name: String, deposit: Long, rent: Long, startIso: String, dueDay: Int) {
        viewModelScope.launch {
            val lease = Lease(
                id = "ls_" + UUID.randomUUID().toString().take(8),
                name = name,
                depositAmount = deposit,
                monthlyRent = rent,
                startDate = startIso,
                dueDayOfMonth = dueDay,
                paidMonths = 0
            )
            repository.addLease(lease)
        }
    }

    fun payLease(leaseId: String) {
        viewModelScope.launch {
            repository.payLeaseRent(leaseId)
        }
    }

    fun deleteLease(leaseId: String) {
        viewModelScope.launch {
            repository.deleteLease(leaseId)
        }
    }

    fun addDebt(kind: String, isGold: Boolean, personName: String, totalAmount: Double, dueDateIso: String, note: String) {
        viewModelScope.launch {
            val debt = Debt(
                id = "d_" + UUID.randomUUID().toString().take(8),
                kind = kind,
                person = personName,
                personName = personName,
                unit = if (isGold) "gold" else "toman",
                totalAmount = totalAmount,
                dueDate = dueDateIso.ifBlank { null },
                note = note
            )
            repository.addDebt(debt)
        }
    }

    fun settleDebt(debtId: String) {
        viewModelScope.launch {
            repository.settleDebt(debtId)
        }
    }

    fun deleteDebt(debtId: String) {
        viewModelScope.launch {
            repository.deleteDebt(debtId)
        }
    }

    fun transferBetweenAccounts(fromId: String, toId: String, amount: Long, note: String) {
        viewModelScope.launch {
            val pt = PersonTransfer(
                id = "pt_" + UUID.randomUUID().toString().take(10),
                fromPersonId = fromId,
                toPersonId = toId,
                amount = amount,
                note = note,
                enteredById = fromId
            )
            repository.addPersonTransfer(pt)
        }
    }

    fun addCategory(type: String, name: String, emoji: String) {
        viewModelScope.launch {
            repository.update { current ->
                val cats = current.categories
                val updatedCats = if (type == "income") {
                    if (!cats.income.contains(name)) cats.copy(income = cats.income + name) else cats
                } else {
                    if (!cats.expense.contains(name)) cats.copy(expense = cats.expense + name) else cats
                }
                val icons = current.categoryIcons.toMutableMap()
                if (emoji.isNotBlank()) {
                    icons[name] = emoji
                }
                current.copy(categories = updatedCats, categoryIcons = icons)
            }
        }
    }

    fun deleteCategory(type: String, name: String) {
        viewModelScope.launch {
            repository.update { current ->
                val cats = current.categories
                val updatedCats = if (type == "income") {
                    cats.copy(income = cats.income.filterNot { it == name })
                } else {
                    cats.copy(expense = cats.expense.filterNot { it == name })
                }
                current.copy(categories = updatedCats)
            }
        }
    }

    fun addPerson(name: String, avatar: String, color: String, canLogin: Boolean, isAdmin: Boolean) {
        viewModelScope.launch {
            val person = Person(
                id = name.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis() % 1000,
                name = name,
                isAdmin = isAdmin,
                canLogin = canLogin,
                avatar = if (avatar.isBlank()) "👤" else avatar,
                color = if (color.isBlank()) "#6B2D5C" else color
            )
            repository.update { current ->
                current.copy(people = current.people + person)
            }
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(pinCode = pin, pinEnabled = pin.isNotBlank())
            }
        }
    }

    fun addRecurring(type: String, personId: String, category: String, desc: String, amount: Long, dayOfMonth: Int) {
        viewModelScope.launch {
            val item = RecurringItem(
                id = "rec_" + UUID.randomUUID().toString().take(8),
                type = type,
                personId = personId,
                category = category,
                desc = desc,
                amount = amount,
                dayOfMonth = dayOfMonth
            )
            repository.update { current ->
                current.copy(recurring = current.recurring + item)
            }
        }
    }

    fun deleteRecurring(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(recurring = current.recurring.filterNot { it.id == id })
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            repository.update { current ->
                val next = if (current.theme == "dark") "light" else "dark"
                current.copy(theme = next)
            }
        }
    }
}
