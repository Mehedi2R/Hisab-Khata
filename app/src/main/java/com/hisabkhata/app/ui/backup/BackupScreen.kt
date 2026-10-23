package com.hisabkhata.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import com.hisabkhata.app.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisabkhata.app.data.local.AppDatabase
import com.hisabkhata.app.data.local.AppLanguageManager
import com.hisabkhata.app.data.local.AppLanguageManager.str
import com.hisabkhata.app.data.local.AppLanguageManager.formatDigitsLocalized
import com.hisabkhata.app.data.local.AppLanguageManager.formatDateLocalized
import com.hisabkhata.app.data.local.BackupManager
import com.hisabkhata.app.data.remote.GoogleDriveBackupManager
import com.hisabkhata.app.ui.components.formatDate
import com.hisabkhata.app.ui.components.toBanglaDigits
import com.hisabkhata.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by AppLanguageManager.currentLanguage.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val backupManager = remember {
        BackupManager(
            context,
            database.customerDao(),
            database.transactionDao(),
            database.cashBookDao(),
            database.familyExpenseDao()
        )
    }
    val googleDriveManager = remember { GoogleDriveBackupManager(context) }
    var signedInGoogleAccount by remember { mutableStateOf(googleDriveManager.getSignedInAccount()) }
    var isDriveUploading by remember { mutableStateOf(false) }
    var isDriveDownloading by remember { mutableStateOf(false) }
    var lastDriveBackupTime by remember { mutableStateOf(googleDriveManager.getLastDriveBackupTime()) }

    LaunchedEffect(signedInGoogleAccount) {
        if (signedInGoogleAccount != null) {
            val res = googleDriveManager.fetchLatestBackupModifiedTime()
            res.getOrNull()?.let { time ->
                if (time > 0L) {
                    lastDriveBackupTime = time
                }
            }
        }
    }

    fun formatDriveBackupDateTime(millis: Long): String {
        if (millis <= 0L) return str("এখনও ব্যাকআপ নেই", "No backup yet")
        val sdf = java.text.SimpleDateFormat("dd MMM yy hh:mm a", java.util.Locale.ENGLISH)
        val formatted = sdf.format(java.util.Date(millis))
        return if (AppLanguageManager.isEnglish()) {
            formatted
        } else {
            toBanglaDigits(formatted)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            signedInGoogleAccount = account
            scope.launch {
                googleDriveManager.fetchLatestBackupModifiedTime().getOrNull()?.let { time ->
                    if (time > 0L) lastDriveBackupTime = time
                }
                snackbarHostState.showSnackbar(
                    "${str("গুগল একাউন্টে যুক্ত হয়েছে:", "Connected to Google:")} ${account?.email ?: ""}"
                )
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "${str("সাইন-ইন সম্পন্ন হয়নি:", "Sign-in incomplete:")} ${e.message}"
                )
            }
        }
    }

    val customersFlow = remember { database.customerDao().getAllActiveCustomersFlow() }
    val transactionsFlow = remember { database.transactionDao().getAllTransactionsFlow() }
    val customers by customersFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val transactions by transactionsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var isOfflineBackingUp by remember { mutableStateOf(false) }

    var existingBackupFile by remember { mutableStateOf(backupManager.getExistingBackupFile()) }
    var showRestoreOptionsDialog by remember { mutableStateOf(false) }

    fun refreshExistingFile() {
        existingBackupFile = backupManager.getExistingBackupFile()
    }

    fun triggerCreateOfflineBackup() {
        scope.launch {
            isOfflineBackingUp = true
            val result = backupManager.createOfflineBackup()
            isOfflineBackingUp = false
            result.onSuccess {
                refreshExistingFile()
                snackbarHostState.showSnackbar(str("একক অফলাইন ব্যাকআপ সম্পন্ন!", "Offline backup saved!"))
            }.onFailure { err ->
                snackbarHostState.showSnackbar("${str("ব্যাকআপ ব্যর্থ:", "Backup failed:")} ${err.message}")
            }
        }
    }

    fun performRestore(content: String) {
        scope.launch {
            try {
                val result = backupManager.restoreOfflineBackup(content)
                result.onSuccess { res ->
                    refreshExistingFile()
                    val msg = buildString {
                        append(str("রিস্টোর সফল!", "Restore successful!"))
                        append(" ${formatDigitsLocalized(res.importedCustomers.toString())} ${str("ব্যক্তি", "accounts")}, ")
                        append("${formatDigitsLocalized(res.importedTransactions.toString())} ${str("লেনদেন", "txns")}")
                        if (res.importedCashBook > 0) {
                            append(", ${formatDigitsLocalized(res.importedCashBook.toString())} ${str("ক্যাশ এন্ট্রি", "cash entries")}")
                        }
                        if (res.importedFamilyExpenses > 0) {
                            append(", ${formatDigitsLocalized(res.importedFamilyExpenses.toString())} ${str("ফ্যামিলি খরচ", "family expenses")}")
                        }
                    }
                    snackbarHostState.showSnackbar(msg)
                }.onFailure { err ->
                    snackbarHostState.showSnackbar("${str("রিস্টোর ব্যর্থ:", "Restore failed:")} ${err.message}")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("${str("ফাইল পড়তে সমস্যা হয়েছে:", "Error reading file:")} ${e.message}")
            }
        }
    }

    // File picker for restoring backup
    val restoreFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().readText()
                    } ?: ""
                    performRestore(content)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("${str("ফাইল পড়তে সমস্যা হয়েছে:", "Error reading file:")} ${e.message}")
                }
            }
        }
    }

    Scaffold(
        containerColor = SurfaceCanvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = str("ডাটা ব্যাকআপ ও নিরাপত্তা", "Data Backup & Security"),
                        fontWeight = FontWeight.Bold,
                        color = TextHead,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = str("পেছনে", "Back"), tint = TextHead, modifier = Modifier.size(18.dp))
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 🛡️ Concept 3: Minimalist Material You Security Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = str("১০০% নিরাপদ ও এনক্রিপ্টেড", "100% Secure & Encrypted"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextHead
                            )
                            val approxSizeKb = (customers.size * 0.4 + transactions.size * 0.6).coerceAtLeast(0.8)
                            val sizeFormatted = String.format(java.util.Locale.US, "%.1f", approxSizeKb)
                            Text(
                                text = "${formatDigitsLocalized(customers.size.toString())} ${str("গ্রাহক", "accounts")} • ${formatDigitsLocalized(transactions.size.toString())} ${str("লেনদেন", "txns")} • ${formatDigitsLocalized(sizeFormatted)} KB",
                                fontSize = 10.5.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7))
                            .border(0.6.dp, Color(0xFF86EFAC), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = str("সুরক্ষিত ✓", "Protected ✓"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }

            // 📂 Google Drive Cloud Backup Card (Minimalist Material You - Update Backup)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_drive),
                                contentDescription = "Google Drive",
                                modifier = Modifier.size(34.dp)
                            )
                            Column {
                                Text(
                                    text = str("গুগল ড্রাইভ ব্যাকআপ", "Google Drive Backup"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextHead
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (signedInGoogleAccount != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Text(
                                            text = signedInGoogleAccount?.email ?: "",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF15803D)
                                        )
                                    } else {
                                        Text(
                                            text = str("অ্যাকাউন্ট সংযুক্ত নেই", "No account connected"),
                                            fontSize = 10.5.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        if (signedInGoogleAccount != null) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        googleDriveManager.signOut()
                                        signedInGoogleAccount = null
                                        snackbarHostState.showSnackbar(str("গুগল ড্রাইভ থেকে সাইন আউট হয়েছে", "Signed out from Google Drive"))
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(str("সাইন আউট", "Sign out"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PrimaryRed)
                            }
                        } else {
                            Button(
                                onClick = {
                                    googleSignInLauncher.launch(googleDriveManager.getSignInIntent())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(str("সাইন ইন", "Sign in"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Tonal Cloud Sync Status Strip (Shows Last Backup Date/Month/Year Time e.g. 23 Oct 26 11:10 AM)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(0.6.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = str("ক্লাউড সিঙ্ক স্ট্যাটাস:", "Cloud Sync Status:"),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            Text(
                                text = if (signedInGoogleAccount != null) {
                                    formatDriveBackupDateTime(lastDriveBackupTime)
                                } else {
                                    str("অ্যাকাউন্ট সংযুক্ত নেই", "No account connected")
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (signedInGoogleAccount != null && lastDriveBackupTime > 0L) Color(0xFF1D4ED8) else TextMuted
                            )
                        }
                    }

                    // Action Buttons: [ ☁ আপডেট ব্যাকআপ (Update Backup) ] & [ ⬇ রিস্টোর ব্যাকআপ ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    if (signedInGoogleAccount == null) {
                                        googleSignInLauncher.launch(googleDriveManager.getSignInIntent())
                                        return@launch
                                    }
                                    isDriveUploading = true
                                    // Ensure local backup exists first
                                    var f = existingBackupFile
                                    if (f == null || !f.exists()) {
                                        val genResult = backupManager.createOfflineBackup()
                                        if (genResult.isSuccess) {
                                            refreshExistingFile()
                                            f = backupManager.getExistingBackupFile()
                                        }
                                    }
                                    if (f != null && f.exists()) {
                                        val uploadResult = googleDriveManager.uploadBackupFile(f)
                                        isDriveUploading = false
                                        uploadResult.onSuccess {
                                            lastDriveBackupTime = googleDriveManager.getLastDriveBackupTime()
                                            snackbarHostState.showSnackbar(str("গুগল ড্রাইভে ব্যাকআপ সফলভাবে আপডেট হয়েছে!", "Backup updated on Google Drive successfully!"))
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar("${str("ড্রাইভ আপলোড ব্যর্থ:", "Drive upload failed:")} ${err.message}")
                                        }
                                    } else {
                                        isDriveUploading = false
                                        snackbarHostState.showSnackbar(str("ব্যাকআপ ফাইল পাওয়া যায়নি", "Backup file not found"))
                                    }
                                }
                            },
                            enabled = !isDriveUploading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            if (isDriveUploading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(str("আপডেট হচ্ছে...", "Updating..."), fontSize = 11.sp, color = Color.White)
                            } else {
                                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(str("আপডেট ব্যাকআপ", "Update Backup"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    if (signedInGoogleAccount == null) {
                                        googleSignInLauncher.launch(googleDriveManager.getSignInIntent())
                                        return@launch
                                    }
                                    isDriveDownloading = true
                                    val dlResult = googleDriveManager.downloadLatestBackup()
                                    isDriveDownloading = false
                                    dlResult.onSuccess { content ->
                                        lastDriveBackupTime = googleDriveManager.getLastDriveBackupTime()
                                        performRestore(content)
                                    }.onFailure { err ->
                                        snackbarHostState.showSnackbar("${str("ড্রাইভ থেকে রিস্টোর ব্যর্থ:", "Drive restore failed:")} ${err.message}")
                                    }
                                }
                            },
                            enabled = !isDriveDownloading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEFF6FF),
                                contentColor = Color(0xFF1D4ED8)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            if (isDriveDownloading) {
                                CircularProgressIndicator(color = Color(0xFF1D4ED8), modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(str("ডাউনলোড হচ্ছে...", "Downloading..."), fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1D4ED8))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(str("রিস্টোর ব্যাকআপ", "Restore Backup"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 💾 Twin Card 2: Offline Local Storage & File Card (Minimalist Material You)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = TextHead, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = str("অফলাইন লোকাল ফাইল", "Offline Local File"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextHead
                            )
                            Text(
                                text = "/Documents/HisabKhata2R/",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Existing File Snippet Row with Clean Share Pill Action
                    val file = existingBackupFile
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(0.6.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (file != null && backupManager.isFileReadable(file)) {
                                    val sizeKb = (file.length() / 1024.0)
                                    val sizeFormatted = String.format(java.util.Locale.US, "%.1f", sizeKb)
                                    Text(
                                        text = "📁 ${file.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF15803D)
                                    )
                                    Text(
                                        text = "${formatDigitsLocalized(sizeFormatted)} KB • ${formatDateLocalized(file.lastModified())}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                } else {
                                    Text(
                                        text = "📁 HisabKhata_Backup.json",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = TextHead
                                    )
                                    Text(
                                        text = str("এখনও অফলাইন ব্যাকআপ তৈরি হয়নি", "No backup created yet"),
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Share Action Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(0.8.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val f = existingBackupFile
                                        if (f != null && f.exists()) {
                                            backupManager.shareBackupFile(f)
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(str("প্রথমে অফলাইন ব্যাকআপ ফাইল তৈরি করুন।", "Create an offline backup file first."))
                                            }
                                        }
                                    }
                                    .padding(horizontal = 9.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = str("শেয়ার ↗", "Share ↗"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextHead
                                )
                            }
                        }
                    }

                    // 50/50 Balanced Action Buttons: [ 💾 নতুন ব্যাকআপ ] & [ 🔄 ফাইল রিস্টোর ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                triggerCreateOfflineBackup()
                            },
                            enabled = !isOfflineBackingUp,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            if (isOfflineBackingUp) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(str("সেভ হচ্ছে...", "Saving..."), fontSize = 11.sp)
                            } else {
                                Text(str("নতুন ব্যাকআপ", "Backup Now"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Button(
                            onClick = {
                                val f = existingBackupFile
                                if (f != null && backupManager.isFileReadable(f)) {
                                    showRestoreOptionsDialog = true
                                } else {
                                    restoreFilePickerLauncher.launch(arrayOf("*/*", "application/json", "text/plain"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF0F172A)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Text(str("ফাইল রিস্টোর", "Restore Now"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                    }
                }
            }

            // 🔒 Subtle Security Footer Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(0.6.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = str("সমস্ত ব্যাকআপ ফাইল ও ডাটা আপনার ডিভাইসে সুরক্ষিত এবং এনক্রিপ্ট থাকে।", "All backup files & data remain securely encrypted on your device."),
                        fontSize = 10.5.sp,
                        color = TextMuted,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Restore Options Dialog
        if (showRestoreOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreOptionsDialog = false },
                title = {
                    Text(str("ব্যাকআপ রিস্টোর", "Restore Backup"), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = str("আপনি কীভাবে ব্যাকআপ রিস্টোর করতে চান?", "How would you like to restore your backup?"),
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        existingBackupFile?.let { f ->
                            Button(
                                onClick = {
                                    showRestoreOptionsDialog = false
                                    val content = backupManager.readBackupContent(f)
                                    if (!content.isNullOrBlank()) {
                                        performRestore(content)
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(str("ব্যাকআপ ফাইল পড়া সম্ভব হয়নি। অনুগ্রহ করে ফাইল পিকার ব্যবহার করুন।", "Could not read backup file. Please use file picker."))
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(str("বর্তমান ব্যাকআপ ফাইল থেকে রিস্টোর", "Restore Current Backup File"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("📁 ${f.name} (${String.format(java.util.Locale.US, "%.1f", f.length() / 1024.0)} KB)", fontSize = 10.sp)
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                showRestoreOptionsDialog = false
                                restoreFilePickerLauncher.launch(arrayOf("*/*", "application/json", "text/plain"))
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(str("স্টোরেজ থেকে অন্য ফাইল নির্বাচন করুন", "Choose Other File from Storage"), fontSize = 11.sp, color = TextHead)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showRestoreOptionsDialog = false }) {
                        Text(str("বাতিল", "Cancel"), color = TextMuted)
                    }
                }
            )
        }

    }
}
