package com.example.ariana.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.example.ariana.ui.components.ChequeRow
import com.example.ariana.ui.components.JalaliDatePickerDialog
import com.example.ariana.ui.theme.CreditGreen
import com.example.ariana.ui.theme.DebitRed
import com.example.ariana.ui.theme.PlumPrimary
import com.example.ariana.util.JalaliCalendar
import com.example.ariana.viewmodel.FundViewModel

@Composable
fun ChequesScreen(viewModel: FundViewModel) {
    val filteredCheques by viewModel.filteredCheques.collectAsState()
    val chequeFilter by viewModel.chequeFilter.collectAsState()

    var activeSubTab by remember { mutableStateOf("new") } // "new" or "list"

    // Form states
    var chType by remember { mutableStateOf("payable") } // "payable" or "receivable"
    var personName by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueIsoDate by remember { mutableStateOf(JalaliCalendar.todayIso()) }
    var numberText by remember { mutableStateOf("") }
    var bankText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var formMessage by remember { mutableStateOf<String?>(null) }

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
                    .background(if (activeSubTab == "new") MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { activeSubTab = "new" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ثبت چک جدید",
                    fontWeight = if (activeSubTab == "new") FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (activeSubTab == "new") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                    text = "فهرست چک‌ها (${JalaliCalendar.toPersianDigits(filteredCheques.size.toString())})",
                    fontWeight = if (activeSubTab == "list") FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (activeSubTab == "list") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (activeSubTab == "new") {
            // New Cheque Form
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Type Toggle (پرداختی / دریافتی)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { chType = "payable" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (chType == "payable") DebitRed else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "پرداختی",
                                color = if (chType == "payable") Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { chType = "receivable" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (chType == "receivable") CreditGreen else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "دریافتی",
                                color = if (chType == "receivable") Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text(if (chType == "payable") "دریافت‌کننده چک" else "صادرکننده چک") },
                        placeholder = { Text("مثال: آقای کاوه ارجمندی") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = nationalId,
                        onValueChange = { nationalId = it },
                        label = { Text("کد ملی (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

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
                                        text = JalaliCalendar.formatJalaliLong(dueIsoDate),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = numberText,
                            onValueChange = { numberText = it },
                            label = { Text("شماره چک (اختیاری)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = bankText,
                            onValueChange = { bankText = it },
                            label = { Text("نام بانک (اختیاری)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = descText,
                        onValueChange = { descText = it },
                        label = { Text("توضیحات (اختیاری)") },
                        placeholder = { Text("مثال: بابت اجاره، خرید...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Button(
                        onClick = {
                            val parsedAmount = JalaliCalendar.toEnglishDigits(amountText).toLongOrNull() ?: 0L
                            if (parsedAmount <= 0) {
                                formMessage = "لطفاً مبلغ معتبر وارد کنید."
                                return@Button
                            }
                            if (personName.isBlank()) {
                                formMessage = "لطفاً نام شخص را وارد کنید."
                                return@Button
                            }

                            viewModel.addCheque(
                                type = chType,
                                personName = personName,
                                nationalId = nationalId,
                                amount = parsedAmount,
                                dueDateIso = dueIsoDate,
                                number = numberText,
                                bank = bankText,
                                desc = descText
                            )

                            // Clear form
                            personName = ""
                            nationalId = ""
                            amountText = ""
                            numberText = ""
                            bankText = ""
                            descText = ""
                            formMessage = "چک با موفقیت ثبت شد!"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PlumPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ثبت چک", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
            // Cheques List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filters
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "all" to "همه",
                            "payable" to "پرداختی",
                            "receivable" to "دریافتی",
                            "pending" to "در انتظار"
                        ).forEach { (f, label) ->
                            FilterChip(
                                selected = chequeFilter == f,
                                onClick = { viewModel.chequeFilter.value = f },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                if (filteredCheques.isEmpty()) {
                    item {
                        Text(
                            text = "چکی در این بخش یافت نشد",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp)
                        )
                    }
                } else {
                    items(filteredCheques, key = { it.id }) { ch ->
                        ChequeRow(
                            cheque = ch,
                            onCash = { viewModel.updateChequeStatus(ch.id, "cashed") },
                            onBounce = { viewModel.updateChequeStatus(ch.id, "bounced") },
                            onCancel = { viewModel.updateChequeStatus(ch.id, "cancelled") },
                            onDelete = { viewModel.deleteCheque(ch.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initialDate = JalaliCalendar.isoToJalali(dueIsoDate),
            onDismiss = { showDatePicker = false },
            onDateSelected = { jDate ->
                dueIsoDate = JalaliCalendar.jalaliToIso(jDate.year, jDate.month, jDate.day)
                showDatePicker = false
            }
        )
    }
}
