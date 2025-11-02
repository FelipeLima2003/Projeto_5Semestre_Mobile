// Em app/src/main/java/com/example/projetointegrador/SobreNosActivity.kt
package com.example.projetointegrador

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Futuramente, você pode fazer esta classe herdar de BaseActivity para ter a função de mudar a fonte.
// Por agora, vamos focar no botão de voltar.
class SobreNosActivity : BaseActivity() {

    private var loggedInUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sobre_nos)

        // 1. Recupera o ID do usuário logado que foi passado pelo Intent
        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)

        // Verificação de segurança, caso a tela seja aberta sem um usuário logado
        if (loggedInUserId == -1) {
            Toast.makeText(this, "Erro: Usuário não identificado.", Toast.LENGTH_SHORT).show()
            finish() // Fecha a activity se não houver um ID válido
            return
        }

        // 2. Encontrar os componentes do botão "VOLTAR" no layout
        val iconVoltar: ImageView = findViewById(R.id.img_back_icon)
        val textVoltar: TextView = findViewById(R.id.text_voltar)

        // 3. Configurar os listeners de clique
        iconVoltar.setOnClickListener {
            finish() // Fecha a tela atual e retorna para a anterior (ConsultaActivity)
        }

        textVoltar.setOnClickListener {
            finish() // Mesma ação para o texto
        }

        // Aqui você pode adicionar lógica para os links de redes sociais, etc.
    }
}
