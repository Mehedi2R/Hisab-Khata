package com.hisabkhata.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisabkhata.app.data.local.AppLanguageManager
import com.hisabkhata.app.data.local.AppLanguageManager.str
import com.hisabkhata.app.ui.theme.*

private const val PREFS_NAME = "hisab_khata_settings"

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val currentLang by AppLanguageManager.currentLanguage.collectAsStateWithLifecycle()
    val isEn = currentLang == AppLanguageManager.LANG_EN

    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val scrollState = rememberScrollState()

    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Section - Ultra-Clean Compact with Version Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = str("সেটিংস", "Settings"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHead
                    )
                    Text(
                        text = str("অ্যাপ ও ভাষা কনফিগারেশন", "App & Language Configuration"),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "v3.4.8",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed
                    )
                }
            }

            // 1. Language Card with Switcher Style
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderLight))
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
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = TextHead,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Column {
                            Text(
                                text = str("অ্যাপ্লিকেশন ভাষা", "App Language"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHead
                            )
                            Text(
                                text = if (isEn) "English (Active)" else "বাংলা (সক্রিয়)",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Interactive Segmented Pill Switcher
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Bangla Option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isEn) PrimaryRed else Color.Transparent)
                                .clickable {
                                    if (isEn) {
                                        AppLanguageManager.setLanguage(context, AppLanguageManager.LANG_BN)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "বাংলা",
                                fontSize = 11.sp,
                                fontWeight = if (!isEn) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isEn) Color.White else TextMuted
                            )
                        }

                        // English Option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isEn) PrimaryRed else Color.Transparent)
                                .clickable {
                                    if (!isEn) {
                                        AppLanguageManager.setLanguage(context, AppLanguageManager.LANG_EN)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "EN",
                                fontSize = 11.sp,
                                fontWeight = if (isEn) FontWeight.Bold else FontWeight.Medium,
                                color = if (isEn) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            // 3. Premium Gradient Developer Banner (Created By @Mehedi2R)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Mehedi2R"))
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0284C7), Color(0xFF2563EB))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 13.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = str("অ্যাপ নির্মাতা ও ডেভেলপার", "Official Developer"),
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Created By @Mehedi2R",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Telegram",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }
        }
    }

    // Language Selection Dialog - বাংলা (ডিফল্ট) & English
    if (showLanguageDialog) {
        val languageOptions = listOf(
            AppLanguageManager.LANG_BN to ("বাংলা (ডিফল্ট)" to "Bangla (Default)"),
            AppLanguageManager.LANG_EN to ("English" to "English (US)")
        )
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = str("অ্যাপের ভাষা নির্বাচন", "Select App Language"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextHead
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    languageOptions.forEach { (code, pair) ->
                        val isSelected = currentLang == code
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    AppLanguageManager.setLanguage(context, code)
                                    showLanguageDialog = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFFEF2F2) else Color(0xFFF8FAFC)
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(if (isSelected) PrimaryRed else BorderLight)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = pair.first,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PrimaryRed else TextHead
                                    )
                                    Text(
                                        text = pair.second,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PrimaryRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(
                    onClick = { showLanguageDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(str("বন্ধ করুন", "Close"), fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun SettingsInteractiveRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(17.dp))
            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextHead)
                Text(text = subtitle, fontSize = 10.sp, color = TextMuted)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = badge,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextHead
            )
        }
    }
}
