package com.example.ariana.model

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val id: String,
    val name: String,
    val isAdmin: Boolean = false,
    val canLogin: Boolean = true,
    val avatar: String = "👤",
    val color: String = "#6B2D5C"
)

@Serializable
data class Transaction(
    val id: String,
    val date: String, // "YYYY-MM-DD"
    val desc: String,
    val type: String, // "income" or "expense"
    val amount: Long,
    val category: String,
    val personId: String,
    val accountId: String? = null,
    val walletId: String? = null,
    val enteredById: String? = null,
    val editedAt: Long? = null,
    val editedBy: String? = null,
    val loanId: String? = null,
    val leaseId: String? = null,
    val photos: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Cheque(
    val id: String,
    val personName: String,
    val nationalId: String? = null,
    val amount: Long,
    val dueDate: String, // "YYYY-MM-DD"
    val number: String? = null,
    val bank: String? = null,
    val desc: String? = null,
    val status: String = "pending", // "pending", "cashed", "bounced", "cancelled"
    val direction: String? = null, // "payable" or "receivable"
    val type: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isPayable: Boolean
        get() = direction == "payable" || type == "payable"
}

@Serializable
data class DebtPayment(
    val id: String,
    val date: String,
    val amount: Double,
    val createdAt: Long? = null,
    val goldPricePerGramAtPayment: Long? = null
)

@Serializable
data class Debt(
    val id: String,
    val kind: String, // "debt" or "credit"
    val person: String? = null,
    val personName: String? = null,
    val unit: String? = "toman", // "gold" or "toman"
    val totalAmount: Double = 0.0,
    val dueDate: String? = null,
    val note: String? = null,
    val payments: List<DebtPayment> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null
) {
    val displayName: String
        get() = person ?: personName ?: ""

    val isGold: Boolean
        get() = unit == "gold"

    val paidAmount: Double
        get() = payments.sumOf { it.amount }

    val remainingAmount: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)

    val isSettled: Boolean
        get() = remainingAmount <= 0.001
}

@Serializable
data class Goal(
    val id: String,
    val name: String,
    val target: Long = 0,
    val saved: Long = 0,
    val emoji: String = "🎯",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Loan(
    val id: String,
    val name: String,
    val totalAmount: Long,
    val installmentAmount: Long,
    val installmentCount: Int = 0,
    val paidCount: Int = 0,
    val startDate: String = "",
    val dueDayOfMonth: Int = 1,
    val walletId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Lease(
    val id: String,
    val name: String,
    val depositAmount: Long = 0,
    val monthlyRent: Long = 0,
    val startDate: String = "",
    val dueDayOfMonth: Int = 1,
    val paidMonths: Int = 0,
    val walletId: String? = null,
    val depositReturned: Boolean? = null,
    val active: Boolean? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Wallet(
    val id: String,
    val name: String,
    val type: String = "fund",
    val icon: String = "🏛️",
    val color: String = "#6B2D5C",
    val initialBalance: Long = 0,
    val note: String? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class PersonTransfer(
    val id: String,
    val fromPersonId: String,
    val toPersonId: String,
    val amount: Long,
    val note: String? = null,
    val enteredById: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class ActivityItem(
    val id: String,
    val who: String? = null,
    val action: String? = null,
    val detail: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class RecurringItem(
    val id: String,
    val type: String, // "income" or "expense"
    val personId: String,
    val category: String,
    val desc: String,
    val amount: Long,
    val dayOfMonth: Int
)

@Serializable
data class Categories(
    val income: List<String> = listOf("حقوق", "کارانه", "پاداش / عیدی", "سایر درآمد"),
    val expense: List<String> = listOf(
        "خانه و اجاره", "قسط وام", "برداشت چک", "خرید و خواربار",
        "گوشت و پروتئین", "وسایل نقلیه", "تحصیل", "قبض و اشتراک",
        "بهداشت و درمان", "پوشاک و اکسسوری", "تفریح و سرگرمی",
        "میوه و سبزیجات", "ورزش", "سایر هزینه"
    )
)

@Serializable
data class Streak(
    val count: Int = 0,
    val lastDateIso: String? = null,
    val longest: Int = 0
)

@Serializable
data class GoldPriceCache(
    val source: String = "manual",
    val dateIso: String = "",
    val pricePerGram: Long = 24000000L
)

@Serializable
data class FamilyFundData(
    val schema: Int = 1,
    val title: String = "گردش مالی خانواده",
    val theme: String? = "light",
    val currency: String = "تومان",
    val people: List<Person> = emptyList(),
    val categories: Categories = Categories(),
    val categoryIcons: Map<String, String> = emptyMap(),
    val transactions: List<Transaction> = emptyList(),
    val cheques: List<Cheque> = emptyList(),
    val budgets: Map<String, Long> = emptyMap(),
    val recurring: List<RecurringItem> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val loans: List<Loan> = emptyList(),
    val leases: List<Lease> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val personTransfers: List<PersonTransfer> = emptyList(),
    val activityLog: List<ActivityItem> = emptyList(),
    val streak: Streak? = null,
    val goldPriceCache: GoldPriceCache? = null,
    val autoLockMinutes: Int = 5,
    val lowBalanceThreshold: Long? = 1000000L,
    val captchaEnabled: Boolean = false,
    val pinCode: String? = null,
    val pinEnabled: Boolean = false
)
