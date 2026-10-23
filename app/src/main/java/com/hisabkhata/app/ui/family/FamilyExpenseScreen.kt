package com.hisabkhata.app.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hisabkhata.app.data.local.AppLanguageManager
import com.hisabkhata.app.data.local.AppLanguageManager.str
import com.hisabkhata.app.data.local.AppLanguageManager.formatMoneyLocalized
import com.hisabkhata.app.data.local.AppLanguageManager.formatDigitsLocalized
import com.hisabkhata.app.data.local.AppLanguageManager.formatDateLocalized
import com.hisabkhata.app.data.local.entity.FamilyExpenseEntity
import com.hisabkhata.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Quick Expense categories with custom icons and bilingual names
data class FamilyCategoryInfo(
    val key: String,
    val bnName: String,
    val enName: String,
    val iconRes: Int
)

private val QuickExpenseCategories = listOf(
    FamilyCategoryInfo("মুদি-দোকান", "মুদি-দোকান", "Groceries", com.hisabkhata.app.R.drawable.ic_cat_groceries),
    FamilyCategoryInfo("মাছ", "মাছ", "Fish", com.hisabkhata.app.R.drawable.ic_cat_fish),
    FamilyCategoryInfo("সবজি", "সবজি", "Vegetables", com.hisabkhata.app.R.drawable.ic_cat_vegetable),
    FamilyCategoryInfo("বিদ্যুৎ বিল", "বিদ্যুৎ বিল", "Electricity Bill", com.hisabkhata.app.R.drawable.ic_cat_electric_bill),
    FamilyCategoryInfo("গ্যাস সিলিন্ডার", "গ্যাস সিলিন্ডার", "Gas Cylinder", com.hisabkhata.app.R.drawable.ic_cat_gas),
    FamilyCategoryInfo("ঔষুধ", "ঔষুধ", "Medicine", com.hisabkhata.app.R.drawable.ic_cat_medicine)
)

private fun getCategoryDrawableRes(note: String, category: String = ""): Int? {
    val text = (note + " " + category).lowercase()
    return when {
        text.contains("গ্যাস") || text.contains("gas") -> com.hisabkhata.app.R.drawable.ic_cat_gas
        text.contains("মাছ") || text.contains("fish") -> com.hisabkhata.app.R.drawable.ic_cat_fish
        text.contains("সবজি") || text.contains("vegetable") || text.contains("vege") -> com.hisabkhata.app.R.drawable.ic_cat_vegetable
        text.contains("মুদি") || text.contains("grocer") -> com.hisabkhata.app.R.drawable.ic_cat_groceries
        text.contains("বিদ্যুৎ") || text.contains("কারেন্ট") || text.contains("electric") || text.contains("current") -> com.hisabkhata.app.R.drawable.ic_cat_electric_bill
        text.contains("ঔষধ") || text.contains("ঔষুধ") || text.contains("মেডিসিন") || text.contains("medicin") || text.contains("pharmacy") -> com.hisabkhata.app.R.drawable.ic_cat_medicine
        else -> null
    }
}

