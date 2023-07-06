package uz.mobile.id.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class PreferenceUtil {
  private val LOCALE = "LOCALE"
  private val FIRST_OPEN = "FIRST_OPEN"

  companion object {
    private var instance: PreferenceUtil? = null
    fun getInstance(context: Context): PreferenceUtil {
      if (instance == null) instance = PreferenceUtil(context)
      return requireNotNull(instance)
    }
  }

  private lateinit var preferences: SharedPreferences

  constructor(context: Context){
    preferences = context.getSharedPreferences("iduz", MODE_PRIVATE)
  }

  fun setLanguage(locale: String) {
    preferences.edit().putString(LOCALE, locale).commit()
  }

  fun getLanguage() = preferences.getString(LOCALE, "uz")!!

  fun setIsFirstOpen(isFirstOpen: Boolean) {
    preferences.edit().putBoolean(FIRST_OPEN, isFirstOpen).commit()
  }

  fun isFirstOpen() = preferences.getBoolean(FIRST_OPEN, true)
}