package com.example.ariana.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ariana.ui.components.JalaliDatePickerDialog
import com.example.ariana.ui.components.TransactionRow
import com.example.ariana.ui.theme.CreditGreen
import com.example.ariana.ui.theme.DebitRed
import com.example.ariana.ui.theme.PlumPrimary
import com.example.ariana.util.JalaliCalendar
import com.example.ariana.viewmodel.FundViewModel

@Composable
fun TransactionsScreen(viewModel: FundViewModel) {
    val fundData by viewModel.fundData.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.txSearchQuery.collectAsState()
    val typeFilter by viewModel.txTypeFilter.collectAsState()
    val personFilter by viewModel.txPersonFilter.collectAsState()
    val accountFilter by viewModel.txAccountFilter.collectAsState()

    var activeSubTab by remember { mutableStateOf("add") } // "add" or "list"

    // Form states
    var txType by remember { mutableStateOf("expense") }
    var selectedPersonId by remember { mutableStateOf(fundData.people.firstOrNull()?.id ?: "") }
    var selectedAccountId by remember { mutableStateOf(fundData.people.firstOrNull { it.canLogin }?.id ?: "") }
    var selectedCategory by remember { mutableStateOf(fundData.categories.expense.firstOrNull() ?: "") }
    var descText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedIsoDate by remember { mutableStateOf(JalaliCalendar.todayIso()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var formMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(txType) {
        selectedCategory = if (txType == "income") {
            fundData.categories.income.firstOrNull() ?: ""
        } else {
            fundData.categories.expense.firstOrNull() ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sub-Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeSubTab == "add") MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { activeSubTab = "add" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ثبت تراکنش جدید",
                    fontWeight = if (activeSubTab == "add") FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (activeSubTab == "add") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeSubTab == "list") MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { activeSubTab = "list" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "فهرست تراکنش‌ها (${JalaliCalendar.toPersianDigits(filteredTransactions.size.toString())})",
                    fontWeight = if (activeSubTab == "list") FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (activeSubTab == "list") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (activeSubTab == "add") {
            // New Transaction Form
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Type Toggle (واریز / برداشت)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { txType = "expense" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (txType == "expense") DebitRed else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "برداشت (−)",
                                color = if (txType == "expense") Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { txType = "income" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (txType == "income") CreditGreen else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "واریز (+)",
                                color = if (txType == "income") Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Attributed to Person (ثبت به نام)
                item {
                    Text("ثبت به نام شخص:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(fundData.people) { person ->
                            FilterChip(
                                selected = selectedPersonId == person.id,
                                onClick = { selectedPersonId = person.id },
                                label = { Text("${person.avatar} ${person.name}", fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // From Account (از حساب کدام‌یک)
                item {
                    val loginOwners = fundData.people.filter { it.canLogin }
                    Text("از حساب مالی:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(loginOwners) { person ->
                            FilterChip(
                                selected = selectedAccountId == person.id,
                                onClick = { selectedAccountId = person.id },
                                label = { Text("حساب ${person.name}", fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // Category Selection
                item {
                    val cats = if (txType == "income") fundData.categories.income else fundData.categories.expense
                    Text("دسته‌بندی:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(cats) { cat ->
                            val icon = fundData.categoryIcons[cat] ?: "🏷️"
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text("$icon $cat", fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // Description
                item {
                    OutlinedTextField(
                        value = descText,
                        onValueChange = { descText = it },
                        label = { Text("توضیح تراکنش (مثلاً: خرید میوه، حقوق شهریور...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Amount and Date
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("مبلغ (تومان)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedCard(
                            onClick = { showDatePicker = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = JalaliCalendar.formatJalaliShort(selectedIsoDate),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    Button(
                        onClick = {
                            val parsedAmount = JalaliCalendar.toEnglishDigits(amountText).toLongOrNull() ?: 0L
                            if (parsedAmount <= 0) {
                                formMessage = "لطفاً مبلغ معتبر وارد کنید."
                                return@Button
                            }
                            if (descText.isBlank()) {
                                formMessage = "لطفاً توضیح تراکنش را وارد کنید."
                                return@Button
                            }

                            viewModel.addTransaction(
                                type = txType,
                                personId = selectedPersonId.ifBlank { fundData.people.firstOrNull()?.id ?: "" },
                                accountId = selectedAccountId.ifBlank { selectedPersonId },
                                category = selectedCategory,
                                desc = descText,
                                amount = parsedAmount,
                                dateIso = selectedIsoDate
                            )

                            // Clear form
                            descText = ""
                            amountText = ""
                            formMessage = "تراکنش با موفقیت ثبت شد!"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ثبت تراکنش", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    formMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            color = if (msg.contains("موفقیت")) CreditGreen else DebitRed,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Transactions List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.txSearchQuery.value = it },
                        placeholder = { Text("جست‌وجو در توضیح یا دسته‌بندی…", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.txSearchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Type Filters (همه, واریزها, برداشت‌ها)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = typeFilter == "all",
                            onClick = { viewModel.txTypeFilter.value = "all" },
                            label = { Text("همه") }
                        )
                        FilterChip(
                            selected = typeFilter == "income",
                            onClick = { viewModel.txTypeFilter.value = "income" },
                            label = { Text("واریزها (+)") }
                        )
                        FilterChip(
                            selected = typeFilter == "expense",
                            onClick = { viewModel.txTypeFilter.value = "expense" },
                            label = { Text("برداشت‌ها (−)") }
                        )
                    }
                }

                // Search Summary Card
                item {
                    val incSum = filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
                    val expSum = filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تعداد: ${JalaliCalendar.toPersianDigits(filteredTransactions.size.toString())}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "واریز: ${JalaliCalendar.formatToman(incSum)}",
                                fontSize = 12.sp,
                                color = CreditGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "برداشت: ${JalaliCalendar.formatToman(expSum)}",
                                fontSize = 12.sp,
                                color = DebitRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Items
                if (filteredTransactions.isEmpty()) {
                    item {
                        Text(
                            text = "موردی یافت نشد",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp)
                        )
                    }
                } else {
                    items(filteredTransactions, key = { it.id }) { tx ->
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
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initialDate = JalaliCalendar.isoToJalali(selectedIsoDate),
            onDismiss = { showDatePicker = false },
            onDateSelected = { jDate ->
                selectedIsoDate = JalaliCalendar.jalaliToIso(jDate.year, jDate.month, jDate.day)
                showDatePicker = false
            }
        )
    }
}
