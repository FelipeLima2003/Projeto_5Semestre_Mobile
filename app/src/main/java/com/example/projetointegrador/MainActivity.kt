package com.example.projetointegrador

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.edit_email)
        passwordEditText = findViewById(R.id.edit_senha)
        val loginButton: LinearLayout = findViewById(R.id.containerButtonConfirmar)

        val textIrParaCadastro: TextView = findViewById(R.id.txt_ir_para_cadastro)

        val btnVerSenhaLogin: ImageView = findViewById(R.id.btn_ver_senha_login)
        var isSenhaVisivelLogin = false

        btnVerSenhaLogin.setOnClickListener {
            isSenhaVisivelLogin = !isSenhaVisivelLogin

            if (isSenhaVisivelLogin) {

                passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnVerSenhaLogin.alpha = 1.0f
            } else {

                passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnVerSenhaLogin.alpha = 0.5f
            }

            passwordEditText.setSelection(passwordEditText.text.length)
        }

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
                        AppPreferences.saveToken(this@MainActivity, loginResponse.token)

                        Log.d("MainActivity", "Login bem-sucedido. Token salvo.")

                        val intent = Intent(this@MainActivity, ConsultaActivity::class.java)
                        intent.putExtra("LOGGED_IN_USER_ID", loggedInUserId)
                        intent.putExtra("LOGGED_IN_USER_NAME", loggedInUserName)

                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, getString(R.string.login_erro_invalido), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}
