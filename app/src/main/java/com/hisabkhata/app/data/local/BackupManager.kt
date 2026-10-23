package com.hisabkhata.app.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.hisabkhata.app.data.local.dao.CashBookDao
import com.hisabkhata.app.data.local.dao.CustomerDao
import com.hisabkhata.app.data.local.dao.FamilyExpenseDao
import com.hisabkhata.app.data.local.dao.TransactionDao
import com.hisabkhata.app.data.local.entity.CashBookEntity
import com.hisabkhata.app.data.local.entity.CustomerEntity
import com.hisabkhata.app.data.local.entity.FamilyExpenseEntity
import com.hisabkhata.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OfflineBackupResult(
    val file: File,
    val customerCount: Int,
    val transactionCount: Int,
    val cashBookCount: Int = 0,
    val familyExpenseCount: Int = 0
)

data class RestoreResult(
    val importedCustomers: Int,
    val importedTransactions: Int,
    val importedCashBook: Int = 0,
    val importedFamilyExpenses: Int = 0
)

class BackupManager(
    private val context: Context,
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao,
    private val cashBookDao: CashBookDao? = null,
    private val familyExpenseDao: FamilyExpenseDao? = null
) {

    companion object {
        const val BACKUP_DIR_NAME = "HisabKhata2R"
        const val SINGLE_BACKUP_FILE_NAME = "HisabKhata_Backup.json"
    }

    /**
     * Resolves the target directory: /Documents/HisabKhata2R/
     * Uses public Documents directory where accessible, with fallback to external files Documents.
     */
    fun getBackupDirectory(): File {
        return try {
            val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val targetDir = File(publicDocs, BACKUP_DIR_NAME)
            if (targetDir.exists() || targetDir.mkdirs()) {
                targetDir
            } else {
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BACKUP_DIR_NAME).apply { mkdirs() }
            }
        } catch (_: Exception) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BACKUP_DIR_NAME).apply { mkdirs() }
        }
    }

    /**
     * Tests if a file exists, is non-empty, and can actually be read without throwing EACCES.
     */
    fun isFileReadable(file: File?): Boolean {
        if (file == null || !file.exists() || file.length() <= 0L) return false
        return try {
            file.inputStream().use { stream -> stream.read() >= 0 }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Returns the single backup file if it exists and is guaranteed readable.
     * Prioritizes accessible storage and never returns an unreadable EACCES-blocked file.
     */
    fun getExistingBackupFile(): File? {
        val candidates = mutableListOf<File>()

        // 1. Check app external Documents directory (100% guaranteed accessible on Android 10-15)
        try {
            val appFile = File(
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BACKUP_DIR_NAME),
                SINGLE_BACKUP_FILE_NAME
            )
            if (isFileReadable(appFile)) {
                candidates.add(appFile)
            }
        } catch (_: Exception) {}

        // 2. Check public Documents directory (only if actually readable without EACCES)
        try {
            val publicFile = File(
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), BACKUP_DIR_NAME),
                SINGLE_BACKUP_FILE_NAME
            )
            if (isFileReadable(publicFile)) {
                candidates.add(publicFile)
            }
        } catch (_: Exception) {}

        // 3. Check internal app files directory as fallback
        try {
            val internalFile = File(
                File(context.filesDir, BACKUP_DIR_NAME),
                SINGLE_BACKUP_FILE_NAME
            )
            if (isFileReadable(internalFile)) {
                candidates.add(internalFile)
            }
        } catch (_: Exception) {}

        // Return candidate with the latest modification timestamp
        return candidates.maxByOrNull { it.lastModified() }
    }

    /**
     * Safely reads the backup content from the preferred file or any accessible backup mirror.
     * Prevents EACCES crashes by cascading across storage candidates.
     */
    fun readBackupContent(preferredFile: File? = null): String? {
        val candidateFiles = mutableListOf<File>()
        if (preferredFile != null) candidateFiles.add(preferredFile)
        getExistingBackupFile()?.let { if (!candidateFiles.contains(it)) candidateFiles.add(it) }

        try {
            val appFile = File(
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BACKUP_DIR_NAME),
                SINGLE_BACKUP_FILE_NAME
            )
            if (!candidateFiles.contains(appFile)) candidateFiles.add(appFile)
        } catch (_: Exception) {}

        try {
            val internalFile = File(File(context.filesDir, BACKUP_DIR_NAME), SINGLE_BACKUP_FILE_NAME)
            if (!candidateFiles.contains(internalFile)) candidateFiles.add(internalFile)
        } catch (_: Exception) {}

        for (cand in candidateFiles) {
            try {
                if (cand.exists() && cand.length() > 0L) {
                    val text = cand.readText()
                    if (text.isNotBlank()) return text
                }
            } catch (_: Exception) {
                // EACCES or read error on this candidate; seamlessly fallback to next candidate
            }
        }
        return null
    }

    /**
     * Creates an offline JSON backup.
     * Always writes to the single master backup file (cleanly replacing previous backup state).
     * Resilient against Android 10+ EACCES Scoped Storage restrictions.
     */
    suspend fun createOfflineBackup(): Result<OfflineBackupResult> = withContext(Dispatchers.IO) {
        try {
            val customers = customerDao.getAllActiveCustomersFlow().first()
            val activeCustomerIds = customers.map { it.id }.toSet()
            val allTransactions = transactionDao.getAllTransactionsFlow().first()
            val transactions = allTransactions.filter { it.customerId in activeCustomerIds }

            val cashBookEntries = cashBookDao?.getAllEntriesFlow()?.first() ?: emptyList()
            val familyExpenses = familyExpenseDao?.getAllExpensesFlow()?.first() ?: emptyList()

            val rootJson = JSONObject()
            rootJson.put("appName", "Hisab Khata")
            rootJson.put("version", "3.3.0")
            rootJson.put("timestamp", System.currentTimeMillis())
            rootJson.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

            // Customers array (Active accounts)
            val customersArray = JSONArray()
            customers.forEach { c ->
                val cJson = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("phone", c.phone)
                    put("address", c.address)
                    put("type", c.type)
                    put("createdAt", c.createdAt)
                    put("updatedAt", c.updatedAt)
                    put("isDeleted", false)
                }
                customersArray.put(cJson)
            }
            rootJson.put("customers", customersArray)

            // Transactions array
            val txArray = JSONArray()
            transactions.forEach { tx ->
                val tJson = JSONObject().apply {
                    put("id", tx.id)
                    put("customerId", tx.customerId)
                    put("type", tx.type)
                    put("amount", tx.amount)
                    put("currency", tx.currency)
                    put("note", tx.note)
                    put("date", tx.date)
                    put("createdAt", tx.createdAt)
                    put("updatedAt", tx.updatedAt)
                    put("isDeleted", false)
                }
                txArray.put(tJson)
            }
            rootJson.put("transactions", txArray)

            // Cash Book array
            val cbArray = JSONArray()
            cashBookEntries.forEach { cb ->
                val cbJson = JSONObject().apply {
                    put("id", cb.id)
                    put("type", cb.type)
                    put("amount", cb.amount)
                    put("category", cb.category)
                    put("paymentMethod", cb.paymentMethod)
                    put("note", cb.note)
                    put("date", cb.date)
                    put("createdAt", cb.createdAt)
                    put("currency", cb.currency)
                }
                cbArray.put(cbJson)
            }
            rootJson.put("cashBook", cbArray)

            // Family Expense array
            val feArray = JSONArray()
            familyExpenses.forEach { fe ->
                val feJson = JSONObject().apply {
                    put("id", fe.id)
                    put("category", fe.category)
                    put("amount", fe.amount)
                    put("note", fe.note)
                    put("date", fe.date)
                }
                feArray.put(feJson)
            }
            rootJson.put("familyExpenses", feArray)

            val jsonBytes = rootJson.toString(2).toByteArray(Charsets.UTF_8)
            var savedFile: File? = null

            fun writeCleanOverwriteFile(targetFile: File) {
                if (targetFile.exists()) {
                    try {
                        targetFile.delete()
                    } catch (_: Exception) {}
                }
                FileOutputStream(targetFile, false).use { fos ->
                    fos.channel.truncate(0)
                    fos.write(jsonBytes)
                    fos.flush()
                    try {
                        fos.fd.sync()
                    } catch (_: Exception) {}
                }
            }

            // 1. Guaranteed App External Documents Storage (100% reliable on all Android versions 10-15)
            val appDocsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BACKUP_DIR_NAME)
            if (!appDocsDir.exists()) {
                appDocsDir.mkdirs()
            }
            appDocsDir.listFiles()?.forEach { f ->
                if (f.name != SINGLE_BACKUP_FILE_NAME && f.name.startsWith("HisabKhata_Backup")) {
                    f.delete()
                }
            }
            val appFile = File(appDocsDir, SINGLE_BACKUP_FILE_NAME)
            writeCleanOverwriteFile(appFile)
            savedFile = appFile

            // 2. Extra safety: Internal app storage mirror
            try {
                val internalDir = File(context.filesDir, BACKUP_DIR_NAME)
                if (!internalDir.exists()) internalDir.mkdirs()
                val internalFile = File(internalDir, SINGLE_BACKUP_FILE_NAME)
                writeCleanOverwriteFile(internalFile)
            } catch (_: Exception) {}

            // 3. Optional Mirror: Public Documents / HisabKhata2R / HisabKhata_Backup.json (if accessible without EACCES)
            try {
                val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val publicDir = File(publicDocs, BACKUP_DIR_NAME)
                if (publicDir.exists() || publicDir.mkdirs()) {
                    publicDir.listFiles()?.forEach { f ->
                        if (f.name != SINGLE_BACKUP_FILE_NAME && f.name.startsWith("HisabKhata_Backup")) {
                            f.delete()
                        }
                    }
                    val publicFile = File(publicDir, SINGLE_BACKUP_FILE_NAME)
                    writeCleanOverwriteFile(publicFile)
                    if (isFileReadable(publicFile)) {
                        savedFile = publicFile
                    }
                }
            } catch (_: Exception) {
                // Public storage restricted or EACCES; safely ignored as appFile is already secured
            }

            Result.success(
                OfflineBackupResult(
                    file = savedFile,
                    customerCount = customers.size,
                    transactionCount = transactions.size,
                    cashBookCount = cashBookEntries.size,
                    familyExpenseCount = familyExpenses.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreOfflineBackup(jsonContent: String): Result<RestoreResult> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonContent)
            val customersArray = root.optJSONArray("customers") ?: JSONArray()
            val txArray = root.optJSONArray("transactions") ?: JSONArray()
            val cbArray = root.optJSONArray("cashBook") ?: JSONArray()
            val feArray = root.optJSONArray("familyExpenses") ?: JSONArray()

            val customerList = mutableListOf<CustomerEntity>()
            for (i in 0 until customersArray.length()) {
                val obj = customersArray.getJSONObject(i)
                customerList.add(
                    CustomerEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        phone = obj.optString("phone", ""),
                        address = obj.optString("address", ""),
                        type = obj.optString("type", CustomerEntity.TYPE_CUSTOMER),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        isDeleted = false,
                        syncStatus = CustomerEntity.SYNC_PENDING
                    )
                )
            }

            val txList = mutableListOf<TransactionEntity>()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                txList.add(
                    TransactionEntity(
                        id = obj.getString("id"),
                        customerId = obj.getString("customerId"),
                        type = obj.getString("type"),
                        amount = obj.getDouble("amount"),
                        currency = obj.optString("currency", TransactionEntity.CURRENCY_BDT),
                        note = obj.optString("note", ""),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        isDeleted = false,
                        syncStatus = TransactionEntity.SYNC_PENDING
                    )
                )
            }

            val cbList = mutableListOf<CashBookEntity>()
            for (i in 0 until cbArray.length()) {
                val obj = cbArray.getJSONObject(i)
                cbList.add(
                    CashBookEntity(
                        id = obj.getString("id"),
                        type = obj.getString("type"),
                        amount = obj.getDouble("amount"),
                        category = obj.optString("category", "সাধারণ"),
                        paymentMethod = obj.optString("paymentMethod", "নগদ"),
                        note = obj.optString("note", ""),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        currency = obj.optString("currency", "BDT")
                    )
                )
            }

            val feList = mutableListOf<FamilyExpenseEntity>()
            for (i in 0 until feArray.length()) {
                val obj = feArray.getJSONObject(i)
                feList.add(
                    FamilyExpenseEntity(
                        id = obj.getString("id"),
                        category = obj.optString("category", "অন্যান্য"),
                        amount = obj.getDouble("amount"),
                        note = obj.optString("note", ""),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            if (customerList.isNotEmpty()) {
                customerDao.insertAll(customerList)
            }
            if (txList.isNotEmpty()) {
                transactionDao.insertAll(txList)
            }
            if (cbList.isNotEmpty() && cashBookDao != null) {
                cashBookDao.insertAll(cbList)
            }
            if (feList.isNotEmpty() && familyExpenseDao != null) {
                familyExpenseDao.insertAll(feList)
            }

            // Clean orphaned records
            transactionDao.cleanOrphanedTransactions()

            Result.success(
                RestoreResult(
                    importedCustomers = customerList.size,
                    importedTransactions = txList.size,
                    importedCashBook = cbList.size,
                    importedFamilyExpenses = feList.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareBackupFile(backupFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "হিসাব খাতা ব্যাকআপ ফাইল")
                putExtra(Intent.EXTRA_TEXT, "হিসাব খাতা একক অফলাইন ব্যাকআপ ফাইল (${backupFile.name})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "ব্যাকআপ ফাইল সংরক্ষণ বা শেয়ার করুন").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
