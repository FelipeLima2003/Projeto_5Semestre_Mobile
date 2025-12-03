package com.example.projetointegrador

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AppPreferences {

    private const val PREFS_NAME = "RunConnectPrefs"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val DEFAULT_FONT_SCALE = 1.0f

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveFontScale(context: Context, scale: Float) {
        val editor = getSharedPreferences(context).edit()
        editor.putFloat(KEY_FONT_SCALE, scale)
        editor.apply()
    }

    fun getFontScale(context: Context): Float {
        return getSharedPreferences(context).getFloat(KEY_FONT_SCALE, DEFAULT_FONT_SCALE)
    }

    fun saveToken(context: Context, token: String) {
        getSharedPreferences(context).edit {
            putString(KEY_AUTH_TOKEN, token)
        }
    }


    fun getToken(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_AUTH_TOKEN, null)
    }

    fun clearToken(context: Context) {
        getSharedPreferences(context).edit {
            remove(KEY_AUTH_TOKEN)
        }
    }
}