package com.hisabkhata.app.ui.cashbook

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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.hisabkhata.app.data.local.AppLanguageManager
import com.hisabkhata.app.data.local.AppLanguageManager.str
import com.hisabkhata.app.data.local.AppLanguageManager.formatMoneyLocalized
import com.hisabkhata.app.data.local.AppLanguageManager.formatDigitsLocalized
import com.hisabkhata.app.data.local.AppLanguageManager.formatDateLocalized
import com.hisabkhata.app.data.local.entity.CashBookEntity
import com.hisabkhata.app.ui.theme.*

@Composable
fun IncomeExpenseScreen(
    viewModel: CashBookViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentLang by AppLanguageManager.currentLanguage.collectAsStateWithLifecycle()
    val entries by viewModel.filteredEntries.collectAsStateWithLifecycle()
    val allEntriesList by viewModel.allEntries.collectAsStateWithLifecycle()
    val cashSummary by viewModel.cashSummary.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()

    var showAddDialogForType by remember { mutableStateOf<String?>(null) }
    var entryToEdit by remember { mutableStateOf<CashBookEntity?>(null) }

    fun shareCashBookReport() {
        val isBn = currentLang == "bn"
        val reportText = buildString {
            appendLine(if (isBn) "📊 ক্যাশ বুক রিপোর্ট" else "📊 CASH BOOK REPORT")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            val sdfTime = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.US)
            val generatedDateStr = sdfTime.format(java.util.Date())
            appendLine("${if (isBn) "রিপোর্ট তৈরির সময়:" else "Generated:"} ${if (isBn) formatDigitsLocalized(generatedDateStr) else generatedDateStr}")
            
            appendLine(if (isBn) "💵 অ্যাকাউন্ট সারাংশ (BDT)" else "💵 ACCOUNT SUMMARY (BDT)")
            val netBdtStr = String.format(java.util.Locale.US, "%.2f", cashSummary.netCashBdt)
            val inBdtStr = String.format(java.util.Locale.US, "%.2f", cashSummary.totalIncomeBdt)
            val outBdtStr = String.format(java.util.Locale.US, "%.2f", cashSummary.totalExpenseBdt)
            appendLine("▪️ ${if (isBn) "নেট ব্যালেন্স:" else "Net Balance:"} ৳ ${if (isBn) formatDigitsLocalized(netBdtStr) else netBdtStr}")
            appendLine("▪️ ${if (isBn) "মোট জমা:" else "Total In:"} ৳ ${if (isBn) formatDigitsLocalized(inBdtStr) else inBdtStr}")
            appendLine("▪️ ${if (isBn) "মোট খরচ:" else "Total Out:"} ৳ ${if (isBn) formatDigitsLocalized(outBdtStr) else outBdtStr}")

            if (cashSummary.hasUsd) {
                appendLine("━━━━━━━━━━━━━━━━━━━━")
                appendLine(if (isBn) "💵 অ্যাকাউন্ট সারাংশ (USD)" else "💵 ACCOUNT SUMMARY (USD)")
                val netUsdStr = String.format(java.util.Locale.US, "%.2f", cashSummary.netCashUsd)
                val inUsdStr = String.format(java.util.Locale.US, "%.2f", cashSummary.totalIncomeUsd)
                val outUsdStr = String.format(java.util.Locale.US, "%.2f", cashSummary.totalExpenseUsd)
                appendLine("▪️ ${if (isBn) "নেট ব্যালেন্স:" else "Net Balance:"} $ ${if (isBn) formatDigitsLocalized(netUsdStr) else netUsdStr}")
                appendLine("▪️ ${if (isBn) "মোট জমা:" else "Total In:"} $ ${if (isBn) formatDigitsLocalized(inUsdStr) else inUsdStr}")
                appendLine("▪️ ${if (isBn) "মোট খরচ:" else "Total Out:"} $ ${if (isBn) formatDigitsLocalized(outUsdStr) else outUsdStr}")
            }

            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine(if (isBn) "📝 বিস্তারিত লেনদেন" else "📝 DETAILED TRANSACTIONS")
            if (allEntriesList.isEmpty()) {
                appendLine(if (isBn) "কোনো এন্ট্রি পাওয়া যায়নি।" else "No entries found.")
            } else {
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
                allEntriesList.sortedByDescending { it.date }.forEach { item ->
                    val dateFormatted = dateFormat.format(java.util.Date(item.date))
                    val dateStr = if (isBn) formatDigitsLocalized(dateFormatted) else dateFormatted
                    val isIncome = item.type == "INCOME"
                    val icon = if (isIncome) "✅ +" else "🔻 -"
                    val currSymbol = if (item.currency == "USD") "$" else "৳"
                    val amtFormatted = String.format(java.util.Locale.US, "%.2f", item.amount)
                    val amtStr = if (isBn) formatDigitsLocalized(amtFormatted) else amtFormatted
                    val noteStr = if (item.note.isNotBlank()) " - ${item.note}" else ""
                    appendLine("$icon $currSymbol $amtStr$noteStr [$dateStr]")
                }
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine(if (isBn) "📱 হিসাব খাতা অ্যাপ" else "📱 Hisab Khata App")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, str("ক্যাশ খাতা রিপোর্ট শেয়ার করুন", "Share Cash Book Report"))
        context.startActivity(shareIntent)
    }

    Scaffold(
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Section - Tight & Clean with Right-Corner Report button
            CashBookHeaderSection(
                onReportClick = { shareCashBookReport() }
            )

            // Cash in Hand Summary Hero - Tight & Compact Multi-Currency
            CashBookSummaryHero(
                summary = cashSummary
            )

            // Primary Quick Action Buttons (+ জমা / - খরচ) - Snug
            ActionButtonsRow(
                onIncomeClick = { showAddDialogForType = "INCOME" },
                onExpenseClick = { showAddDialogForType = "EXPENSE" }
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Filter Tabs (সকল, জমা, খরচ) - Tight
            CashBookFilterTabs(
                currentFilter = filterType,
                onFilterSelect = { viewModel.setFilterType(it) }
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Entries List
            val groupedEntries = remember(entries) {
                val sdfKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                entries.groupBy { sdfKey.format(java.util.Date(it.date)) }
            }
            var collapsedDates by remember { mutableStateOf(setOf<String>()) }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = TextHead,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = str("কোনো আয়-ব্যয়ের হিসাব পাওয়া যায়নি", "No cash book records found"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHead
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = str("উপরে '+ জমা / আয়' অথবা '- খরচ / ব্যয়' বাটনে চাপ দিয়ে এন্ট্রি করুন", "Tap '+ Cash In' or '- Cash Out' above to add an entry"),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    groupedEntries.forEach { (dateKey, dateEntries) ->
                        val isCollapsed = collapsedDates.contains(dateKey)
                        val hasUsdEntries = dateEntries.any { it.currency == "USD" }
                        val dayBdtEntries = dateEntries.filter { it.currency != "USD" }
                        val dayBdtIn = dayBdtEntries.filter { it.type == "INCOME" }.sumOf { it.amount }
                        val dayBdtOut = dayBdtEntries.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                        val dayNetBdt = dayBdtIn - dayBdtOut

                        val dayUsdEntries = dateEntries.filter { it.currency == "USD" }
                        val dayUsdIn = dayUsdEntries.filter { it.type == "INCOME" }.sumOf { it.amount }
                        val dayUsdOut = dayUsdEntries.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                        val dayNetUsd = dayUsdIn - dayUsdOut

                        val firstDateMillis = dateEntries.firstOrNull()?.date ?: 0L

                        val summaryText = if (!hasUsdEntries) {
                            if (dayNetBdt >= 0) "+${AppLanguageManager.formatCurrencyLocalized(dayNetBdt, "BDT")}" else "-${AppLanguageManager.formatCurrencyLocalized(kotlin.math.abs(dayNetBdt), "BDT")}"
                        } else {
                            val bdtStr = if (dayNetBdt >= 0) "+${AppLanguageManager.formatCurrencyLocalized(dayNetBdt, "BDT")}" else "-${AppLanguageManager.formatCurrencyLocalized(kotlin.math.abs(dayNetBdt), "BDT")}"
                            val usdStr = if (dayNetUsd >= 0) "+${AppLanguageManager.formatCurrencyLocalized(dayNetUsd, "USD")}" else "-${AppLanguageManager.formatCurrencyLocalized(kotlin.math.abs(dayNetUsd), "USD")}"
                            "$bdtStr • $usdStr"
                        }

                        item(key = "header_$dateKey") {
                            CollapsibleCashDateHeaderCard(
                                dateMillis = firstDateMillis,
                                count = dateEntries.size,
                                summaryText = summaryText,
                                summaryColor = if (dayNetBdt >= 0) PrimaryGreen else PrimaryRed,
                                isCollapsed = isCollapsed,
                                onToggle = {
                                    collapsedDates = if (isCollapsed) collapsedDates - dateKey else collapsedDates + dateKey
                                }
                            )
                        }

                        if (!isCollapsed) {
                            items(dateEntries, key = { it.id }) { item ->
                                CashBookItemCard(
                                    item = item,
                                    onEdit = { entryToEdit = item },
                                    onDelete = { viewModel.deleteEntry(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Fully open, smooth instant full-screen dialog without delay
    showAddDialogForType?.let { type ->
        AddCashBookDialog(
            initialType = type,
            onDismiss = { showAddDialogForType = null },
            onSave = { entryType, amount, note, date, currency ->
                viewModel.addEntry(entryType, amount, note, date, currency) {
                    showAddDialogForType = null
                }
            }
        )
    }

    // Edit Cash Book Entry Dialog
    entryToEdit?.let { entry ->
        AddCashBookDialog(
            initialType = entry.type,
            initialAmount = entry.amount,
            initialNote = entry.note,
            initialDate = entry.date,
            initialCurrency = entry.currency,
            isEditing = true,
            onDismiss = { entryToEdit = null },
            onSave = { entryType, amount, note, date, currency ->
                viewModel.updateEntry(entry.id, entryType, amount, note, date, currency) {
                    entryToEdit = null
                }
            }
        )
    }
}

@Composable
private fun CashBookHeaderSection(
    onReportClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = str("আয়-ব্যয় হিসাব", "Cash Book"),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextHead
            )
            Text(
                text = str("দোকান ও ব্যবসার ক্যাশ খাতা", "Store & business cash register"),
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        
        // Right Corner Report Option Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFECFDF5))
                .border(0.8.dp, PrimaryGreen.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .clickable(onClick = onReportClick)
                .padding(horizontal = 9.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = str("রিপোর্ট", "Report"),
                    tint = PrimaryGreen,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = str("রিপোর্ট", "Report"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreenDark
                )
            }
        }
    }
}

@Composable
private fun CashBookSummaryHero(
    summary: CashSummaryState
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x14000000))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFF0FDF4), Color(0xFFFFFFFF))
                )
            )
            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = str("হাতে অবশিষ্ট ক্যাশ (Net Cash)", "Cash in Hand (Net Cash)"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    Text(
                        text = AppLanguageManager.formatCurrencyLocalized(summary.netCashBdt, "BDT"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.netCashBdt >= 0) PrimaryGreen else PrimaryRed
                    )
                }

                if (summary.hasUsd) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = str("USD অবশিষ্ট ক্যাশ", "USD Net Cash"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                        Text(
                            text = AppLanguageManager.formatCurrencyLocalized(summary.netCashUsd, "USD"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.netCashUsd >= 0) PrimaryGreen else PrimaryRed
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFDCFCE7), thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${str("মোট জমা:", "In:")}", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "+" + AppLanguageManager.formatCurrencyLocalized(summary.totalIncomeBdt, "BDT"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    if (summary.hasUsd) {
                        Text(
                            text = "(+${AppLanguageManager.formatCurrencyLocalized(summary.totalIncomeUsd, "USD")})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryGreen
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${str("মোট খরচ:", "Out:")}", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "-" + AppLanguageManager.formatCurrencyLocalized(summary.totalExpenseBdt, "BDT"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed
                    )
                    if (summary.hasUsd) {
                        Text(
                            text = "(-${AppLanguageManager.formatCurrencyLocalized(summary.totalExpenseUsd, "USD")})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Income Button (+ জমা)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryGreen)
                .clickable(onClick = onIncomeClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text(
                    text = str("+ জমা / আয়", "+ Cash In"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Expense Button (- খরচ)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryRed)
                .clickable(onClick = onExpenseClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Default.RemoveCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text(
                    text = str("- খরচ / ব্যয়", "- Cash Out"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CashBookFilterTabs(
    currentFilter: String,
    onFilterSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(
            "ALL" to str("সকল", "All"),
            "INCOME" to str("জমা / আয়", "Cash In"),
            "EXPENSE" to str("খরচ / ব্যয়", "Cash Out")
        ).forEach { (key, label) ->
            val isSelected = currentFilter == key
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) SurfaceCard else Color.Transparent)
                    .border(
                        0.5.dp,
                        if (isSelected) PrimaryRed else BorderLight,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onFilterSelect(key) }
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) PrimaryRed else TextMuted
                )
            }
        }
    }
}

@Composable
private fun CashBookItemCard(
    item: CashBookEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = item.type == "INCOME"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderLight))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isIncome) Color(0xFFECFDF5) else Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIncome) PrimaryGreen else PrimaryRed,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    val titleText = if (item.note.isNotBlank()) {
                        item.note
                    } else {
                        if (isIncome) str("নগদ জমা / আয়", "Cash In") else str("নগদ খরচ / ব্যয়", "Cash Out")
                    }

                    Text(
                        text = titleText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextHead,
                        maxLines = 1
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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = (if (isIncome) "+" else "-") + AppLanguageManager.formatCurrencyLocalized(item.amount, item.currency),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) PrimaryGreen else PrimaryRed
                )

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        painter = painterResource(id = com.hisabkhata.app.R.drawable.ic_custom_edit),
                        contentDescription = str("সম্পাদনা", "Edit"),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        painter = painterResource(id = com.hisabkhata.app.R.drawable.ic_custom_delete),
                        contentDescription = str("মুছুন", "Delete"),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Fully Open, Zero-Delay Instant Full-Screen Cash Book Entry Dialog
 * (Ultra Tight, Short Single-Line Inputs, Zero Category, Zero Payment Method)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCashBookDialog(
    initialType: String,
    initialAmount: Double? = null,
    initialNote: String = "",
    initialDate: Long? = null,
    initialCurrency: String = "BDT",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Double, note: String, date: Long, currency: String) -> Unit
) {
    var entryType by remember { mutableStateOf(initialType) }
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

    val isIncome = entryType == "INCOME"

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
                            Column {
                                Text(
                                    text = if (isEditing) {
                                        if (isIncome) str("জমা / আয় সম্পাদনা", "Edit Cash In")
                                        else str("খরচ / ব্যয় সম্পাদনা", "Edit Cash Out")
                                    } else {
                                        if (isIncome) str("+ জমা / আয় এন্ট্রি", "+ Cash In Entry")
                                        else str("- খরচ / ব্যয় এন্ট্রি", "- Cash Out Entry")
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHead
                                )
                                Text(
                                    text = if (isEditing) str("ক্যাশ হিসাবের তথ্য আপডেট করুন", "Update Cash Record")
                                           else if (isIncome) str("ব্যবসায়ের ক্যাশ জমা রেকর্ড", "Record Cash In")
                                           else str("ব্যবসায়ের ক্যাশ খরচ রেকর্ড", "Record Cash Out"),
                                    fontSize = 10.sp,
                                    color = if (isIncome) PrimaryGreen else PrimaryRed
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = str("বন্ধ করুন", "Close"),
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
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type Selector Tabs (জমা / আয় vs খরচ / ব্যয়) - Ultra Smooth & Tight
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfacePill)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Income Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isIncome) PrimaryGreen else Color.Transparent)
                                .clickable { entryType = "INCOME" }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = str("+ জমা / আয়", "+ Cash In"),
                                fontSize = 11.sp,
                                fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                                color = if (isIncome) Color.White else TextMuted
                            )
                        }

                        // Expense Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isIncome) PrimaryRed else Color.Transparent)
                                .clickable { entryType = "EXPENSE" }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = str("- খরচ / ব্যয়", "- Cash Out"),
                                fontSize = 11.sp,
                                fontWeight = if (!isIncome) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isIncome) Color.White else TextMuted
                            )
                        }
                    }

                    // Concept 2: Compact Clean Unified Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (amountError) PrimaryRed else BorderLight))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Row: Amount Input + Currency Pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedCurrency == "USD") str("টাকার পরিমাণ ($) *", "Amount ($) *") else str("টাকার পরিমাণ (৳) *", "Amount (৳) *"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (amountError) PrimaryRed else (if (isIncome) PrimaryGreen else PrimaryRed)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (selectedCurrency == "USD") "$" else "৳",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isIncome) PrimaryGreen else PrimaryRed
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        androidx.compose.foundation.text.BasicTextField(
                                            value = amountText,
                                            onValueChange = {
                                                amountText = it
                                                if (it.isNotBlank()) amountError = false
                                            },
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextHead
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            decorationBox = { innerTextField ->
                                                if (amountText.isEmpty()) {
                                                    Text(
                                                        text = "0",
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextMuted.copy(alpha = 0.4f)
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )
                                    }
                                }

                                // Currency Pills (BDT / USD)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfacePill)
                                        .padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selectedCurrency == "BDT") (if (isIncome) PrimaryGreen else PrimaryRed) else Color.Transparent)
                                            .clickable { selectedCurrency = "BDT" }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "৳ BDT",
                                            fontSize = 10.5.sp,
                                            fontWeight = if (selectedCurrency == "BDT") FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedCurrency == "BDT") Color.White else TextMuted
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selectedCurrency == "USD") (if (isIncome) PrimaryGreen else PrimaryRed) else Color.Transparent)
                                            .clickable { selectedCurrency = "USD" }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$ USD",
                                            fontSize = 10.5.sp,
                                            fontWeight = if (selectedCurrency == "USD") FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedCurrency == "USD") Color.White else TextMuted
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = BorderHairline, thickness = 0.5.dp)

                            // Details / Note Input
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text(str("বিবরণ / নোট (ঐচ্ছিক)", "Note (Optional)"), fontSize = 11.sp) },
                                placeholder = {
                                    Text(
                                        text = str("যেমন: নগদ বিক্রি, পণ্য কেনা", "e.g. Cash sale, Goods purchase"),
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
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
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Save Entry Button directly underneath
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                amountError = true
                            } else {
                                onSave(entryType, amt, noteText.trim(), initialDate ?: System.currentTimeMillis(), selectedCurrency)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncome) PrimaryGreen else PrimaryRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isEditing) str("✓ আপডেট সংরক্ষণ করুন", "✓ Update Entry") else str("✓ সংরক্ষণ করুন", "✓ Save Entry"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsibleCashDateHeaderCard(
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
