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
        setContentView(R.layout.activity_login) // Assume que o layout de login é activity_login.xml

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

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha o e-mail e a senha.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(email, senha)
                if (response.isSuccessful) {
                    val loginResponseList = response.body()
                    if (!loginResponseList.isNullOrEmpty()) {
                        val loginData = loginResponseList[0]

                        val loggedInUserId = loginData.usuarioId
                        val loggedInUserName = loginData.usuarioNome

                        Log.d("MainActivity", "Login bem-sucedido para o usuário ID: $loggedInUserId")

                        val intent = Intent(this@MainActivity, ConsultaActivity::class.java)

                        intent.putExtra("LOGGED_IN_USER_ID", loggedInUserId)
                        intent.putExtra("LOGGED_IN_USER_NAME", loggedInUserName)

                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "Usuário ou senha inválidos", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("MainActivity", "Erro de login (${response.code()}): $errorBody")
                    Toast.makeText(this@MainActivity, "Usuário ou senha inválidos", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Falha na chamada de rede", e)
                Toast.makeText(this@MainActivity, "Falha na conexão: Verifique sua internet", Toast.LENGTH_LONG).show()
            }
        }
    }
}
