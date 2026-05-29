package com.example.projetointegrador

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val scale = AppPreferences.getFontScale(newBase)
        val config = newBase.resources.configuration
        config.fontScale = scale
        val newContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(newContext)
    }
    fun applyAndSaveFontSize(scale: Float) {

        AppPreferences.saveFontScale(this, scale)

        recreate()
    }
}
