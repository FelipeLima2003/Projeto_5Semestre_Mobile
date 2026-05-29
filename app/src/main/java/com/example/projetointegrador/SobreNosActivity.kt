// Em app/src/main/java/com/example/projetointegrador/SobreNosActivity.kt
package com.example.projetointegrador

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class SobreNosActivity : BaseActivity() {

    private var loggedInUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sobre_nos)

        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)

        if (loggedInUserId == -1) {
            Toast.makeText(this, "Erro: Usuário não identificado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val iconVoltar: ImageView = findViewById(R.id.img_back_icon)
        val textVoltar: TextView = findViewById(R.id.text_voltar)


        iconVoltar.setOnClickListener {
            finish()
        }

        textVoltar.setOnClickListener {
            finish()
        }


    }
}
