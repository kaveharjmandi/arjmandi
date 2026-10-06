package com.example.ariana.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ariana.ui.components.StatTile
import com.example.ariana.ui.theme.CreditGreen
import com.example.ariana.ui.theme.DebitRed
import com.example.ariana.ui.theme.PlumPrimary
import com.example.ariana.util.JalaliCalendar
import com.example.ariana.viewmodel.FundViewModel

@Composable
fun ReportsAndGoalsScreen(viewModel: FundViewModel) {
    val fundData by viewModel.fundData.collectAsState()
    val categoryExpenses by viewModel.categoryExpenses.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()

    var activeSubTab by remember { mutableStateOf("charts") } // "charts" or "goals"

    // Dialog states
    var showNewGoalDialog by remember { mutableStateOf(false) }
    var showNewLoanDialog by remember { mutableStateOf(false) }
    var showNewLeaseDialog by remember { mutableStateOf(false) }
    var showNewDebtDialog by remember { mutableStateOf(false) }

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
                    .background(if (activeSubTab == "charts") MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { activeSubTab = "charts" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "نمودارها و گزارش",
                    fontWeight = if (activeSubTab == "charts") FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (activeSubTab == "charts") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeSubTab == "goals") MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { activeSubTab = "goals" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "اهداف، وام و بدهی",
                    fontWeight = if (activeSubTab == "goals") FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (activeSubTab == "goals") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (activeSubTab == "charts") {
            // Charts & Category breakdown
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Overview
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

                // Category Expense breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "هزینه‌ها بر اساس دسته‌بندی",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            if (categoryExpenses.isEmpty()) {
                                Text(
                                    text = "هنوز هزینه‌ای ثبت نشده است",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                val chartColors = listOf(
                                    Color(0xFF7A2E68), Color(0xFF2E7D5B), Color(0xFF2563EB),
                                    Color(0xFFEA580C), Color(0xFF7C3AED), Color(0xFFDC2626),
                                    Color(0xFF16A34A), Color(0xFF0D9488), Color(0xFFCA8A04)
                                )

                                categoryExpenses.forEachIndexed { index, cat ->
                                    val color = chartColors[index % chartColors.size]
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cat.category,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${JalaliCalendar.formatToman(cat.amount)} تومان (%${JalaliCalendar.toPersianDigits(cat.percentage.toInt().toString())})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { (cat.percentage / 100f).coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = color,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Largest expenses
                val topExpenses = fundData.transactions
                    .filter { it.type == "expense" }
                    .sortedByDescending { it.amount }
                    .take(5)

                if (topExpenses.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "بزرگ‌ترین هزینه‌های ثبت‌شده",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                topExpenses.forEach { tx ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(tx.desc, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            Text(
                                                text = "${tx.category} · ${JalaliCalendar.formatJalaliShort(tx.date)}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        Text(
                                            text = "- ${JalaliCalendar.formatToman(tx.amount)} تومان",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DebitRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Goals, Loans & Debts
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Loans (وام و اقساط)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏦 وام و اقساط", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PlumPrimary)
                                TextButton(onClick = { showNewLoanDialog = true }) {
                                    Text("+ ثبت وام جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (fundData.loans.isEmpty()) {
                                Text("وامی ثبت نشده است", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                fundData.loans.forEach { loan ->
                                    val remaining = (loan.installmentCount - loan.paidCount).coerceAtLeast(0)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(loan.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                text = "قسط: ${JalaliCalendar.formatToman(loan.installmentAmount)} تومان",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = DebitRed
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "پرداخت شده: ${JalaliCalendar.toPersianDigits(loan.paidCount.toString())} از ${JalaliCalendar.toPersianDigits(loan.installmentCount.toString())} قسط (باقیمانده: ${JalaliCalendar.toPersianDigits(remaining.toString())})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (remaining > 0) {
                                                Button(
                                                    onClick = { viewModel.payLoan(loan.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = CreditGreen),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                ) {
                                                    Text("پرداخت قسط این ماه", fontSize = 11.sp, color = Color.White)
                                                }
                                            } else {
                                                Text("تسویه شده ✅", fontSize = 11.sp, color = CreditGreen, fontWeight = FontWeight.Bold)
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteLoan(loan.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Debts & Credits (بدهی و طلب)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🤝 بدهی و طلب", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PlumPrimary)
                                TextButton(onClick = { showNewDebtDialog = true }) {
                                    Text("+ ثبت طلب/بدهی", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (fundData.debts.isEmpty()) {
                                Text("موردی ثبت نشده است", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                fundData.debts.forEach { debt ->
                                    val isCredit = debt.kind == "credit"
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (isCredit) "🟢 طلب از:" else "🔴 بدهی به:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (isCredit) CreditGreen else DebitRed
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(debt.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Text(
                                                text = if (debt.isGold) "${JalaliCalendar.toPersianDigits(debt.totalAmount.toString())} گرم طلا" else "${JalaliCalendar.formatToman(debt.totalAmount)} تومان",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isCredit) CreditGreen else DebitRed
                                            )
                                        }

                                        if (!debt.note.isNullOrBlank()) {
                                            Text(
                                                text = debt.note,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }

                                        if (debt.payments.isNotEmpty()) {
                                            Text(
                                                text = "پرداخت شده: ${if (debt.isGold) JalaliCalendar.toPersianDigits(debt.paidAmount.toString()) + " گرم" else JalaliCalendar.formatToman(debt.paidAmount) + " تومان"} (باقیمانده: ${if (debt.isGold) JalaliCalendar.toPersianDigits(debt.remainingAmount.toString()) + " گرم" else JalaliCalendar.formatToman(debt.remainingAmount) + " تومان"})",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!debt.isSettled) {
                                                OutlinedButton(
                                                    onClick = { viewModel.settleDebt(debt.id) },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                ) {
                                                    Text("تسویه شد", fontSize = 11.sp)
                                                }
                                            } else {
                                                Text("تسویه شده ✅", fontSize = 11.sp, color = CreditGreen, fontWeight = FontWeight.Bold)
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteDebt(debt.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Savings Goals (جیب‌های پس‌انداز)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎯 جیب‌های پس‌انداز", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PlumPrimary)
                                TextButton(onClick = { showNewGoalDialog = true }) {
                                    Text("+ ساخت جیب جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (fundData.goals.isEmpty()) {
                                Text("هنوز جیب پس‌اندازی ایجاد نشده است", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                fundData.goals.forEach { goal ->
                                    val progress = if (goal.target > 0) (goal.saved.toFloat() / goal.target.toFloat()).coerceIn(0f, 1f) else 0f
                                    val percentInt = (progress * 100).toInt()

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${goal.emoji} ${goal.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                text = "${JalaliCalendar.formatToman(goal.saved)} از ${JalaliCalendar.formatToman(goal.target)} تومان",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = CreditGreen,
                                            trackColor = Color.LightGray.copy(alpha = 0.4f)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "پیشرفت: %${JalaliCalendar.toPersianDigits(percentInt.toString())}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CreditGreen
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedButton(
                                                    onClick = { viewModel.adjustGoal(goal.id, 500000L) },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text("+۵۰۰ هزار", fontSize = 10.sp)
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteGoal(goal.id) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Goal Dialog
    if (showNewGoalDialog) {
        var gName by remember { mutableStateOf("") }
        var gTarget by remember { mutableStateOf("") }
        var gEmoji by remember { mutableStateOf("🎯") }

        Dialog(onDismissRequest = { showNewGoalDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("ساخت جیب پس‌انداز جدید", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = gName,
                        onValueChange = { gName = it },
                        label = { Text("نام هدف (مثلاً: سفر شمال)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = gTarget,
                        onValueChange = { gTarget = it },
                        label = { Text("مبلغ هدف (تومان)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = gEmoji,
                        onValueChange = { gEmoji = it },
                        label = { Text("ایموجی (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showNewGoalDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                val target = JalaliCalendar.toEnglishDigits(gTarget).toLongOrNull() ?: 0L
                                if (gName.isNotBlank() && target > 0) {
                                    viewModel.addGoal(gName, target, gEmoji)
                                    showNewGoalDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary)
                        ) {
                            Text("ایجاد", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // New Loan Dialog
    if (showNewLoanDialog) {
        var lName by remember { mutableStateOf("") }
        var lTotal by remember { mutableStateOf("") }
        var lInstallment by remember { mutableStateOf("") }
        var lCount by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showNewLoanDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("ثبت وام جدید", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = lName,
                        onValueChange = { lName = it },
                        label = { Text("عنوان وام (مثلاً: وام ازدواج، خودرو)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lTotal,
                        onValueChange = { lTotal = it },
                        label = { Text("مبلغ کل وام (تومان)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lInstallment,
                        onValueChange = { lInstallment = it },
                        label = { Text("مبلغ هر قسط (تومان)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lCount,
                        onValueChange = { lCount = it },
                        label = { Text("تعداد کل اقساط (مثلاً ۱۲)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showNewLoanDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                val total = JalaliCalendar.toEnglishDigits(lTotal).toLongOrNull() ?: 0L
                                val inst = JalaliCalendar.toEnglishDigits(lInstallment).toLongOrNull() ?: 0L
                                val cnt = JalaliCalendar.toEnglishDigits(lCount).toIntOrNull() ?: 0
                                if (lName.isNotBlank() && total > 0 && cnt > 0) {
                                    viewModel.addLoan(lName, total, inst, cnt, JalaliCalendar.todayIso(), 1)
                                    showNewLoanDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary)
                        ) {
                            Text("ثبت وام", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // New Debt Dialog
    if (showNewDebtDialog) {
        var dKind by remember { mutableStateOf("credit") } // "credit" or "debt"
        var dPerson by remember { mutableStateOf("") }
        var dAmount by remember { mutableStateOf("") }
        var dNote by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showNewDebtDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("ثبت بدهی یا طلب", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { dKind = "credit" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dKind == "credit") CreditGreen else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("طلب (ما می‌خواهیم)", fontSize = 11.sp, color = if (dKind == "credit") Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = { dKind = "debt" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dKind == "debt") DebitRed else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("بدهی (باید بدهیم)", fontSize = 11.sp, color = if (dKind == "debt") Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = dPerson,
                        onValueChange = { dPerson = it },
                        label = { Text("نام شخص") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dAmount,
                        onValueChange = { dAmount = it },
                        label = { Text("مبلغ کل (تومان)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dNote,
                        onValueChange = { dNote = it },
                        label = { Text("توضیحات (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showNewDebtDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                val amt = JalaliCalendar.toEnglishDigits(dAmount).toDoubleOrNull() ?: 0.0
                                if (dPerson.isNotBlank() && amt > 0) {
                                    viewModel.addDebt(dKind, false, dPerson, amt, JalaliCalendar.todayIso(), dNote)
                                    showNewDebtDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary)
                        ) {
                            Text("ثبت", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
