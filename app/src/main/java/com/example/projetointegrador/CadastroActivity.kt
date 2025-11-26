package com.example.projetointegrador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
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

        val iconVoltar: ImageView = findViewById(R.id.icon_voltar)
        val textVoltar: TextView = findViewById(R.id.text_voltar)

        iconVoltar.setOnClickListener {
            finish()
        }

        textVoltar.setOnClickListener {
            finish()
        }

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
            // Alterado para usar R.string.cadastro_erro_campos_vazios
            Toast.makeText(this, getString(R.string.cadastro_erro_campos_vazios), Toast.LENGTH_SHORT).show()
            return
        }

        if (genero == null) {
            // Alterado para usar R.string.cadastro_erro_genero
            Toast.makeText(this, getString(R.string.cadastro_erro_genero), Toast.LENGTH_SHORT).show()
            return
        }

        if (senha != confirmaSenha) {
            // Alterado para usar R.string.cadastro_erro_senhas_diferentes e R.string.cadastro_erro_senhas_iguais_msg
            Toast.makeText(this, getString(R.string.cadastro_erro_senhas_diferentes), Toast.LENGTH_SHORT).show()
            binding.editConfirmaSenha.error = getString(R.string.cadastro_erro_senhas_iguais_msg)
            return
        }

        val dataNascimentoFormatada = try {
            val partes = nascimentoStr.split("/")
            if (partes.size != 3 || partes[0].length != 2 || partes[1].length != 2 || partes[2].length != 4) {
                throw IllegalArgumentException("Formato de data inválido")
            }
            "${partes[2]}-${partes[1]}-${partes[0]}"
        } catch (e: Exception) {
            // Alterado para usar R.string.cadastro_erro_data_formato e R.string.cadastro_erro_data_msg
            Toast.makeText(this, getString(R.string.cadastro_erro_data_formato), Toast.LENGTH_LONG).show()
            binding.editNascimento.error = getString(R.string.cadastro_erro_data_msg)
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

                    // Alterado para usar R.string.cadastro_sucesso
                    Toast.makeText(this@CadastroActivity, getString(R.string.cadastro_sucesso), Toast.LENGTH_LONG).show()

                    val intent = Intent(this@CadastroActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CadastroActivity", "Erro no cadastro: ${response.code()} - $errorBody")

                    // Alterado para usar R.string.cadastro_erro_prefixo
                    Toast.makeText(this@CadastroActivity, "${getString(R.string.cadastro_erro_prefixo)} $errorBody", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("CadastroActivity", "Falha na chamada de rede", e)
                // Alterado para usar R.string.erro_conexao
                Toast.makeText(this@CadastroActivity, "${getString(R.string.erro_conexao)}: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
