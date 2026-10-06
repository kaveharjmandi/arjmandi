package com.example.ariana.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ariana.model.Cheque
import com.example.ariana.model.Transaction
import com.example.ariana.ui.theme.*
import com.example.ariana.util.JalaliCalendar
import com.example.ariana.util.JalaliDate

@Composable
fun BankCard(
    balance: Long,
    isBalanceHidden: Boolean,
    onToggleVisibility: () -> Unit,
    holderName: String = "صندوق خانواده",
    dateString: String = JalaliCalendar.today().toLongReadable()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF7A2E68),
                            Color(0xFF591F4B),
                            Color(0xFF1F5940)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "صندوق خانواده آریانا",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "💳",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Label and Eye Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مانده فعلی حساب",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "نمایش یا پنهان‌کردن موجودی",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Balance Amount
                val balanceText = if (isBalanceHidden) "••••••••" else JalaliCalendar.formatToman(balance) + " تومان"
                Text(
                    text = balanceText,
                    color = if (balance < 0) Color(0xFFFFB3B3) else Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom row: Holder & Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👤 $holderName",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = dateString,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatTile(
    title: String,
    amount: Long,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${if (isIncome) "+" else "-"} ${JalaliCalendar.formatToman(amount)} تومان",
                color = if (isIncome) CreditGreen else DebitRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun TransactionRow(
    tx: Transaction,
    personName: String,
    onDelete: (() -> Unit)? = null
) {
    val isIncome = tx.type == "income"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) CreditGreen.copy(alpha = 0.15f) else DebitRed.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isIncome) "➕" else "➖",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tx.desc,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tx.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " · $personName · ${JalaliCalendar.formatJalaliShort(tx.date)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (isIncome) "+" else "-"} ${JalaliCalendar.formatToman(tx.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isIncome) CreditGreen else DebitRed
                )
                Text(
                    text = "تومان",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف تراکنش",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChequeRow(
    cheque: Cheque,
    onCash: () -> Unit,
    onBounce: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val isPayable = cheque.isPayable
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPayable) "🧾 پرداختی" else "🧾 دریافتی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isPayable) DebitRed else CreditGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ChequeStatusBadge(cheque.status)
                }
                Text(
                    text = "${JalaliCalendar.formatToman(cheque.amount)} تومان",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isPayable) DebitRed else CreditGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${if (isPayable) "در وجه:" else "از طرف:"} ${cheque.personName}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "سررسید: ${JalaliCalendar.formatJalaliLong(cheque.dueDate)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (!cheque.bank.isNullOrBlank()) {
                    Text(
                        text = "بانک ${cheque.bank} ${if (!cheque.number.isNullOrBlank()) "· شماره ${JalaliCalendar.toPersianDigits(cheque.number)}" else ""}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (!cheque.desc.isNullOrBlank()) {
                Text(
                    text = cheque.desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Action Buttons
            if (cheque.status == "pending") {
                Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBounce,
                        modifier = Modifier.padding(end = 6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("برگشت خورد", fontSize = 11.sp, color = DebitRed)
                    }
                    Button(
                        onClick = onCash,
                        modifier = Modifier.padding(end = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CreditGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("پاس شد", fontSize = 11.sp, color = Color.White)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف چک", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChequeStatusBadge(status: String) {
    val (label, bg, fg) = when (status) {
        "cashed" -> Triple("پاس شده", CreditGreen.copy(alpha = 0.15f), CreditGreen)
        "bounced" -> Triple("برگشت خورده", DebitRed.copy(alpha = 0.15f), DebitRed)
        "cancelled" -> Triple("باطل شده", Color.Gray.copy(alpha = 0.15f), Color.Gray)
        else -> Triple("در انتظار", WarnBgLight, WarnTextLight)
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun JalaliDatePickerDialog(
    initialDate: JalaliDate = JalaliCalendar.today(),
    onDismiss: () -> Unit,
    onDateSelected: (JalaliDate) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialDate.year) }
    var selectedMonth by remember { mutableStateOf(initialDate.month) }
    var selectedDay by remember { mutableStateOf(initialDate.day) }

    val daysInMonth = JalaliCalendar.getDaysInJalaliMonth(selectedYear, selectedMonth)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "انتخاب تاریخ شمسی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear++ }) {
                        Text("▲", fontSize = 14.sp)
                    }
                    Text(
                        text = "سال ${JalaliCalendar.toPersianDigits(selectedYear.toString())}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = { selectedYear-- }) {
                        Text("▼", fontSize = 14.sp)
                    }
                }

                // Month selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (selectedMonth < 12) selectedMonth++ else { selectedMonth = 1; selectedYear++ }
                    }) {
                        Text("▲", fontSize = 14.sp)
                    }
                    Text(
                        text = "${JalaliCalendar.MONTH_NAMES[selectedMonth - 1]} (${JalaliCalendar.toPersianDigits(selectedMonth.toString())})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = {
                        if (selectedMonth > 1) selectedMonth-- else { selectedMonth = 12; selectedYear-- }
                    }) {
                        Text("▼", fontSize = 14.sp)
                    }
                }

                // Day selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (selectedDay < daysInMonth) selectedDay++ else selectedDay = 1
                    }) {
                        Text("▲", fontSize = 14.sp)
                    }
                    Text(
                        text = "روز ${JalaliCalendar.toPersianDigits(selectedDay.toString())}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = {
                        if (selectedDay > 1) selectedDay-- else selectedDay = daysInMonth
                    }) {
                        Text("▼", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("انصراف")
                    }
                    Button(
                        onClick = {
                            val finalDay = selectedDay.coerceAtMost(daysInMonth)
                            onDateSelected(JalaliDate(selectedYear, selectedMonth, finalDay))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("تأیید", color = Color.White)
                    }
                }
            }
        }
    }
}
