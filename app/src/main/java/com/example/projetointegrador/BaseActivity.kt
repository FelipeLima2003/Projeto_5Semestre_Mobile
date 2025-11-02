// Em app/src/main/java/com/example/projetointegrador/BaseActivity.kt
package com.example.projetointegrador

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Carrega a configuração de fonte ANTES da activity ser criada
        val scale = AppPreferences.getFontScale(newBase)
        val config = newBase.resources.configuration
        config.fontScale = scale
        val newContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(newContext)
    }

    // Esta função será chamada pela nossa DialogFragment
    fun applyAndSaveFontSize(scale: Float) {
        // 1. Salva a nova preferência
        AppPreferences.saveFontScale(this, scale)

        // 2. Reinicia a atividade para aplicar a mudança de fonte em toda a UI
        recreate()
    }
}
