package com.example.smartcard.utils

import android.content.Context
import android.util.Log
import com.example.smartcard.SmartCartLogTags
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LanguageManager {

    private const val PREFS_NAME = "smartcart_prefs"
    private const val KEY_LANGUAGE = "language_code"

    private val _language = MutableStateFlow(readLanguageFromSystem())
    val language: StateFlow<String> = _language.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return

        synchronized(this) {
            if (initialized) return

            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val persisted = prefs.getString(KEY_LANGUAGE, null)
            val resolved = normalizeLanguage(persisted ?: readLanguageFromSystem())

            if (persisted == null) {
                prefs.edit().putString(KEY_LANGUAGE, resolved).apply()
            }

            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(resolved)
            )
            _language.value = resolved
            Log.d(SmartCartLogTags.LANG, "init language=$resolved")
            initialized = true
        }
    }

    fun setLanguage(context: Context, langCode: String) {
        val normalized = normalizeLanguage(langCode)

        if (_language.value == normalized) {
            Log.d(SmartCartLogTags.LANG, "setLanguage ignored same language=$normalized")
            return
        }

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(normalized)
        )
        _language.value = normalized

        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_LANGUAGE, normalized)
            ?.apply()

        Log.d(SmartCartLogTags.LANG, "language_changed new=$normalized")
    }

    fun getLanguage(): String {
        return _language.value
    }

    private fun readLanguageFromSystem(): String {
        val systemLang = Locale.getDefault().language
        return normalizeLanguage(systemLang)
    }

    private fun normalizeLanguage(value: String): String {
        return when {
            value.startsWith("ru", ignoreCase = true) -> "ru"
            value.startsWith("kk", ignoreCase = true) -> "kk"
            value.startsWith("en", ignoreCase = true) -> "en"
            else -> "en"
        }
    }
}