package com.example.ariana.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ariana.ui.theme.PlumPrimary
import com.example.ariana.util.JalaliCalendar
import com.example.ariana.viewmodel.FundViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: FundViewModel,
    onClose: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val fundData by viewModel.fundData.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showShareSummaryDialog by remember { mutableStateOf(false) }

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
            Text(
                text = "تنظیمات",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(onClick = { viewModel.toggleTheme() }) {
                    Text(if (fundData.theme == "dark") "☀️" else "🌙", fontSize = 18.sp)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "بستن")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Security Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("🔒 امنیت", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PlumPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPinDialog = true }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("قفل سریع (PIN چهاررقمی)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (fundData.pinEnabled) "فعال است" else "غیرفعال است",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        }
                    }
                }
            }

            // Backup & Restore Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("💾 پشتیبان‌گیری و بازیابی", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PlumPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBackupDialog = true }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("پشتیبان‌گیری (JSON)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("دریافت متن پشتیبان برای نگهداری امن", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRestoreDialog = true }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("بازیابی از فایل پشتیبان", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("وارد کردن متن JSON پشتیبان", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        }
                    }
                }
            }

            // Reports & Share Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("📊 گزارش‌ها و اشتراک‌گذاری", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PlumPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showShareSummaryDialog = true }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("اشتراک خلاصه صندوق", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("خلاصه واریز و برداشت ماه جاری", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        }
                    }
                }
            }

            // Admin Panel Entry
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAdmin() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PlumPrimary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛠️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("پنل مدیریت صندوق", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PlumPrimary)
                                Text("اعضا، بودجه‌بندی ماهانه، تراکنش‌های تکراری و دسته‌ها", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = PlumPrimary)
                    }
                }
            }

            // Footer
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("آریانا | نسخه ۱.۰", fontSize = 12.sp, color = Color.Gray)
                    Text("ساخته شده برای مدیریت مالی خانواده کاوه ارجمندی", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }

    // PIN Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showPinDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("تنظیم قفل سریع PIN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("یک پین ۴ رقمی برای ورود آسان وارد کنید (یا خالی برای غیرفعال کردن):", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("PIN چهاررقمی") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showPinDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                viewModel.setPin(pinInput)
                                showPinDialog = false
                                Toast.makeText(context, "تنظیمات پین ذخیره شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary)
                        ) {
                            Text("ذخیره", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Backup Dialog
    if (showBackupDialog) {
        var backupJson by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            backupJson = viewModel.repository.exportJson()
        }

        Dialog(onDismissRequest = { showBackupDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("پشتیبان‌گیری از اطلاعات صندوق", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("متن کامل داده‌ها به صورت JSON آماده است:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showBackupDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("بستن")
                        }
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(backupJson))
                                Toast.makeText(context, "در حافظه کپی شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary)
                        ) {
                            Text("کپی در کلیپ‌بورد", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Restore Dialog
    if (showRestoreDialog) {
        var restoreInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showRestoreDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("بازیابی اطلاعات از فایل پشتیبان", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("متن JSON پشتیبان را در کادر زیر وارد کنید:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreInput,
                        onValueChange = { restoreInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showRestoreDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    val ok = viewModel.repository.importJson(restoreInput)
                                    if (ok) {
                                        Toast.makeText(context, "اطلاعات با موفقیت بازیابی شد", Toast.LENGTH_SHORT).show()
                                        showRestoreDialog = false
                                    } else {
                                        Toast.makeText(context, "خطا در قالب داده‌های وارد شده", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary)
                        ) {
                            Text("بازیابی", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Share Summary Dialog
    if (showShareSummaryDialog) {
        val totalInc by viewModel.totalIncome.collectAsState()
        val totalExp by viewModel.totalExpense.collectAsState()
        val totalBal by viewModel.totalBalance.collectAsState()

        val summaryText = """
📊 گردش مالی خانواده آریانا
تاریخ: ${JalaliCalendar.toPersianDigits(JalaliCalendar.todayString())}
-----------------------
🟢 مجموع واریز: ${JalaliCalendar.formatToman(totalInc)} تومان
🔴 مجموع برداشت: ${JalaliCalendar.formatToman(totalExp)} تومان
💳 مانده فعلی حساب: ${JalaliCalendar.formatToman(totalBal)} تومان
-----------------------
ثبت شده در اپلیکیشن آریانا
        """.trimIndent()

        Dialog(onDismissRequest = { showShareSummaryDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("خلاصه وضعیت مالی خانواده", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = summaryText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showShareSummaryDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("بستن")
                        }
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(summaryText))
                                Toast.makeText(context, "متن خلاصه برای ارسال کپی شد", Toast.LENGTH_SHORT).show()
                                showShareSummaryDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary)
                        ) {
                            Text("کپی برای ارسال", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
