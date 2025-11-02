package com.example.projetointegrador

import android.content.Context

object FontSizeManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val DEFAULT_FONT_SCALE = 1.0f // 100%

    fun getFontScale(context: Context): Float { /* ... */ return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getFloat(KEY_FONT_SCALE, DEFAULT_FONT_SCALE) }
    fun setFontScale(context: Context, scale: Float) { /* ... */ context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_FONT_SCALE, scale).apply() }
}