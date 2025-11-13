package com.example.projetointegrador

import android.os.Bundle
import android.util.Log
import android.view.View // <<< IMPORTAR VIEW
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.projetointegrador.databinding.ActivityPerfilBinding
import kotlinx.coroutines.launch

class PerfilActivity : BaseActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private var userProfileId: Int = -1
    private var loggedInUserId: Int = -1 // Variável para o usuário logado
    private var isMyProfile: Boolean = false // Flag para saber se é o próprio perfil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // *** PASSO 1: RECEBER AMBOS OS IDs ***
        userProfileId = intent.getIntExtra("USER_PROFILE_ID", -1)
        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)

        if (userProfileId == -1 || loggedInUserId == -1) {
            Toast.makeText(this, "Erro: Dados do usuário incompletos.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // *** PASSO 2: COMPARAR OS IDs ***
        isMyProfile = (userProfileId == loggedInUserId)

        setupListeners()
        buscarDadosDoPerfil()
    }

    private fun setupListeners() {
        binding.iconVoltar.setOnClickListener { finish() }
        binding.textVoltar.setOnClickListener { finish() }

        // O listener do botão salvar só fará algo se o botão estiver visível
        binding.btnSalvarPerfil.setOnClickListener {
            if (isMyProfile) {
                salvarDescricao()
            }
        }
    }

    private fun buscarDadosDoPerfil() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUsuarioById(userProfileId)
                if (response.isSuccessful) {
                    response.body()?.let { perfil ->
                        preencherDadosNaTela(perfil)
                    } ?: Toast.makeText(this@PerfilActivity, "Usuário não encontrado.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("PerfilActivity", "Erro ao buscar perfil: ${response.code()}")
                    Toast.makeText(this@PerfilActivity, "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PerfilActivity", "Falha na chamada de rede", e)
                Toast.makeText(this@PerfilActivity, "Falha na conexão.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun preencherDadosNaTela(perfil: PerfilUsuarioResponse) {
        binding.txtPerfilNome.text = perfil.nome
        binding.txtPerfilGenero.text = perfil.genero.replaceFirstChar { it.titlecase() }

        // *** PASSO 3: CONTROLAR A VISIBILIDADE DOS COMPONENTES ***
        if (isMyProfile) {
            // É o meu perfil: mostro o campo de edição e o botão de salvar
            binding.editPerfilDescricao.visibility = View.VISIBLE
            binding.btnSalvarPerfil.visibility = View.VISIBLE
            binding.txtPerfilDescricao.visibility = View.GONE // Escondo o campo de texto puro

            binding.editPerfilDescricao.setText(perfil.descricao ?: "")
        } else {
            // É o perfil de outra pessoa: mostro apenas o texto da descrição
            binding.editPerfilDescricao.visibility = View.GONE
            binding.btnSalvarPerfil.visibility = View.GONE
            binding.txtPerfilDescricao.visibility = View.VISIBLE

            binding.txtPerfilDescricao.text = perfil.descricao ?: "Este usuário ainda não escreveu sobre si..."
        }
    }

    private fun salvarDescricao() {
        val novaDescricao = binding.editPerfilDescricao.text.toString().trim()
        val request = UpdateDescricaoRequest(descricao = novaDescricao)

        lifecycleScope.launch {
            try {
                // A API de update usa o userProfileId, que neste caso é o mesmo do loggedInUserId
                val response = RetrofitClient.apiService.updateDescricao(userProfileId, request)
                if (response.isSuccessful) {
                    Toast.makeText(this@PerfilActivity, "Descrição salva com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PerfilActivity", "Erro ao salvar descrição: ${response.code()} - $errorBody")
                    Toast.makeText(this@PerfilActivity, "Não foi possível salvar: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("PerfilActivity", "Falha de rede ao salvar", e)
                Toast.makeText(this@PerfilActivity, "Falha na conexão.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
