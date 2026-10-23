package com.hisabkhata.app.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hisabkhata.app.R
import com.hisabkhata.app.data.local.entity.CustomerEntity
import com.hisabkhata.app.data.local.entity.CustomerWithBalance
import com.hisabkhata.app.data.local.entity.TransactionEntity
import com.hisabkhata.app.ui.components.BalanceBadge
import com.hisabkhata.app.ui.components.formatDate
import com.hisabkhata.app.ui.components.formatMoney
import com.hisabkhata.app.ui.components.generateCustomerReportText
import com.hisabkhata.app.ui.components.shareReportText
import com.hisabkhata.app.data.local.AppLanguageManager
import com.hisabkhata.app.data.local.AppLanguageManager.str
import com.hisabkhata.app.data.local.AppLanguageManager.formatMoneyLocalized
import com.hisabkhata.app.data.local.AppLanguageManager.formatDigitsLocalized
import com.hisabkhata.app.ui.components.toBanglaDigits
import com.hisabkhata.app.ui.customer.AddCustomerDialog
import com.hisabkhata.app.ui.theme.BorderHairline
import com.hisabkhata.app.ui.theme.BorderLight
import com.hisabkhata.app.ui.theme.DeckBackground
import com.hisabkhata.app.ui.theme.FluidAuraCyan
import com.hisabkhata.app.ui.theme.FluidAuraPurple
import com.hisabkhata.app.ui.theme.FluidNeonCyan
import com.hisabkhata.app.ui.theme.FluidNeonGreen
import com.hisabkhata.app.ui.theme.FluidNeonPurple
import com.hisabkhata.app.ui.theme.GlassBorder
import com.hisabkhata.app.ui.theme.GlassSurface
import com.hisabkhata.app.ui.theme.PrimaryGreen
import com.hisabkhata.app.ui.theme.PrimaryGreenDark
import com.hisabkhata.app.ui.theme.PrimaryRed
import com.hisabkhata.app.ui.theme.PrimaryRedDark
import com.hisabkhata.app.ui.theme.SurfaceCanvas
import com.hisabkhata.app.ui.theme.SurfaceCard
import com.hisabkhata.app.ui.theme.SurfacePill
import com.hisabkhata.app.ui.theme.TextHead
import com.hisabkhata.app.ui.theme.TextMuted
import com.hisabkhata.app.ui.transaction.AddTransactionBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToCustomer: (String) -> Unit,
    onNavigateToBackup: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by AppLanguageManager.currentLanguage.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val customers by viewModel.customersWithBalance.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val payableCount by viewModel.payableProfilesCount.collectAsStateWithLifecycle()
    val receivableCount by viewModel.receivableProfilesCount.collectAsStateWithLifecycle()

    val selectedCustomerId by viewModel.selectedCustomerId.collectAsStateWithLifecycle()
    val customerTransactions by viewModel.selectedCustomerTransactions.collectAsStateWithLifecycle()

    var showAddPersonDialog by remember { mutableStateOf(false) }
    var activeQuickEntryCustomer by remember { mutableStateOf<CustomerWithBalance?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isReceivableTab = selectedTab == CustomerEntity.TYPE_CUSTOMER

    Scaffold(
        containerColor = SurfaceCanvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_hisab_khata),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Text(
                                text = str("হিসাব খাতা", "Hisab Khata"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextHead
                            )
                        }
                    }
                },
                actions = {
                    // Backup navigation
                    IconButton(onClick = onNavigateToBackup) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = str("ব্যাকআপ", "Backup"),
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassSurface)
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FluidAuraCyan,
                            FluidAuraPurple.copy(alpha = 0.35f),
                            SurfaceCanvas
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            val isTabletOrLandscape = maxWidth >= 600.dp

            Box(modifier = Modifier.fillMaxSize()) {
                if (isTabletOrLandscape) {
                    // Adaptive Dual-Pane layout for Tablets & Landscape
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left Pane: Fluid Glass Header, Tabs, Filters & List
                        Column(
                            modifier = Modifier
                                .weight(0.48f)
                                .fillMaxHeight()
                                .border(width = 0.5.dp, color = BorderHairline)
                        ) {
                            FluidInteractiveGlassHero(
                                summary = summary,
                                payableCount = payableCount,
                                receivableCount = receivableCount
                            )
                            TabsSection(
                                isReceivableTab = isReceivableTab,
                                payableCount = payableCount,
                                receivableCount = receivableCount,
                                onTabSelected = { viewModel.setTab(it) }
                            )
                            CustomerListSection(
                                customers = customers,
                                isReceivableTab = isReceivableTab,
                                onCustomerClick = { customer ->
                                    viewModel.selectCustomer(customer.customer.id)
                                },
                                onCallClick = { phone -> dialPhone(context, phone) }
                            )
                        }

                        // Right Pane: Active Customer Details & Transactions
                        Box(
                            modifier = Modifier
                                .weight(0.52f)
                                .fillMaxHeight()
                                .background(SurfaceCard)
                        ) {
                            val activeCustomer = customers.find { it.customer.id == selectedCustomerId }
                                ?: customers.firstOrNull()

                            if (activeCustomer != null) {
                                TabletCustomerDetailPane(
                                    customerItem = activeCustomer,
                                    transactions = customerTransactions,
                                    onOpenFullDetail = { onNavigateToCustomer(activeCustomer.customer.id) },
                                    onQuickEntry = { activeQuickEntryCustomer = activeCustomer }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(str("তালিকা থেকে যেকোনো হিসাব নির্বাচন করুন", "Select an account from the list"), color = TextMuted, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Mobile Single-Column Layout with Floating Neo-Deck
                    Column(modifier = Modifier.fillMaxSize()) {
                        FluidInteractiveGlassHero(
                            summary = summary,
                            payableCount = payableCount,
                            receivableCount = receivableCount
                        )
                        TabsSection(
                            isReceivableTab = isReceivableTab,
                            payableCount = payableCount,
                            receivableCount = receivableCount,
                            onTabSelected = { viewModel.setTab(it) }
                        )
                        CustomerListSection(
                            customers = customers,
                            isReceivableTab = isReceivableTab,
                            onCustomerClick = { onNavigateToCustomer(it.customer.id) },
                            onCallClick = { phone -> dialPhone(context, phone) }
                        )
                    }

                    // FAB for Adding Customer / Supplier
                    FloatingActionButton(
                        onClick = { showAddPersonDialog = true },
                        containerColor = if (isReceivableTab) PrimaryRed else PrimaryGreen,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 12.dp, end = 16.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(
                                text = "+ Profile Add",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Quick Transaction Entry Bottom Sheet
    activeQuickEntryCustomer?.let { customerItem ->
        AddTransactionBottomSheet(
            customerName = customerItem.customer.name,
            currentBalance = customerItem.netBalance,
            onDismiss = { activeQuickEntryCustomer = null },
            onSave = { type, amount, note, date, currency ->
                viewModel.addTransactionForCustomer(
                    customerId = customerItem.customer.id,
                    type = type,
                    amount = amount,
                    note = note,
                    date = date,
                    currency = currency
                ) {
                    activeQuickEntryCustomer = null
                }
            }
        )
    }

    // Dialog: Add Person / Customer
    if (showAddPersonDialog) {
        AddCustomerDialog(
            initialType = selectedTab,
            onDismiss = { showAddPersonDialog = false },
            onSave = { name, phone, address, type ->
                viewModel.addCustomer(name, phone, address, type) {
                    showAddPersonDialog = false
                }
            }
        )
    }
}

/**
 * 🌟 কনসেপ্ট ২: Split Twin Deck (Tight Gap & High Elegance)
 * পাশাপাশি দুটি সুবিন্যস্ত কার্ড: পাবো (Receive) ও দেবো (Pay) + স্লিম নীট ব্যালেন্স টিকার
 */
@Composable
private fun FluidInteractiveGlassHero(
    summary: com.hisabkhata.app.data.repository.DashboardSummary,
    payableCount: Int = 0,
    receivableCount: Int = 0
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Twin Metric Cards Grid (Receive vs Pay)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Card 1: পাবো / Receive (Emerald Theme)
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = GlassSurface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF10B981).copy(alpha = 0.35f)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = str("পাবো / Receive", "Receive"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${formatDigitsLocalized(receivableCount.toString())}${str(" জন", "")}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = AppLanguageManager.formatCurrencyLocalized(summary.totalReceivableBdt, "BDT"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )

                    if (summary.hasUsd) {
                        Text(
                            text = "USD: ${AppLanguageManager.formatCurrencyLocalized(summary.totalReceivableUsd, "USD")}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryGreen
                        )
                    }
                }
            }

            // Card 2: দেবো / Pay (Rose Theme)
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = GlassSurface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFFEF4444).copy(alpha = 0.35f)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = str("দেবো / Pay", "Pay"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEF2F2))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${formatDigitsLocalized(payableCount.toString())}${str(" জন", "")}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = AppLanguageManager.formatCurrencyLocalized(summary.totalPayableBdt, "BDT"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed
                    )

                    if (summary.hasUsd) {
                        Text(
                            text = "USD: ${AppLanguageManager.formatCurrencyLocalized(summary.totalPayableUsd, "USD")}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryRed
                        )
                    }
                }
            }
        }

        // Net Balance Slim Ticker (Tight Gap)
        val netBdt = summary.totalReceivableBdt - summary.totalPayableBdt
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderLight))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = str("নীট ব্যালেন্স:", "Net Balance:"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    Text(
                        text = AppLanguageManager.formatCurrencyLocalized(netBdt, "BDT"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netBdt > 0) PrimaryGreen else if (netBdt < 0) PrimaryRed else TextHead
                    )
                }

                if (summary.hasUsd) {
                    val netUsd = summary.totalReceivableUsd - summary.totalPayableUsd
                    Text(
                        text = "USD: ${AppLanguageManager.formatCurrencyLocalized(netUsd, "USD")}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netUsd > 0) PrimaryGreen else if (netUsd < 0) PrimaryRed else TextHead
                    )
                }
            }
        }
    }
}

