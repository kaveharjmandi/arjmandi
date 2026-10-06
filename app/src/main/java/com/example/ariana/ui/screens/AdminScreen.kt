package com.example.ariana.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ariana.model.Person
import com.example.ariana.ui.theme.PlumPrimary
import com.example.ariana.util.JalaliCalendar
import com.example.ariana.viewmodel.FundViewModel

@Composable
fun AdminScreen(
    viewModel: FundViewModel,
    onBack: () -> Unit
) {
    val fundData by viewModel.fundData.collectAsState()

    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryTypeToAdd by remember { mutableStateOf("expense") }
    var showAddRecurringDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "بازگشت")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("پنل مدیریت صندوق", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PlumPrimary)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: People & Avatars
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👥 اعضای خانواده و آواتار", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PlumPrimary)
                            TextButton(onClick = { showAddPersonDialog = true }) {
                                Text("+ افزودن عضو", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        fundData.people.forEach { person ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(person.avatar, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(person.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    if (person.isAdmin) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = PlumPrimary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("مدیر", fontSize = 9.sp, color = PlumPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = if (person.canLogin) "دارای حساب مالی" else "عضو خانواده",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Expense Categories
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏷️ دسته‌بندی‌های برداشت", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PlumPrimary)
                            TextButton(onClick = {
                                categoryTypeToAdd = "expense"
                                showAddCategoryDialog = true
                            }) {
                                Text("+ دسته جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        fundData.categories.expense.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = fundData.categoryIcons[cat] ?: "🏷️"
                                Text("$icon $cat", fontSize = 13.sp)
                                IconButton(
                                    onClick = { viewModel.deleteCategory("expense", cat) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Income Categories
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💰 دسته‌بندی‌های واریز", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PlumPrimary)
                            TextButton(onClick = {
                                categoryTypeToAdd = "income"
                                showAddCategoryDialog = true
                            }) {
                                Text("+ دسته جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        fundData.categories.income.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = fundData.categoryIcons[cat] ?: "💰"
                                Text("$icon $cat", fontSize = 13.sp)
                                IconButton(
                                    onClick = { viewModel.deleteCategory("income", cat) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Recurring Transactions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔄 تراکنش‌های تکرارشونده", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PlumPrimary)
                            TextButton(onClick = { showAddRecurringDialog = true }) {
                                Text("+ افزودن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (fundData.recurring.isEmpty()) {
                            Text("تراکنش تکراری (مثل حقوق ماهانه یا اجاره) ثبت نشده است", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            fundData.recurring.forEach { rec ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(rec.desc, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = "روز ${JalaliCalendar.toPersianDigits(rec.dayOfMonth.toString())} ماه · ${JalaliCalendar.formatToman(rec.amount)} تومان",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteRecurring(rec.id) },
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

    // Add Person Dialog
    if (showAddPersonDialog) {
        var pName by remember { mutableStateOf("") }
        var pAvatar by remember { mutableStateOf("👤") }
        var pCanLogin by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { showAddPersonDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("افزودن عضو جدید خانواده", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("نام شخص") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pAvatar,
                        onValueChange = { pAvatar = it },
                        label = { Text("ایموجی آواتار (مثلاً 👨، 👩، 👦)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showAddPersonDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                if (pName.isNotBlank()) {
                                    viewModel.addPerson(pName, pAvatar, "#6B2D5C", pCanLogin, false)
                                    showAddPersonDialog = false
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

    // Add Category Dialog
    if (showAddCategoryDialog) {
        var cName by remember { mutableStateOf("") }
        var cEmoji by remember { mutableStateOf("🏷️") }

        Dialog(onDismissRequest = { showAddCategoryDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("افزودن دسته‌بندی جدید", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cName,
                        onValueChange = { cName = it },
                        label = { Text("نام دسته‌بندی") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cEmoji,
                        onValueChange = { cEmoji = it },
                        label = { Text("ایموجی آیکون") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showAddCategoryDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                if (cName.isNotBlank()) {
                                    viewModel.addCategory(categoryTypeToAdd, cName, cEmoji)
                                    showAddCategoryDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary)
                        ) {
                            Text("افزودن", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Add Recurring Dialog
    if (showAddRecurringDialog) {
        var rDesc by remember { mutableStateOf("") }
        var rAmount by remember { mutableStateOf("") }
        var rDay by remember { mutableStateOf("1") }
        var rType by remember { mutableStateOf("expense") }

        Dialog(onDismissRequest = { showAddRecurringDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("تراکنش تکرارشونده جدید", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rDesc,
                        onValueChange = { rDesc = it },
                        label = { Text("توضیح (مثلاً: حقوق یا اجاره)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rAmount,
                        onValueChange = { rAmount = it },
                        label = { Text("مبلغ (تومان)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rDay,
                        onValueChange = { rDay = it },
                        label = { Text("روز ماه شمسی (۱ تا ۳۰)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showAddRecurringDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                val amt = JalaliCalendar.toEnglishDigits(rAmount).toLongOrNull() ?: 0L
                                val day = JalaliCalendar.toEnglishDigits(rDay).toIntOrNull() ?: 1
                                if (rDesc.isNotBlank() && amt > 0) {
                                    viewModel.addRecurring(
                                        type = rType,
                                        personId = fundData.people.firstOrNull()?.id ?: "kaveh",
                                        category = "سایر",
                                        desc = rDesc,
                                        amount = amt,
                                        dayOfMonth = day.coerceIn(1, 30)
                                    )
                                    showAddRecurringDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary)
                        ) {
                            Text("افزودن", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
