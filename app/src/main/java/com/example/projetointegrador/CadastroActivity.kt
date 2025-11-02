package com.example.projetointegrador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.projetointegrador.databinding.ActivityCadastroBinding
import kotlinx.coroutines.launch


class CadastroActivity : BaseActivity() {

    private lateinit var binding: ActivityCadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.containerButtonConfirmar.setOnClickListener {
            realizarCadastro()
        }
    }

    private fun realizarCadastro() {

        val nome = binding.editNome.text.toString().trim()
        val nascimentoStr = binding.editNascimento.text.toString().trim()
        val cpf = binding.editCpf.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val telefone = binding.editTelefone.text.toString().trim()
        val senha = binding.editSenha.text.toString().trim()
        val confirmaSenha = binding.editConfirmaSenha.text.toString().trim()
        val selectedGeneroId = binding.rgGenero.checkedRadioButtonId
        val genero: Genero? = when (selectedGeneroId) {
            R.id.rb_masculino -> Genero.MASCULINO
            R.id.rb_feminino -> Genero.FEMININO
            R.id.rb_outro -> Genero.PREFIRO_NAO_INFORMAR
            else -> null
        }

        if (nome.isEmpty() || nascimentoStr.isEmpty() || cpf.isEmpty() || email.isEmpty() || telefone.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (genero == null) {
            Toast.makeText(this, "Por favor, selecione um gênero", Toast.LENGTH_SHORT).show()
            return
        }

        if (senha != confirmaSenha) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            binding.editConfirmaSenha.error = "As senhas devem ser iguais"
            return
        }

        val dataNascimentoFormatada = try {
            nascimentoStr.split("/").reversed().joinToString("-")
        } catch (e: Exception) {
            Toast.makeText(this, "Formato de data inválido. Use AAAA/MM/DD", Toast.LENGTH_SHORT).show()
            return
        }

        val cadastroRequest = CadastroRequest(
            nome = nome,
            dataNascimento = dataNascimentoFormatada,
            cpf = cpf,
            email = email,
            telefone = telefone,
            genero = genero,
            senha = senha
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.cadastrar(cadastroRequest)

                if (response.isSuccessful && response.body() != null) {
                    val cadastroResponse = response.body()!!
                    Log.d("CadastroActivity", "Cadastro bem-sucedido: ${cadastroResponse.message}")
                    Toast.makeText(this@CadastroActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show()

                    val intent = Intent(this@CadastroActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CadastroActivity", "Erro no cadastro: ${response.code()} - $errorBody")
                    Toast.makeText(this@CadastroActivity, "Erro ao cadastrar: $errorBody", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("CadastroActivity", "Falha na chamada de rede", e)
                Toast.makeText(this@CadastroActivity, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
