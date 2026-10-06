package com.example.ariana.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ariana.ui.components.StatTile
import com.example.ariana.ui.components.TransactionRow
import com.example.ariana.ui.theme.CreditGreen
import com.example.ariana.ui.theme.DebitRed
import com.example.ariana.util.JalaliCalendar
import com.example.ariana.viewmodel.FundViewModel

@Composable
fun BreakdownScreen(viewModel: FundViewModel) {
    val fundData by viewModel.fundData.collectAsState()
    var selectedPeriodType by remember { mutableStateOf("monthly") } // daily, weekly, monthly, yearly
    var selectedPersonId by remember { mutableStateOf("all") }

    val today = JalaliCalendar.today()
    val periodLabel = when (selectedPeriodType) {
        "daily" -> "امروز (${today.toLongReadable()})"
        "weekly" -> "هفته جاری"
        "monthly" -> "ماه ${JalaliCalendar.MONTH_NAMES[today.month - 1]} ${JalaliCalendar.toPersianDigits(today.year.toString())}"
        "yearly" -> "کل سال ${JalaliCalendar.toPersianDigits(today.year.toString())}"
        else -> ""
    }

    // Filtered by person and period
    val personTransactions = fundData.transactions.filter { tx ->
        val personMatches = selectedPersonId == "all" || tx.personId == selectedPersonId
        if (!personMatches) return@filter false

        val txJalali = JalaliCalendar.isoToJalali(tx.date)
        when (selectedPeriodType) {
            "daily" -> txJalali.year == today.year && txJalali.month == today.month && txJalali.day == today.day
            "monthly" -> txJalali.year == today.year && txJalali.month == today.month
            "yearly" -> txJalali.year == today.year
            else -> true
        }
    }

    val periodIncome = personTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val periodExpense = personTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    val netTotal = periodIncome - periodExpense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "ریز حساب هر شخص",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Period Type Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "daily" to "روزانه",
                    "weekly" to "هفتگی",
                    "monthly" to "ماهانه",
                    "yearly" to "سالانه"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = selectedPeriodType == type,
                        onClick = { selectedPeriodType = type },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Person Selector
        item {
            Text("انتخاب شخص:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedPersonId == "all",
                        onClick = { selectedPersonId = "all" },
                        label = { Text("همه افراد", fontSize = 12.sp) }
                    )
                }
                items(fundData.people) { person ->
                    FilterChip(
                        selected = selectedPersonId == person.id,
                        onClick = { selectedPersonId = person.id },
                        label = { Text("${person.avatar} ${person.name}", fontSize = 12.sp) }
                    )
                }
            }
        }

        // Period Label Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = periodLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Income & Expense for period
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    title = "واریز این بازه",
                    amount = periodIncome,
                    isIncome = true,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    title = "برداشت این بازه",
                    amount = periodExpense,
                    isIncome = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Net Amount Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "خالص این بازه",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (netTotal >= 0) "+" else ""}${JalaliCalendar.formatToman(netTotal)} تومان",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netTotal >= 0) CreditGreen else DebitRed
                    )
                }
            }
        }

        // Transactions list for person
        item {
            Text(
                text = "تراکنش‌های مرتبط (${JalaliCalendar.toPersianDigits(personTransactions.size.toString())})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (personTransactions.isEmpty()) {
            item {
                Text(
                    text = "تراکنشی در این بازه یافت نشد",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
            }
        } else {
            items(personTransactions, key = { it.id }) { tx ->
                val person = fundData.people.find { it.id == tx.personId }?.name ?: tx.personId
                TransactionRow(
                    tx = tx,
                    personName = person,
                    onDelete = { viewModel.deleteTransaction(tx.id) }
                )
            }
        }
    }
}
