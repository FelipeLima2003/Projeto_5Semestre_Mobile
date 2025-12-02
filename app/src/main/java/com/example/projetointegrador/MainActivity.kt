package com.example.projetointegrador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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

        val textIrParaCadastro: TextView = findViewById(R.id.txt_ir_para_cadastro)

        textIrParaCadastro.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            realizarLogin()
        }
    }

    private fun realizarLogin() {
        val email = emailEditText.text.toString().trim()
        val senha = passwordEditText.text.toString().trim()

        if (email.isEmpty() || senha.isEmpty()) {

            Toast.makeText(this, getString(R.string.login_erro_vazio), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, senha)
                val response = RetrofitClient.apiService.login(request)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {

                        val loggedInUserId = loginResponse.usuarioId
                        val loggedInUserName = loginResponse.usuarioNome
                        RetrofitClient.setAuthToken(loginResponse.token)

                        Log.d("MainActivity", "Login bem-sucedido para o usuário ID: $loggedInUserId")

                        val intent = Intent(this@MainActivity, ConsultaActivity::class.java)

                        intent.putExtra("LOGGED_IN_USER_ID", loggedInUserId)
                        intent.putExtra("LOGGED_IN_USER_NAME", loggedInUserName)

                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, getString(R.string.login_erro_invalido), Toast.LENGTH_LONG).show()
                    }


                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("MainActivity", "Erro de login (${response.code()}): $errorBody")

                    Toast.makeText(this@MainActivity, getString(R.string.login_erro_invalido), Toast.LENGTH_LONG).show()
                }


            } catch (e: Exception) {
                Log.e("MainActivity", "Falha na chamada de rede", e)

                Toast.makeText(this@MainActivity, getString(R.string.erro_conexao), Toast.LENGTH_LONG).show()
            }
        }
    }
}
