package com.hisabkhata.app.ui.customer

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ReceiptLong
import com.hisabkhata.app.ui.components.shareReportText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hisabkhata.app.data.local.AppLanguageManager
import com.hisabkhata.app.data.local.AppLanguageManager.str
import com.hisabkhata.app.data.local.AppLanguageManager.formatMoneyLocalized
import com.hisabkhata.app.data.local.AppLanguageManager.formatDigitsLocalized
import com.hisabkhata.app.data.local.AppLanguageManager.formatDateLocalized
import com.hisabkhata.app.data.local.entity.TransactionEntity
import com.hisabkhata.app.ui.components.BalanceBadge
import com.hisabkhata.app.ui.components.formatDate
import com.hisabkhata.app.ui.components.formatMoney
import com.hisabkhata.app.ui.theme.BorderHairline
import com.hisabkhata.app.ui.theme.BorderLight
import com.hisabkhata.app.ui.theme.PrimaryGreen
import com.hisabkhata.app.ui.theme.PrimaryRed
import com.hisabkhata.app.ui.theme.SurfaceCanvas
import com.hisabkhata.app.ui.theme.SurfaceCard
import com.hisabkhata.app.ui.theme.TextHead
import com.hisabkhata.app.ui.theme.TextMuted
import com.hisabkhata.app.ui.transaction.AddTransactionBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: CustomerDetailViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CustomerDetailViewModel(
                    context.applicationContext as Application,
                    customerId
                ) as T
            }
        }
    )

    val currentLang by AppLanguageManager.currentLanguage.collectAsStateWithLifecycle()
    val customerBalance by viewModel.customerBalance.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    var showAddTxSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var txEntryType by remember { mutableStateOf(TransactionEntity.TYPE_DEBIT) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditCustomerDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val customer = customerBalance?.customer

    Scaffold(
        containerColor = SurfaceCanvas,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = customer?.name ?: str("গ্রাহক বিবরণ", "Account Details"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHead
                        )
                        if (!customer?.phone.isNullOrBlank()) {
                            Text(
                                text = customer?.phone ?: "",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = str("পেছনে", "Back"), tint = TextHead)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        shareReportText(context, viewModel.getStatementShareText())
                    }) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = str("বিবরণী রিপোর্ট", "Statement Report"), tint = TextHead)
                    }
                    if (!customer?.phone.isNullOrBlank()) {
                        IconButton(onClick = { dialPhone(context, customer?.phone ?: "") }) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = str("কল", "Call"), tint = TextHead)
                        }
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = TextHead)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(str("প্রোফাইল সম্পাদনা", "Edit Profile"), color = TextHead) },
                            onClick = {
                                showMenu = false
                                showEditCustomerDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = com.hisabkhata.app.R.drawable.ic_custom_edit),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(str("হিসাব বিবরণী রিপোর্ট", "Statement Report"), color = TextHead) },
                            onClick = {
                                showMenu = false
                                shareReportText(context, viewModel.getStatementShareText())
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = TextHead,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(str("গ্রাহক ডিলিট", "Delete Account"), color = PrimaryRed) },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = com.hisabkhata.app.R.drawable.ic_custom_delete),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceCanvas)
                .padding(paddingValues)
        ) {
            // Customer Header Card - Concept 3: Minimalist Ticker Clean
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(0.5.dp),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    val netBdt = customerBalance?.netBalanceBdt ?: (customerBalance?.netBalance ?: 0.0)
                    val isPayable = netBdt > 0.001
                    val isReceivable = netBdt < -0.001

                    // Slim Ticker Balance Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isPayable) Color(0xFFFEF2F2)
                                else if (isReceivable) Color(0xFFECFDF5)
                                else Color(0xFFF8FAFC)
                            )
                            .border(
                                0.5.dp,
                                if (isPayable) PrimaryRed.copy(alpha = 0.25f)
                                else if (isReceivable) PrimaryGreen.copy(alpha = 0.25f)
                                else BorderLight,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = str("বর্তমান নীট হিসাব", "Current Net Balance"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isPayable) PrimaryRed else if (isReceivable) PrimaryGreen else TextMuted
                                )
                                Text(
                                    text = AppLanguageManager.formatCurrencyLocalized(netBdt, "BDT"),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPayable) PrimaryRed else if (isReceivable) PrimaryGreen else TextHead
                                )
                                if (customerBalance?.hasUsd == true) {
                                    val netUsd = customerBalance?.netBalanceUsd ?: 0.0
                                    Text(
                                        text = "USD: ${AppLanguageManager.formatCurrencyLocalized(netUsd, "USD")}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (netUsd > 0) PrimaryRed else if (netUsd < 0) PrimaryGreen else TextHead
                                    )
                                }
                            }

                            customerBalance?.let {
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    val hasActiveUsd = it.hasUsd && kotlin.math.abs(it.netBalanceUsd) > 0.001
                                    BalanceBadge(
                                        netBalance = it.netBalanceBdt,
                                        currency = "BDT",
                                        suppressSettled = hasActiveUsd
                                    )
                                    if (it.hasUsd) {
                                        val hasActiveBdt = kotlin.math.abs(it.netBalanceBdt) > 0.001
                                        BalanceBadge(
                                            netBalance = it.netBalanceUsd,
                                            currency = "USD",
                                            suppressSettled = hasActiveBdt
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    // Full Width Twin Actions: পেলাম (+) & দিলাম (-) [Report removed from here]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                editingTransaction = null
                                txEntryType = TransactionEntity.TYPE_CREDIT
                                showAddTxSheet = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Text(str("পেলাম (+)", "Got (+)"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                editingTransaction = null
                                txEntryType = TransactionEntity.TYPE_DEBIT
                                showAddTxSheet = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Text(str("দিলাম (-)", "Gave (-)"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Transactions Header - Tight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${str("লেনদেন খতিয়ান", "Transaction Ledger")} (${formatDigitsLocalized(transactions.size.toString())})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHead
                )
            }

            // Transaction List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = str("কোনো লেনদেন এন্ট্রি নেই", "No transaction records found"),
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItemCard(
                            transaction = tx,
                            onEdit = {
                                editingTransaction = tx
                                txEntryType = tx.type
                                showAddTxSheet = true
                            },
                            onDelete = { viewModel.deleteTransaction(tx.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddTxSheet) {
        val isEditing = editingTransaction != null
        AddTransactionBottomSheet(
            customerName = customer?.name ?: "গ্রাহক",
            currentBalance = customerBalance?.netBalance ?: 0.0,
            initialType = editingTransaction?.type ?: txEntryType,
            initialAmount = editingTransaction?.amount,
            initialCurrency = editingTransaction?.currency ?: TransactionEntity.CURRENCY_BDT,
            initialNote = editingTransaction?.note ?: "",
            initialDate = editingTransaction?.date,
            isEditing = isEditing,
            onDismiss = {
                showAddTxSheet = false
                editingTransaction = null
            },
            onSave = { type, amount, note, date, currency ->
                if (isEditing && editingTransaction != null) {
                    viewModel.updateTransaction(
                        transactionId = editingTransaction!!.id,
                        type = type,
                        amount = amount,
                        note = note,
                        date = date,
                        currency = currency
                    ) {
                        showAddTxSheet = false
                        editingTransaction = null
                    }
                } else {
                    viewModel.addTransaction(type, amount, note, date, currency) {
                        showAddTxSheet = false
                    }
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(str("গ্রাহক মুছবেন?", "Delete Account?")) },
            text = { Text(if (AppLanguageManager.isEnglish()) "Are you sure you want to delete '${customer?.name}'?" else "'${customer?.name}' কে খাতা থেকে মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteCustomer {
                            onNavigateBack()
                        }
                    }
                ) {
                    Text(str("মুছুন", "Delete"), color = PrimaryRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(str("বাতিল", "Cancel"))
                }
            }
        )
    }

    if (showEditCustomerDialog && customer != null) {
        AddCustomerDialog(
            initialType = customer.type,
            initialName = customer.name,
            initialPhone = customer.phone,
            initialAddress = customer.address,
            isEditing = true,
            onDismiss = { showEditCustomerDialog = false },
            onSave = { name, phone, address, type ->
                viewModel.updateCustomer(name, phone, address, type) {
                    showEditCustomerDialog = false
                }
            }
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDebit = transaction.type == TransactionEntity.TYPE_DEBIT
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(0.5.dp),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDateLocalized(transaction.date),
                    fontSize = 11.sp,
                    color = TextMuted
                )
                if (transaction.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.note,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextHead
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = (if (isDebit) "-" else "+") + AppLanguageManager.formatCurrencyLocalized(transaction.amount, transaction.currency),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDebit) PrimaryRed else PrimaryGreen
                    )
                    Text(
                        text = if (isDebit) str("দিলাম", "Gave") else str("পেলাম", "Got"),
                        fontSize = 10.sp,
                        color = if (isDebit) PrimaryRed else PrimaryGreen
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

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

                Spacer(modifier = Modifier.width(2.dp))

                IconButton(
                    onClick = { showDeleteConfirm = true },
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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(str("লেনদেন মুছবেন?", "Delete Transaction?")) },
            text = { Text(str("লেনদেনটি খাতা থেকে স্থায়ীভাবে মুছে যাবে।", "This transaction will be permanently deleted from the ledger.")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text(str("মুছুন", "Delete"), color = PrimaryRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(str("বাতিল", "Cancel"))
                }
            }
        )
    }
}

private fun dialPhone(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private fun shareText(context: Context, text: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "বিবরণী শেয়ার করুন"))
    } catch (_: Exception) {}
}