private fun getCategoryDisplayName(rawNote: String): String {
    val trimmed = rawNote.trim()
    return when {
        trimmed.contains("গ্যাস সিলিন্ডার") || trimmed.contains("গ্যাস গিলিন্ডার") || trimmed.equals("Gas Cylinder", ignoreCase = true) -> str("গ্যাস সিলিন্ডার", "Gas Cylinder")
        trimmed.contains("মুদি-দোকান") || trimmed.equals("Groceries", ignoreCase = true) || trimmed.equals("Grocery", ignoreCase = true) -> str("মুদি-দোকান", "Groceries")
        trimmed.contains("মাছ") || trimmed.equals("Fish", ignoreCase = true) -> str("মাছ", "Fish")
        trimmed.contains("সবজি") || trimmed.equals("Vegetables", ignoreCase = true) || trimmed.equals("Vegetable", ignoreCase = true) -> str("সবজি", "Vegetables")
        trimmed.contains("বিদ্যুৎ বিল") || trimmed.equals("Electricity Bill", ignoreCase = true) -> str("বিদ্যুৎ বিল", "Electricity Bill")
        trimmed.contains("ঔষুধ") || trimmed.contains("ঔষধ") || trimmed.equals("Medicine", ignoreCase = true) -> str("ঔষুধ", "Medicine")
        trimmed.isBlank() -> str("পারিবারিক খরচ", "Family Expense")
        else -> trimmed
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyExpenseScreen(
    viewModel: FamilyExpenseViewModel = viewModel()
) {
    val currentLang by AppLanguageManager.currentLanguage.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<FamilyExpenseEntity?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(
                        text = str("+ খরচ যোগ", "+ Add Expense"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar with Full Family Expense Report Option on Top Right Corner
            val context = androidx.compose.ui.platform.LocalContext.current
            FamilyHeaderSection(
                onReportClick = {
                    val reportText = generateFamilyExpenseReportText(
                        expenses = expenses,
                        totalExpense = totalExpense ?: 0.0,
                        monthlyBudget = monthlyBudget
                    )
                    com.hisabkhata.app.ui.components.shareReportText(
                        context = context,
                        text = reportText,
                        title = str("ফ্যামিলি খরচের পূর্ণাঙ্গ রিপোর্ট শেয়ার", "Share Full Family Expense Report")
                    )
                }
            )

            // Fluid Glass Summary Hero Card with Monthly Budget
            FamilySummaryCard(
                totalAmount = totalExpense ?: 0.0,
                monthlyBudget = monthlyBudget,
                entryCount = expenses.size,
                onSetBudgetClick = { showBudgetDialog = true }
            )

            Spacer(modifier = Modifier.height(6.dp))

            val groupedExpenses = remember(expenses) {
                val sdfKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                expenses.groupBy { sdfKey.format(java.util.Date(it.date)) }
            }
            var collapsedDates by remember { mutableStateOf(setOf<String>()) }

            // Expenses List
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = PrimaryRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = str("কোনো খরচের হিসাব পাওয়া যায়নি", "No expense records found"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHead
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = str("নিচের '+ খরচ যোগ' বাটনে চাপ দিয়ে এন্ট্রি করুন", "Tap '+ Add Expense' below to add a record"),
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    groupedExpenses.forEach { (dateKey, dateExpenses) ->
                        val isCollapsed = collapsedDates.contains(dateKey)
                        val dayTotal = dateExpenses.sumOf { it.amount }
                        val firstDateMillis = dateExpenses.firstOrNull()?.date ?: 0L

                        item(key = "header_$dateKey") {
                            CollapsibleDateHeaderCard(
                                dateMillis = firstDateMillis,
                                count = dateExpenses.size,
                                summaryText = formatMoneyLocalized(dayTotal),
                                summaryColor = PrimaryRed,
                                isCollapsed = isCollapsed,
                                onToggle = {
                                    collapsedDates = if (isCollapsed) collapsedDates - dateKey else collapsedDates + dateKey
                                }
                            )
                        }

                        if (!isCollapsed) {
                            items(dateExpenses, key = { it.id }) { item ->
                                FamilyExpenseItemCard(
                                    item = item,
                                    onEdit = { expenseToEdit = item },
                                    onDelete = { viewModel.deleteExpense(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Smooth Full Screen Add Expense Dialog (Without Delay, Zero Categories, '/' Quick Menu)
    if (showAddDialog) {
        AddFamilyExpenseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { amount, note, date ->
                viewModel.addExpense(amount, note, date) {
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Family Expense Dialog
    expenseToEdit?.let { expense ->
        AddFamilyExpenseDialog(
            initialAmount = expense.amount,
            initialNote = expense.note,
            initialDate = expense.date,
            isEditing = true,
            onDismiss = { expenseToEdit = null },
            onSave = { amount, note, date ->
                viewModel.updateExpense(expense.id, amount, note, date) {
                    expenseToEdit = null
                }
            }
        )
    }

    // Set Monthly Budget Dialog with Cancel, Reset, Save
    if (showBudgetDialog) {
        SetMonthlyBudgetDialog(
            currentBudget = monthlyBudget,
            onDismiss = { showBudgetDialog = false },
            onReset = {
                viewModel.resetMonthlyBudget()
                showBudgetDialog = false
            },
            onSave = { newBudget ->
                viewModel.setMonthlyBudget(newBudget)
                showBudgetDialog = false
            }
        )
    }
}

@Composable
private fun FamilyHeaderSection(
    onReportClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = str("ফ্যামিলি খরচ", "Family Expense"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextHead
            )
            Text(
                text = str("দৈনন্দিন ও মাসিক পারিবারিক বাজেট ট্র্যাকার", "Daily & monthly family budget tracker"),
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        // Top Right Corner: Full Family Expense Report Button
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onReportClick),
            color = Color(0xFFFEF2F2),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = str("পূর্ণাঙ্গ রিপোর্ট", "Full Report"),
                    tint = PrimaryRed,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = str("রিপোর্ট", "Report"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed
                )
            }
        }
    }
}

/**
 * 🌟 ফ্যামিলি খরচের পূর্ণাঙ্গ স্টেটমেন্ট ও রিপোর্ট জেনারেটর
 */
fun generateFamilyExpenseReportText(
    expenses: List<FamilyExpenseEntity>,
    totalExpense: Double,
    monthlyBudget: Double
): String {
    val isEn = AppLanguageManager.isEnglish()
    val sb = StringBuilder()
    sb.append(if (isEn) "📋 Family Expense Statement\n" else "📋 পারিবারিক খরচের বিবরণী\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("${if (isEn) "💰 Total Expense: " else "💰 মোট খরচ: "}${formatMoneyLocalized(totalExpense)}\n")
    sb.append("${if (isEn) "📝 Total Entries: " else "📝 মোট এন্ট্রি: "}${formatDigitsLocalized(expenses.size.toString())}${if (isEn) "" else " টি"}\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━\n")

    if (expenses.isNotEmpty()) {
        sb.append(if (isEn) "📅 Detailed Records:\n\n" else "📅 বিস্তারিত হিসাব:\n\n")
        
        fun getCategoryEmoji(rawNote: String): String {
            val trimmed = rawNote.trim().lowercase()
            return when {
                trimmed.contains("সবজি") || trimmed.contains("vegetable") || trimmed.contains("vege") -> "🛒"
                trimmed.contains("মাছ") || trimmed.contains("fish") -> "🐟"
                trimmed.contains("মুদি") || trimmed.contains("grocer") -> "🛍️"
                trimmed.contains("বিদ্যুৎ") || trimmed.contains("electric") || trimmed.contains("current") -> "⚡"
                trimmed.contains("গ্যাস") || trimmed.contains("gas") -> "🔥"
                trimmed.contains("ঔষধ") || trimmed.contains("ঔষুধ") || trimmed.contains("medicine") || trimmed.contains("pharmacy") -> "💊"
                else -> "📌"
            }
        }

        fun formatReportDateTime(millis: Long): String {
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val hour24 = cal.get(Calendar.HOUR_OF_DAY)
            val hour12 = cal.get(Calendar.HOUR)
            val min = cal.get(Calendar.MINUTE)
            val displayHour = if (hour12 == 0) 12 else hour12

            if (isEn) {
                val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
                val sdfMonth = SimpleDateFormat("MMM", Locale.ENGLISH).format(Date(millis))
                val hourStr = if (displayHour < 10) "0$displayHour" else "$displayHour"
                val minStr = if (min < 10) "0$min" else "$min"
                return "$day $sdfMonth, $hourStr:$minStr $amPm"
            }

            val shortBanglaMonths = arrayOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")
            val monthName = shortBanglaMonths.getOrElse(month) { "" }
            val period = when (hour24) {
                in 4..5 -> "ভোর"
                in 6..11 -> "সকাল"
                in 12..14 -> "দুপুর"
                in 15..17 -> "বিকাল"
                in 18..19 -> "সন্ধ্যা"
                else -> "রাত"
            }
            val hourStr = if (displayHour < 10) "0$displayHour" else "$displayHour"
            val minStr = if (min < 10) "0$min" else "$min"
            val dayFormatted = com.hisabkhata.app.ui.components.toBanglaDigits(day.toString())
            val timeFormatted = com.hisabkhata.app.ui.components.toBanglaDigits("$hourStr:$minStr")
            return "$dayFormatted $monthName, $period $timeFormatted"
        }

        val sortedList = expenses.sortedByDescending { it.date }
        sortedList.forEachIndexed { index, exp ->
            val emoji = getCategoryEmoji(exp.note)
            val noteDisplay = getCategoryDisplayName(exp.note)
            val timeStr = formatReportDateTime(exp.date)
            sb.append("$emoji $noteDisplay: ${formatMoneyLocalized(exp.amount)}\n")
            sb.append("🕒 $timeStr\n")
            if (index < sortedList.size - 1) {
                sb.append("\n")
            }
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    }
    sb.append(if (isEn) "📱 Generated by Hisab Khata App" else "📱 হিসাব খাতা অ্যাপ দ্বারা প্রস্তুতকৃত")
    return sb.toString()
}

@Composable
private fun FamilySummaryCard(
    totalAmount: Double,
    monthlyBudget: Double,
    entryCount: Int,
    onSetBudgetClick: () -> Unit
) {
    val hasBudget = monthlyBudget > 0.0
    val remaining = if (hasBudget) monthlyBudget - totalAmount else 0.0
    val progressFraction = if (hasBudget && monthlyBudget > 0) {
        (totalAmount / monthlyBudget).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    val progressColor = when {
        !hasBudget -> PrimaryRed
        totalAmount > monthlyBudget -> PrimaryRed
        progressFraction > 0.8f -> Color(0xFFF59E0B) // Amber warning
        else -> PrimaryGreen
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x18000000))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFF1F2), Color(0xFFFFFFFF))
                )
            )
            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Top Row: Expense & Budget Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = str("মোট পারিবারিক খরচ", "Total Family Expense"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    Text(
                        text = formatMoneyLocalized(totalAmount),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed
                    )
                    Text(
                        text = "${str("মোট", "Total")} ${formatDigitsLocalized(entryCount.toString())} ${str("টি এন্ট্রি সংরক্ষিত", "entries recorded")}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                // Monthly Budget Trigger Chip / Button
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFE4E6))
                            .clickable { onSetBudgetClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = PrimaryRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (hasBudget) str("বাজেট পরিবর্তন", "Edit Budget") else str("+ বাজেট সেট", "+ Set Budget"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRed
                            )
                        }
                    }

                    if (hasBudget) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${str("বাজেট:", "Budget:")} ${formatMoneyLocalized(monthlyBudget)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHead
                        )
                    }
                }
            }

            // Budget Progress & Remaining Row (when monthly budget is configured)
            if (hasBudget) {
                HorizontalDivider(color = Color(0xFFFEE2E2), thickness = 0.5.dp)

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (remaining >= 0) str("অবশিষ্ট বাজেট:", "Remaining Budget:") else str("বাজেট অতিরিক্ত (ঘাটতি):", "Over Budget (Deficit):"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (remaining >= 0) TextMuted else PrimaryRed
                        )
                        Text(
                            text = if (remaining >= 0) formatMoneyLocalized(remaining) else "-" + formatMoneyLocalized(-remaining),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remaining >= 0) PrimaryGreenDark else PrimaryRed
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = progressColor,
                        trackColor = Color(0xFFFEE2E2)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${str("ব্যয় হয়েছে:", "Used:")} ${(progressFraction * 100).toInt()}%",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        if (totalAmount > monthlyBudget) {
                            Text(
                                text = str("সীমা অতিক্রম করেছে!", "Budget Exceeded!"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyExpenseItemCard(
    item: FamilyExpenseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderLight))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val catIconRes = getCategoryDrawableRes(item.note, item.category)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    if (catIconRes != null) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = catIconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    val titleText = getCategoryDisplayName(item.note)
                    Text(
                        text = titleText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextHead
                    )
                    Text(
                        text = formatDateLocalized(item.date),
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "-" + formatMoneyLocalized(item.amount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed
                )

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = com.hisabkhata.app.R.drawable.ic_custom_edit),
                        contentDescription = str("সম্পাদনা", "Edit"),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = com.hisabkhata.app.R.drawable.ic_custom_delete),
                        contentDescription = str("মুছুন", "Delete"),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full-Screen Smooth Add Family Expense Dialog (Zero Categories, Zero Delay, Slash '/' Quick Items)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddFamilyExpenseDialog(
    initialAmount: Double? = null,
    initialNote: String = "",
    initialDate: Long? = null,
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (amount: Double, note: String, date: Long) -> Unit
) {
    var amountText by remember { mutableStateOf(if (initialAmount != null && initialAmount > 0) (if (initialAmount % 1.0 == 0.0) initialAmount.toLong().toString() else initialAmount.toString()) else "") }
    var noteText by remember { mutableStateOf(initialNote) }
    var amountError by remember { mutableStateOf(false) }

    fun applyCategory(cat: FamilyCategoryInfo) {
        // Single selection: Direct assignment, no chaining or multiple entries
        noteText = str(cat.bnName, cat.enName)
    }

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
                            Text(
                                text = if (isEditing) str("খরচ সম্পাদনা", "Edit Expense") else str("+ নতুন খরচ যোগ", "+ Add Expense"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHead
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = str("বন্ধ করুন", "Close"),
                                    tint = TextHead,
                                    modifier = Modifier.size(20.dp)
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
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Concept 1: FinTech Hero Amount Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (amountError) PrimaryRed else BorderHairline))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = str("টাকার পরিমাণ (টাকা) *", "Amount (৳) *"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (amountError) PrimaryRed else TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "৳",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                androidx.compose.foundation.text.BasicTextField(
                                    value = amountText,
                                    onValueChange = {
                                        amountText = it
                                        if (it.isNotBlank()) amountError = false
                                    },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHead
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (amountText.isEmpty()) {
                                            Text(
                                                text = "0",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextMuted.copy(alpha = 0.4f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }

                    // Expense Note Field with '/' Quick Trigger Button
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderHairline))
                    ) {
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text(str("খরচের বিবরণ / নোট (ঐচ্ছিক)", "Expense Note (Optional)"), fontSize = 11.sp) },
                            placeholder = {
                                Text(
                                    text = str("যেমন: মাছ, মুদি, সবজি", "e.g. Fish, Grocery, Vegetables"),
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // One-Tap Quick Categories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = str("⚡ দ্রুত ক্যাটেগরি নির্বাচন:", "⚡ One-Tap Categories:"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHead
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        QuickExpenseCategories.forEach { cat ->
                            val isSelected = noteText.trim().equals(str(cat.bnName, cat.enName), ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFFFEE2E2) else SurfaceCard)
                                    .border(
                                        width = 0.8.dp,
                                        color = if (isSelected) PrimaryRed else BorderLight,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { applyCategory(cat) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = cat.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = str(cat.bnName, cat.enName),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PrimaryRed else TextHead
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 🌟 Save Expense Button placed directly underneath the category options
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                amountError = true
                            } else {
                                val cleanNote = noteText.replace(Regex("/+$"), "").trim()
                                onSave(amt, cleanNote, initialDate ?: System.currentTimeMillis())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isEditing) str("✓ আপডেট সংরক্ষণ করুন", "✓ Update Expense") else str("✓ খরচ সংরক্ষণ করুন", "✓ Save Expense"),
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

/**
 * Monthly Budget Configuration Dialog with Cancel, Reset, and Save
 */
@Composable
private fun SetMonthlyBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onSave: (Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toInt().toString() else "") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = str("মাসিক বাজেট সেট করুন", "Set Monthly Budget"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextHead
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = str("প্রতি মাসের পারিবারিক খরচের সর্বোচ্চ বাজেট নির্ধারণ করুন:", "Set maximum monthly budget limit for family expenses:"),
                    fontSize = 11.sp,
                    color = TextMuted
                )

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = {
                        budgetText = it
                        errorText = null
                    },
                    label = { Text(str("বাজেট পরিমাণ (৳)", "Budget Amount (৳)"), fontSize = 11.sp) },
                    placeholder = { Text("যেমন: 20000", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    isError = errorText != null,
                    supportingText = if (errorText != null) {
                        { Text(errorText ?: "", color = PrimaryRed, fontSize = 9.sp) }
                    } else null
                )

                if (currentBudget > 0) {
                    Text(
                        text = "${str("বর্তমান বাজেট:", "Current Budget:")} ${formatMoneyLocalized(currentBudget)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreenDark
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = str("Cancel", "Cancel"),
                        fontSize = 11.sp,
                        color = TextHead
                    )
                }

                // Reset
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = str("Reset", "Reset"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryRed
                    )
                }

                // Save
                Button(
                    onClick = {
                        val budget = budgetText.toDoubleOrNull()
                        if (budget == null || budget < 0) {
                            errorText = str("সঠিক বাজেট পরিমাণ লিখুন", "Enter a valid budget amount")
                        } else {
                            onSave(budget)
                        }
                    },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = str("Save", "Save"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    )
}

@Composable
private fun CollapsibleDateHeaderCard(
    dateMillis: Long,
    count: Int,
    summaryText: String,
    summaryColor: Color,
    isCollapsed: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderLight))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isCollapsed) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = AppLanguageManager.formatDateSimpleLocalized(dateMillis),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHead
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${formatDigitsLocalized(count.toString())} ${str("টি", "items")}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = summaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = summaryColor
            )
        }
    }
}
