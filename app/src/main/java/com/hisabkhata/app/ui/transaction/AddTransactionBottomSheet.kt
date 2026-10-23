package com.hisabkhata.app.ui.transaction

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.hisabkhata.app.data.local.AppLanguageManager
import com.hisabkhata.app.data.local.AppLanguageManager.str
import com.hisabkhata.app.data.local.AppLanguageManager.formatMoneyLocalized
import com.hisabkhata.app.data.local.AppLanguageManager.formatDateSimpleLocalized
import com.hisabkhata.app.data.local.entity.TransactionEntity
import com.hisabkhata.app.ui.theme.*
import java.util.Calendar

/**
 * Instant Full-Screen Transaction Entry Dialog with 'পেলাম' & 'দিলাম' at the TOP (Zero Delay)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    customerName: String,
    currentBalance: Double = 0.0,
    initialType: String = TransactionEntity.TYPE_DEBIT,
    initialAmount: Double? = null,
    initialCurrency: String = TransactionEntity.CURRENCY_BDT,
    initialNote: String = "",
    initialDate: Long? = null,
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Double, note: String, date: Long, currency: String) -> Unit
) {
    val context = LocalContext.current
    val currentLang by AppLanguageManager.currentLanguage.collectAsStateWithLifecycle()

    var selectedType by remember { mutableStateOf(initialType) }
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var amountText by remember {
        mutableStateOf(
            if (initialAmount != null && initialAmount > 0) {
                if (initialAmount % 1.0 == 0.0) initialAmount.toLong().toString() else initialAmount.toString()
            } else ""
        )
    }
    var noteText by remember { mutableStateOf(initialNote) }
    var amountError by remember { mutableStateOf(false) }

    // Date Picker state
    val calendar = remember { Calendar.getInstance() }
    var selectedDateMillis by remember { mutableStateOf(initialDate ?: System.currentTimeMillis()) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedDateMillis = newCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val isPelam = selectedType == TransactionEntity.TYPE_CREDIT

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogWindow = (androidx.compose.ui.platform.LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.setWindowAnimations(com.hisabkhata.app.R.style.DialogSlideAnimation)
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SurfaceCanvas
        ) {
            Scaffold(
                containerColor = SurfaceCanvas,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val initial = customerName.trim().take(1).uppercase().ifEmpty { "K" }
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isPelam) PrimaryGreen.copy(alpha = 0.15f) else PrimaryRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial,
                                        color = if (isPelam) PrimaryGreen else PrimaryRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = customerName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHead
                                    )
                                    if (isEditing) {
                                        Text(
                                            text = "(${str("সম্পাদনা", "Edit")})",
                                            fontSize = 11.sp,
                                            color = PrimaryRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = str("পেছনে", "Back"),
                                    tint = TextHead,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard),
                        windowInsets = WindowInsets.statusBars
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Segmented Type Selector: [ ✓ পেলাম (+) | দিলাম (-) ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfacePill)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // পেলাম (+) Tab (Credit)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPelam) PrimaryGreen else Color.Transparent)
                                .clickable { selectedType = TransactionEntity.TYPE_CREDIT }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (isPelam) {
                                    Text("✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = str("পেলাম (+)", "Got / Received (+)"),
                                    fontSize = 13.sp,
                                    fontWeight = if (isPelam) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPelam) Color.White else TextMuted
                                )
                            }
                        }

                        // দিলাম (-) Tab (Debit)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isPelam) PrimaryRed else Color.Transparent)
                                .clickable { selectedType = TransactionEntity.TYPE_DEBIT }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!isPelam) {
                                    Text("✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = str("দিলাম (-)", "Gave / Sale (-)"),
                                    fontSize = 13.sp,
                                    fontWeight = if (!isPelam) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isPelam) Color.White else TextMuted
                                )
                            }
                        }
                    }

                    // Card 1: Currency & Amount Hero Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderHairline))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = str("টাকার পরিমাণ", "Amount"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHead
                                )

                                // Currency Toggle Chips
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfacePill)
                                        .padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    val isBdt = selectedCurrency == TransactionEntity.CURRENCY_BDT
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isBdt) (if (isPelam) PrimaryGreen else PrimaryRed) else Color.Transparent)
                                            .clickable { selectedCurrency = TransactionEntity.CURRENCY_BDT }
                                            .padding(horizontal = 10.dp, vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "৳ BDT",
                                            fontSize = 11.sp,
                                            fontWeight = if (isBdt) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isBdt) Color.White else TextMuted
                                        )
                                    }

                                    val isUsd = selectedCurrency == TransactionEntity.CURRENCY_USD
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isUsd) (if (isPelam) PrimaryGreen else PrimaryRed) else Color.Transparent)
                                            .clickable { selectedCurrency = TransactionEntity.CURRENCY_USD }
                                            .padding(horizontal = 10.dp, vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$ USD",
                                            fontSize = 11.sp,
                                            fontWeight = if (isUsd) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isUsd) Color.White else TextMuted
                                        )
                                    }
                                }
                            }

                            val currSymbol = if (selectedCurrency == TransactionEntity.CURRENCY_USD) "$" else "৳"
                            val activeThemeColor = if (isPelam) PrimaryGreen else PrimaryRed

                            // Hero Amount Input Field
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = {
                                    amountText = it
                                    if (it.isNotBlank()) amountError = false
                                },
                                placeholder = {
                                    Text(
                                        "0",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted.copy(alpha = 0.4f)
                                    )
                                },
                                leadingIcon = {
                                    Text(
                                        text = currSymbol,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = activeThemeColor,
                                        modifier = Modifier.padding(start = 10.dp, end = 2.dp)
                                    )
                                },
                                isError = amountError,
                                supportingText = if (amountError) {
                                    { Text(str("সঠিক টাকার পরিমাণ লিখুন", "Enter a valid amount"), color = PrimaryRed, fontSize = 9.sp) }
                                } else null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextHead
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = activeThemeColor,
                                    unfocusedBorderColor = BorderLight
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Card 2: Note & Date Picker
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderHairline))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Note Input
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text(str("নোট (ঐচ্ছিক)", "Note (Optional)"), fontSize = 11.sp) },
                                placeholder = {
                                    Text(
                                        text = str("যেমন: নগদ জমা, চালান", "e.g. Cash, invoice"),
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Date Picker Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(0.5.dp, BorderHairline, RoundedCornerShape(8.dp))
                                    .clickable { datePickerDialog.show() }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                    Text(str("তারিখ:", "Date:"), fontSize = 11.sp, color = TextMuted)
                                    Text(formatDateSimpleLocalized(selectedDateMillis), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextHead)
                                }
                                Text(str("পরিবর্তন", "Change"), fontSize = 10.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 🌟 Save Transaction Button placed directly below the options
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                amountError = true
                            } else {
                                onSave(selectedType, amt, noteText.trim(), selectedDateMillis, selectedCurrency)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPelam) PrimaryGreen else PrimaryRed
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isEditing) str("✓ আপডেট সংরক্ষণ করুন", "✓ Update Transaction") else str("✓ লেনদেন সংরক্ষণ করুন", "✓ Save Transaction"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
