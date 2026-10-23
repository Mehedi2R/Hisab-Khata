package com.hisabkhata.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.Intent
import com.hisabkhata.app.data.local.AppLanguageManager
import com.hisabkhata.app.data.local.entity.CustomerWithBalance
import com.hisabkhata.app.data.local.entity.TransactionEntity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun toBanglaDigits(input: String): String {
    val enDigits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    val bnDigits = arrayOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
    var res = input
    for (i in 0..9) {
        res = res.replace(enDigits[i], bnDigits[i])
    }
    return res
}

fun formatMoney(amount: Double): String {
    val formatter = if (abs(amount) % 1.0 == 0.0) {
        DecimalFormat("#,##,##0")
    } else {
        DecimalFormat("#,##,##0.##")
    }
    val formatted = formatter.format(abs(amount))
    return "৳ " + toBanglaDigits(formatted)
}

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return toBanglaDigits(sdf.format(Date(millis)))
}

@Composable
fun BalanceBadge(
    netBalance: Double,
    modifier: Modifier = Modifier,
    currency: String = "BDT",
    suppressSettled: Boolean = false
) {
    val currentLang by com.hisabkhata.app.data.local.AppLanguageManager.currentLanguage.collectAsStateWithLifecycle()
    val isReceivable = netBalance > 0.001
    val isPayable = netBalance < -0.001
    val isSettled = !isReceivable && !isPayable

    if (isSettled && suppressSettled) {
        return
    }

    val bgColor = when {
        isReceivable -> Color(0xFFECFDF5)
        isPayable -> Color(0xFFFEF2F2)
        else -> Color(0xFFF1F5F9)
    }

    val textColor = when {
        isReceivable -> Color(0xFF059669)
        isPayable -> Color(0xFFDC2626)
        else -> Color(0xFF64748B)
    }

    val label = when {
        isReceivable -> "${com.hisabkhata.app.data.local.AppLanguageManager.str("পাবো", "Receive")}: ${com.hisabkhata.app.data.local.AppLanguageManager.formatCurrencyLocalized(kotlin.math.abs(netBalance), currency)}"
        isPayable -> "${com.hisabkhata.app.data.local.AppLanguageManager.str("দেবো", "Pay")}: ${com.hisabkhata.app.data.local.AppLanguageManager.formatCurrencyLocalized(kotlin.math.abs(netBalance), currency)}"
        else -> com.hisabkhata.app.data.local.AppLanguageManager.str("পরিশোধ", "Settled")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 🌟 ডিজাইন ১: প্রিমিয়াম ইনভয়েস স্টাইল স্টেটমেন্ট রিপোর্ট জেনারেটর
 */
fun generateCustomerReportText(balance: CustomerWithBalance, txs: List<TransactionEntity>): String {
    val isEn = AppLanguageManager.isEnglish()
    val sb = StringBuilder()
    sb.append(if (isEn) "📋 Hisab Khata Statement\n" else "📋 হিসাব খাতা বিবরণী\n")
    sb.append("${if (isEn) "👤 Customer: " else "👤 গ্রাহক: "}${balance.customer.name}\n")
    if (balance.customer.phone.isNotBlank()) {
        sb.append("${if (isEn) "📞 Mobile: " else "📞 মোবাইল: "}${balance.customer.phone}\n")
    }
    sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    sb.append(if (isEn) "📊 Current Balance:\n" else "📊 বর্তমান ব্যালেন্স:\n")
    if (balance.hasUsd) {
        val usdFmt = if (abs(balance.netBalanceUsd) % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", balance.netBalanceUsd)
        } else {
            String.format(Locale.US, "%.2f", balance.netBalanceUsd)
        }
        sb.append("💵 USD: $ $usdFmt\n")
    }
    sb.append("৳ BDT: ${formatMoney(balance.netBalanceBdt)}\n\n")

    sb.append(if (isEn) "📈 Summary (BDT):\n" else "📈 সারসংক্ষেপ (BDT):\n")
    sb.append("${if (isEn) "🔻 Total Given: " else "🔻 মোট দেওয়া: "}${formatMoney(balance.totalDebitBdt)}\n")
    sb.append("${if (isEn) "✅ Total Received: " else "✅ মোট জমা: "}${formatMoney(balance.totalCreditBdt)}\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━\n")

    if (txs.isNotEmpty()) {
        sb.append(if (isEn) "📝 Transactions:\n" else "📝 লেনদেনসমূহ:\n")
        txs.take(15).forEach { tx ->
            val isCredit = tx.type == TransactionEntity.TYPE_CREDIT
            val sign = if (isCredit) "➕ " else "➖ "
            val tag = if (isCredit) {
                if (isEn) "(Received)" else "(জমা)"
            } else {
                if (isEn) "(Given)" else "(দেওয়া)"
            }
            val amtStr = if (tx.currency.equals("USD", ignoreCase = true)) {
                val f = if (abs(tx.amount) % 1.0 == 0.0) String.format(Locale.US, "%.0f", tx.amount) else String.format(Locale.US, "%.2f", tx.amount)
                "$ $f"
            } else {
                formatMoney(tx.amount)
            }
            sb.append("$sign$amtStr $tag 💰")
            if (tx.note.isNotBlank()) {
                sb.append(" • ${tx.note}")
            }
            sb.append("\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    }
    sb.append(if (isEn) "📱 Generated by Hisab Khata App" else "📱 হিসাব খাতা অ্যাপ দ্বারা প্রস্তুতকৃত")
    return sb.toString()
}

fun shareReportText(context: Context, text: String, title: String = "হিসাব বিবরণী শেয়ার") {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
}
