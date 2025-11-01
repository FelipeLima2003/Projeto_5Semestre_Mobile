package com.example.projetointegrador
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ConsultaActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta)

        // 1. Encontre o botão pelo ID
        val button = findViewById<LinearLayout>(R.id.containerButtonConfirmar)

        // 2. Configure o listener de clique para mostrar o diálogo
        button.setOnClickListener {
            val dialog = FontSizeDialogFragment()
            dialog.show(supportFragmentManager, FontSizeDialogFragment.TAG)
        }

    }


}