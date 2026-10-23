package com.hisabkhata.app.data.local

import android.content.Context
import com.hisabkhata.app.ui.components.toBanglaDigits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object AppLanguageManager {
    const val LANG_BN = "bn"
    const val LANG_EN = "en"

    private val banglaMonths = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    private const val PREFS_NAME = "hisab_khata_settings"
    private const val KEY_LANG = "app_language"

    private val _currentLanguage = MutableStateFlow(LANG_BN)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLang = prefs.getString(KEY_LANG, LANG_BN) ?: LANG_BN
        _currentLanguage.value = if (savedLang == LANG_EN) LANG_EN else LANG_BN
    }

    fun setLanguage(context: Context, lang: String) {
        _currentLanguage.value = lang
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    fun isEnglish(): Boolean = _currentLanguage.value == LANG_EN

    fun str(bn: String, en: String): String {
        return if (isEnglish()) en else bn
    }

    fun formatMoneyLocalized(amount: Double): String {
        return formatCurrencyLocalized(amount, "BDT")
    }

    fun formatCurrencyLocalized(amount: Double, currency: String = "BDT"): String {
        val formatter = if (abs(amount) % 1.0 == 0.0) {
            DecimalFormat("#,##,##0")
        } else {
            DecimalFormat("#,##,##0.##")
        }
        val formatted = formatter.format(abs(amount))
        val symbol = if (currency.equals("USD", ignoreCase = true)) "$" else "৳"
        return if (isEnglish()) "$symbol $formatted" else "$symbol " + toBanglaDigits(formatted)
    }

    fun formatDigitsLocalized(input: String): String {
        return if (isEnglish()) input else toBanglaDigits(input)
    }

    fun formatDateLocalized(millis: Long): String {
        return if (isEnglish()) {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)
            sdf.format(Date(millis))
        } else {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            toBanglaDigits(sdf.format(Date(millis)))
        }
    }

    fun formatDateSimpleLocalized(millis: Long): String {
        return if (isEnglish()) {
            val sdf = SimpleDateFormat("dd MMM", Locale.ENGLISH)
            sdf.format(Date(millis))
        } else {
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val dayStr = if (day < 10) "0$day" else "$day"
            "${toBanglaDigits(dayStr)} ${banglaMonths.getOrElse(month) { "" }}"
        }
    }
}
