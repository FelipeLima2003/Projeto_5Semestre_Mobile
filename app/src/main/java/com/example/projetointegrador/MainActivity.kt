package com.example.projetointegrador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.edit_email)
        passwordEditText = findViewById(R.id.edit_senha)
        val loginButton: LinearLayout = findViewById(R.id.containerButtonConfirmar)

        loginButton.setOnClickListener {
            realizarLogin()
        }
    }

    private fun realizarLogin() {
        val email = emailEditText.text.toString().trim()
        val senha = passwordEditText.text.toString().trim()


        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(email, senha)
                if (response.isSuccessful) {
                    val loginResponseList = response.body()
                    if (!loginResponseList.isNullOrEmpty()) {

                        val loginData = loginResponseList[0]
                        Log.d("MainActivity", "Login bem-sucedido para o usuário ID:")
                        val intent = Intent(this@MainActivity, ConsultaActivity::class.java)
                        intent.putExtra("USER_NOME", loginData.usuarioNome)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, "Usuário ou senha inválidos", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Usuário ou senha inválidos", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Falha na chamada de rede", e)
                Toast.makeText(this@MainActivity, "Falha na conexão: Verifique sua internet", Toast.LENGTH_LONG).show()
            }
        }

    }
}