@Composable
private fun TabsSection(
    isReceivableTab: Boolean,
    payableCount: Int,
    receivableCount: Int,
    onTabSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePill)
            .padding(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Tab 1: দেনা / ঋণ (Left side)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isReceivableTab) SurfaceCard else Color.Transparent)
                    .clickable { onTabSelected(CustomerEntity.TYPE_CUSTOMER) }
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isReceivableTab) PrimaryRed.copy(alpha = 0.12f) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (isReceivableTab) PrimaryRed else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = str("দেনা / ঋণ", "Debt / Payable"),
                        fontSize = 12.sp,
                        fontWeight = if (isReceivableTab) FontWeight.Bold else FontWeight.Medium,
                        color = if (isReceivableTab) PrimaryRed else TextMuted
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isReceivableTab) Color(0xFFFEF2F2) else Color(0xFFF1F5F9))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = formatDigitsLocalized(payableCount.toString()),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isReceivableTab) PrimaryRed else TextMuted
                        )
                    }
                }
            }

            // Tab 2: পাওনা (Right side)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (!isReceivableTab) SurfaceCard else Color.Transparent)
                    .clickable { onTabSelected(CustomerEntity.TYPE_SUPPLIER) }
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (!isReceivableTab) PrimaryGreen.copy(alpha = 0.12f) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = if (!isReceivableTab) PrimaryGreen else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = str("পাওনা", "Due / Receivable"),
                        fontSize = 12.sp,
                        fontWeight = if (!isReceivableTab) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isReceivableTab) PrimaryGreen else TextMuted
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isReceivableTab) Color(0xFFECFDF5) else Color(0xFFF1F5F9))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = formatDigitsLocalized(receivableCount.toString()),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isReceivableTab) PrimaryGreen else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerListSection(
    customers: List<CustomerWithBalance>,
    isReceivableTab: Boolean,
    onCustomerClick: (CustomerWithBalance) -> Unit,
    onCallClick: (String) -> Unit
) {
    if (customers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isReceivableTab) str("দেনা / ঋণের তালিকা খালি", "No Debt / Payable accounts")
                       else str("পাওনার তালিকা খালি", "No Due / Receivable accounts"),
                color = TextMuted,
                fontSize = 13.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(customers, key = { it.customer.id }) { item ->
                CustomerCardWithMicroActions(
                    item = item,
                    onClick = { onCustomerClick(item) },
                    onCallClick = { onCallClick(item.customer.phone) }
                )
            }
        }
    }
}

