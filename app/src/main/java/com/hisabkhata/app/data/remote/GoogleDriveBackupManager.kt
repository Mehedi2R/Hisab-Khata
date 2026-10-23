package com.hisabkhata.app.data.remote

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections

class GoogleDriveBackupManager(private val context: Context) {

    companion object {
        const val BACKUP_MIME_TYPE = "application/json"
        const val DRIVE_BACKUP_FILE_NAME = "HisabKhata_Backup.json"
        const val BACKUP_FOLDER_NAME = "HisabKhata2R_Backups"
    }

    private val driveScope = Scope(DriveScopes.DRIVE_FILE)

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(driveScope)
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && GoogleSignIn.hasPermissions(account, driveScope)) {
            account
        } else {
            null
        }
    }

    fun getSignInIntent(): Intent {
        return getGoogleSignInClient().signInIntent
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            getGoogleSignInClient().signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccount = account.account
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Hisab Khata")
            .build()
    }

    private fun getOrCreateBackupFolder(driveService: Drive): String {
        val query = "mimeType = 'application/vnd.google-apps.folder' and name = '$BACKUP_FOLDER_NAME' and trashed = false"
        val folderList = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val existingFolder = folderList.files?.firstOrNull()
        if (existingFolder != null) {
            return existingFolder.id
        }

        val folderMetadata = com.google.api.services.drive.model.File().apply {
            name = BACKUP_FOLDER_NAME
            mimeType = "application/vnd.google-apps.folder"
        }

        val createdFolder = driveService.files().create(folderMetadata)
            .setFields("id")
            .execute()

        return createdFolder.id
    }

    fun getLastDriveBackupTime(): Long {
        val prefs = context.getSharedPreferences("google_drive_backup_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("last_backup_time", 0L)
    }

    fun setLastDriveBackupTime(timeMillis: Long) {
        val prefs = context.getSharedPreferences("google_drive_backup_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_backup_time", timeMillis).apply()
    }

    suspend fun fetchLatestBackupModifiedTime(): Result<Long?> = withContext(Dispatchers.IO) {
        val account = getSignedInAccount() ?: return@withContext Result.success(null)
        try {
            val driveService = getDriveService(account)
            val folderId = getOrCreateBackupFolder(driveService)
            val fileQuery = "name = '$DRIVE_BACKUP_FILE_NAME' and '$folderId' in parents and trashed = false"
            val fileList = driveService.files().list()
                .setQ(fileQuery)
                .setSpaces("drive")
                .setFields("files(id, name, modifiedTime)")
                .execute()
            val file = fileList.files?.firstOrNull()
            val time = file?.modifiedTime?.value
            if (time != null && time > 0L) {
                setLastDriveBackupTime(time)
            }
            Result.success(time)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadBackupFile(localFile: File): Result<String> = withContext(Dispatchers.IO) {
        val account = getSignedInAccount()
            ?: return@withContext Result.failure(IllegalStateException("Google অ্যাকাউন্টে লগইন করা নেই। প্রথমে সাইন ইন করুন।"))

        if (!localFile.exists() || localFile.length() <= 0) {
            return@withContext Result.failure(IllegalStateException("লোকাল ব্যাকআপ ফাইল পাওয়া যায়নি।"))
        }

        try {
            val driveService = getDriveService(account)
            val folderId = getOrCreateBackupFolder(driveService)

            // Check if an existing backup file is present in our folder
            val fileQuery = "name = '$DRIVE_BACKUP_FILE_NAME' and '$folderId' in parents and trashed = false"
            val fileList = driveService.files().list()
                .setQ(fileQuery)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            val existingFile = fileList.files?.firstOrNull()
            val mediaContent = FileContent(BACKUP_MIME_TYPE, localFile)

            if (existingFile != null) {
                // Update existing file (overwrite with latest backup)
                val updateMetadata = com.google.api.services.drive.model.File()
                val updatedFile = driveService.files().update(existingFile.id, updateMetadata, mediaContent)
                    .setFields("id, name, modifiedTime")
                    .execute()
                val modTime = updatedFile.modifiedTime?.value ?: System.currentTimeMillis()
                setLastDriveBackupTime(modTime)
                Result.success(updatedFile.id)
            } else {
                // Create a new file in the folder
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = DRIVE_BACKUP_FILE_NAME
                    parents = Collections.singletonList(folderId)
                }
                val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, modifiedTime")
                    .execute()
                val modTime = uploadedFile.modifiedTime?.value ?: System.currentTimeMillis()
                setLastDriveBackupTime(modTime)
                Result.success(uploadedFile.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadLatestBackup(): Result<String> = withContext(Dispatchers.IO) {
        val account = getSignedInAccount()
            ?: return@withContext Result.failure(IllegalStateException("Google অ্যাকাউন্টে লগইন করা নেই।"))

        try {
            val driveService = getDriveService(account)
            val folderId = getOrCreateBackupFolder(driveService)

            val fileQuery = "name = '$DRIVE_BACKUP_FILE_NAME' and '$folderId' in parents and trashed = false"
            val fileList = driveService.files().list()
                .setQ(fileQuery)
                .setSpaces("drive")
                .setFields("files(id, name, modifiedTime)")
                .execute()

            val file = fileList.files?.firstOrNull()
                ?: return@withContext Result.failure(IllegalStateException("Google Drive-এ কোনো সংরক্ষিত ব্যাকআপ পাওয়া যায়নি।"))

            file.modifiedTime?.value?.let { modTime ->
                if (modTime > 0L) setLastDriveBackupTime(modTime)
            }

            val outputStream = java.io.ByteArrayOutputStream()
            driveService.files().get(file.id).executeMediaAndDownloadTo(outputStream)
            val content = outputStream.toString("UTF-8")
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
