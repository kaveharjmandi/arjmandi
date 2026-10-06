package com.example.ariana.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ariana.ui.components.BankCard
import com.example.ariana.ui.components.StatTile
import com.example.ariana.ui.components.TransactionRow
import com.example.ariana.ui.theme.CreditGreen
import com.example.ariana.ui.theme.DebitRed
import com.example.ariana.ui.theme.PlumPrimary
import com.example.ariana.util.JalaliCalendar
import com.example.ariana.viewmodel.FundViewModel

@Composable
fun HomeScreen(
    viewModel: FundViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCheques: () -> Unit,
    onQuickAdd: (String) -> Unit
) {
    val fundData by viewModel.fundData.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val accountBalances by viewModel.accountBalances.collectAsState()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsState()
    val dueChequesAlerts by viewModel.dueChequesAlerts.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var showTransferDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // User & Sync Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👤", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("کاوه", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (syncStatus == "synced") CreditGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (syncStatus) {
                                        "synced" -> "☁️ سینک"
                                        "syncing" -> "⏳ در حال سینک"
                                        else -> "📡 آفلاین"
                                    },
                                    fontSize = 10.sp,
                                    color = if (syncStatus == "synced") CreditGreen else Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refreshData() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "همگام‌سازی", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Cheques Alert Banner
        if (dueChequesAlerts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF4E5)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "شما ${JalaliCalendar.toPersianDigits(dueChequesAlerts.size.toString())} چک در انتظار بررسی دارید",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8A5A00)
                            )
                            Text(
                                text = "برای مشاهده تاریخ سررسید و ثبت وضعیت کلیک کنید",
                                fontSize = 11.sp,
                                color = Color(0xFF8A5A00).copy(alpha = 0.8f)
                            )
                        }
                        TextButton(onClick = onNavigateToCheques) {
                            Text("مشاهده", fontSize = 12.sp, color = PlumPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Bank Card
        item {
            BankCard(
                balance = totalBalance,
                isBalanceHidden = isBalanceHidden,
                onToggleVisibility = { viewModel.toggleBalanceVisibility() },
                holderName = "صندوق خانواده",
                dateString = JalaliCalendar.today().toLongReadable()
            )
        }

        // Account Balances Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "موجودی حساب هرکدوم",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val loginOwners = fundData.people.filter { it.canLogin }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        loginOwners.forEach { person ->
                            val balData = accountBalances[person.id] ?: com.example.ariana.viewmodel.AccountBalance()
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${person.avatar} ${person.name}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("واریز:", fontSize = 10.sp, color = Color.Gray)
                                        Text(JalaliCalendar.formatToman(balData.income), fontSize = 10.sp, color = CreditGreen, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("برداشت:", fontSize = 10.sp, color = Color.Gray)
                                        Text(JalaliCalendar.formatToman(balData.expense), fontSize = 10.sp, color = DebitRed, fontWeight = FontWeight.Bold)
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Text(
                                        text = "${JalaliCalendar.formatToman(balData.balance)} تومان",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (balData.balance >= 0) CreditGreen else DebitRed
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showTransferDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🔁 جابجایی بین حساب‌ها", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Totals (Income & Expense)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    title = "مجموع واریز",
                    amount = totalIncome,
                    isIncome = true,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    title = "مجموع برداشت",
                    amount = totalExpense,
                    isIncome = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Streak Card
        val streakCount = fundData.streak?.count ?: 0
        val longestStreak = fundData.streak?.longest ?: 0
        if (streakCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ثبت پیوسته تراکنش‌ها: ${JalaliCalendar.toPersianDigits(streakCount.toString())} روز پیاپی",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "بیشترین رکورد پیوستگی: ${JalaliCalendar.toPersianDigits(longestStreak.toString())} روز",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخرین تراکنش‌ها (${JalaliCalendar.toPersianDigits(fundData.transactions.size.toString())})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onNavigateToTransactions) {
                    Text("مشاهده همه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Recent Transactions list
        val recentList = fundData.transactions.take(6)
        if (recentList.isEmpty()) {
            item {
                Text(
                    text = "تراکنشی ثبت نشده است",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
            }
        } else {
            items(recentList, key = { it.id }) { tx ->
                val person = fundData.people.find { it.id == tx.personId }?.name ?: tx.personId
                TransactionRow(
                    tx = tx,
                    personName = person,
                    onDelete = { viewModel.deleteTransaction(tx.id) }
                )
            }
        }
    }

    // Transfer Dialog
    if (showTransferDialog) {
        var fromPerson by remember { mutableStateOf(fundData.people.firstOrNull { it.canLogin }?.id ?: "") }
        var toPerson by remember { mutableStateOf(fundData.people.filter { it.canLogin }.getOrNull(1)?.id ?: "") }
        var amountText by remember { mutableStateOf("") }
        var noteText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showTransferDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("جابجایی بین حساب‌ها", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    val loginPeople = fundData.people.filter { it.canLogin }

                    Text("از حساب:", fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        loginPeople.forEach { p ->
                            FilterChip(
                                selected = fromPerson == p.id,
                                onClick = { fromPerson = p.id },
                                label = { Text(p.name, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("به حساب:", fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        loginPeople.forEach { p ->
                            FilterChip(
                                selected = toPerson == p.id,
                                onClick = { toPerson = p.id },
                                label = { Text(p.name, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("مبلغ (تومان)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("یادداشت (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showTransferDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                val amount = JalaliCalendar.toEnglishDigits(amountText).toLongOrNull() ?: 0L
                                if (amount > 0 && fromPerson.isNotBlank() && toPerson.isNotBlank()) {
                                    viewModel.transferBetweenAccounts(fromPerson, toPerson, amount, noteText)
                                    showTransferDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary)
                        ) {
                            Text("ثبت جابجایی", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
