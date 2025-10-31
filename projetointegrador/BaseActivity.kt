package com.example.projetointegrador

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val scale = FontSizeManager.getFontScale(newBase)
        val config = newBase.resources.configuration
        config.fontScale = scale
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}