@Composable
private fun CustomerCardWithMicroActions(
    item: CustomerWithBalance,
    onClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderLight))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val initial = item.customer.name.take(1).uppercase()
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF334155)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.customer.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextHead
                    )
                    if (item.customer.phone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.customer.phone,
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = str("কল করুন", "Call"),
                                tint = PrimaryGreen,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable(onClick = onCallClick)
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val hasActiveUsd = item.hasUsd && kotlin.math.abs(item.netBalanceUsd) > 0.001
                BalanceBadge(
                    netBalance = item.netBalanceBdt,
                    currency = "BDT",
                    suppressSettled = hasActiveUsd
                )
                if (item.hasUsd) {
                    val hasActiveBdt = kotlin.math.abs(item.netBalanceBdt) > 0.001
                    BalanceBadge(
                        netBalance = item.netBalanceUsd,
                        currency = "USD",
                        suppressSettled = hasActiveBdt
                    )
                }
            }
        }
    }
}


@Composable
private fun TabletCustomerDetailPane(
    customerItem: CustomerWithBalance,
    transactions: List<TransactionEntity>,
    onOpenFullDetail: () -> Unit,
    onQuickEntry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Customer Header Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = customerItem.customer.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHead
                )
                Text(
                    text = customerItem.customer.phone.ifBlank { str("ফোন নম্বর নেই", "No phone number") },
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val hasActiveUsd = customerItem.hasUsd && kotlin.math.abs(customerItem.netBalanceUsd) > 0.001
                BalanceBadge(
                    netBalance = customerItem.netBalanceBdt,
                    currency = "BDT",
                    suppressSettled = hasActiveUsd
                )
                if (customerItem.hasUsd) {
                    val hasActiveBdt = kotlin.math.abs(customerItem.netBalanceBdt) > 0.001
                    BalanceBadge(
                        netBalance = customerItem.netBalanceUsd,
                        currency = "USD",
                        suppressSettled = hasActiveBdt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryGreen)
                    .clickable(onClick = onQuickEntry)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(str("+ লেনদেন এন্ট্রি", "+ Add Entry"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9))
                    .clickable(onClick = onOpenFullDetail)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(str("বিস্তারিত দেখুন", "View Details"), color = TextHead, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "${str("সাম্প্রতিক লেনদেন", "Recent Transactions")} (${formatDigitsLocalized(transactions.size.toString())})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextHead
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(str("কোনো লেনদেন পাওয়া যায়নি", "No transactions found"), color = TextMuted, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    val isDebit = tx.type == TransactionEntity.TYPE_DEBIT
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(0.5.dp, BorderLight, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isDebit) str("দিলাম", "Gave") else str("পেলাম", "Got"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDebit) PrimaryRed else PrimaryGreen
                            )
                            Text(
                                text = formatDate(tx.date),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        Text(
                            text = (if (isDebit) "-" else "+") + AppLanguageManager.formatCurrencyLocalized(tx.amount, tx.currency),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDebit) PrimaryRed else PrimaryGreen
                        )
                    }
                }
            }
        }
    }
}

private fun dialPhone(context: Context, phone: String) {
    if (phone.isBlank()) return
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (_: Exception) {}
}
