package com.example.projetointegrador

import CorridaResponse
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.databinding.ActivityPerfilBinding
import kotlinx.coroutines.launch

class PerfilActivity : BaseActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private var userProfileId: Int = -1
    private var loggedInUserId: Int = -1
    private var isMyProfile: Boolean = false

    private lateinit var corridaAdapter: CorridaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userProfileId = intent.getIntExtra("USER_PROFILE_ID", -1)
        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)

        if (userProfileId == -1 || loggedInUserId == -1) {
            Toast.makeText(this, "Erro: Dados do usuário incompletos.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        isMyProfile = (userProfileId == loggedInUserId)

        setupListeners()
        buscarDadosDoPerfil()
        buscarHistoricoDeCorridas()

    }

    override fun onResume() {
        super.onResume()
        verificarEventosSalvos()
    }

    private fun verificarEventosSalvos() {
        if (!isMyProfile) return

        val prefs = getSharedPreferences("DadosApp", MODE_PRIVATE)
        val nomeEvento = prefs.getString("EVENTO_NOME", null)
        val detalhesEvento = prefs.getString("EVENTO_DETALHES", null)

        val cardEvento = findViewById<androidx.cardview.widget.CardView>(R.id.card_proximo_evento)
        val txtNome = findViewById<TextView>(R.id.txt_evento_nome_perfil)
        val txtDetalhes = findViewById<TextView>(R.id.txt_evento_detalhes_perfil)

        if (nomeEvento != null && cardEvento != null) {

            cardEvento.visibility = View.VISIBLE
            txtNome.text = nomeEvento
            txtDetalhes.text = detalhesEvento
        }
    }

    private fun setupListeners() {
        binding.iconVoltar.setOnClickListener { finish() }
        binding.textVoltar.setOnClickListener { finish() }

        binding.btnSalvarPerfil.setOnClickListener {
            if (isMyProfile) {
                salvarDescricao()
            }
        }

        binding.btnExcluirPerfil.setOnClickListener {
            if (isMyProfile) {
                mostrarDialogoDeConfirmacao()
            }
        }
    }

    private fun buscarDadosDoPerfil() {
        lifecycleScope.launch {
            try {
                Log.d("DEBUG_PERFIL", "Tentando buscar perfil para o ID: $userProfileId")

                val response = RetrofitClient.apiService.getUsuarioById(userProfileId)

                // *** LOG CRUCIAL: MOSTRA A URL EXATA QUE O APP MONTOU ***
                Log.e("DEBUG_PERFIL", "URL chamada (Perfil): ${response.raw().request.url}")

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

    private fun buscarHistoricoDeCorridas() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getCorridasDoUsuario(userProfileId)

                // *** LOG CRUCIAL: MOSTRA A URL EXATA QUE O APP MONTOU ***
                Log.e("DEBUG_PERFIL", "URL chamada (Corridas): ${response.raw().request.url}")

                if (response.isSuccessful) {
                    val listaDeCorridas = response.body()
                    if (!listaDeCorridas.isNullOrEmpty()) {
                        setupRecyclerViewCorridas(listaDeCorridas)
                    } else {
                        Log.d("PerfilActivity", "Nenhum histórico de corridas encontrado para o usuário $userProfileId")
                    }
                } else {
                    Log.e("PerfilActivity", "Erro ao buscar histórico de corridas: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PerfilActivity", "Falha de rede ao buscar histórico de corridas", e)
            }
        }
    }

    private fun setupRecyclerViewCorridas(corridas: List<CorridaResponse>) {
        corridaAdapter = CorridaAdapter(corridas)
        binding.recyclerViewCorridas.apply {
            layoutManager = LinearLayoutManager(this@PerfilActivity)
            adapter = corridaAdapter
            isNestedScrollingEnabled = false
        }
    }


    private fun preencherDadosNaTela(perfil: UsuarioPublicoResponse) {
        binding.txtPerfilNome.text = perfil.nome
        binding.txtPerfilGenero.text = perfil.genero.replaceFirstChar { it.titlecase() }

        if (isMyProfile) {
            binding.editPerfilDescricao.visibility = View.VISIBLE
            binding.btnSalvarPerfil.visibility = View.VISIBLE
            binding.btnExcluirPerfil.visibility = View.VISIBLE
            binding.txtPerfilDescricao.visibility = View.GONE
            binding.editPerfilDescricao.setText(perfil.descricao ?: "")
        } else {
            binding.editPerfilDescricao.visibility = View.GONE
            binding.btnSalvarPerfil.visibility = View.GONE
            binding.btnExcluirPerfil.visibility = View.GONE
            binding.txtPerfilDescricao.visibility = View.VISIBLE
            binding.txtPerfilDescricao.text = perfil.descricao ?: "Este usuário ainda não escreveu sobre si."
        }
    }

    private fun salvarDescricao() {
        val novaDescricao = binding.editPerfilDescricao.text.toString().trim()
        val request = UpdateDescricaoRequest(descricao = novaDescricao)

        lifecycleScope.launch {
            try {

                val response = RetrofitClient.apiService.updateDescricao(userProfileId, request)

                if (response.isSuccessful) {
                    Toast.makeText(this@PerfilActivity, "Descrição salva com sucesso!", Toast.LENGTH_SHORT).show()
                } else {

                    Log.e("PerfilActivity", "Erro ao salvar: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@PerfilActivity, "Erro ao salvar: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("PerfilActivity", "Falha de rede", e)
                Toast.makeText(this@PerfilActivity, "Falha na conexão.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoDeConfirmacao() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Conta")
            .setMessage("Você tem certeza que deseja excluir sua conta? Esta ação é permanente e não pode ser desfeita.")
            .setPositiveButton("Sim, Excluir") { _, _ ->
                excluirConta()
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun excluirConta() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.excluirUsuario(loggedInUserId)
                if (response.isSuccessful) {
                    Toast.makeText(this@PerfilActivity, "Sua conta foi excluída com sucesso.", Toast.LENGTH_LONG).show()
                    AppPreferences.saveFontScale(this@PerfilActivity, 1.0f)

                    val intent = Intent(this@PerfilActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PerfilActivity", "Erro ao excluir conta (${response.code()}): $errorBody")
                    Toast.makeText(this@PerfilActivity, "Não foi possível excluir a conta: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("PerfilActivity", "Falha de rede ao tentar excluir", e)
                Toast.makeText(this@PerfilActivity, "Falha na conexão.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
