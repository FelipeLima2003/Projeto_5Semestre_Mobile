package com.example.projetointegrador

import CorridaResponse
import android.content.Intent
import android.net.Uri
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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class PerfilActivity : BaseActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private var userProfileId: Int = -1
    private var loggedInUserId: Int = -1
    private var isMyProfile: Boolean = false

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // Mostra visualmente antes de enviar
            Glide.with(this)
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.imgPerfilAvatar)

            // Inicia o processo de upload
            uploadEAtualizarFoto(uri)
        }
    }
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


        setupUI()
        setupListeners()
        buscarDadosDoPerfil()
        buscarHistoricoDeCorridas()

    }

    private fun setupUI() {
        binding.iconVoltar.setOnClickListener { finish() }
        binding.textVoltar.setOnClickListener { finish() }

        // 2. SÓ MOSTRA O BOTÃO DE EDITAR SE FOR O MEU PERFIL
        if (isMyProfile) {
            binding.btnEditarFoto.visibility = View.VISIBLE

            // Clique na foto ou no ícone abre a galeria
            binding.btnEditarFoto.setOnClickListener { abrirGaleria() }
            binding.imgPerfilAvatar.setOnClickListener { abrirGaleria() }
        } else {
            binding.btnEditarFoto.visibility = View.GONE
            // Remove o clique se não for meu perfil
            binding.imgPerfilAvatar.setOnClickListener(null)
        }
    }

    private fun abrirGaleria() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // 3. FUNÇÃO QUE FAZ TUDO: UPLOAD ARQUIVO -> PEGA URL -> ATUALIZA BANCO
    private fun uploadEAtualizarFoto(uri: Uri) {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@PerfilActivity, "Atualizando imagem...", Toast.LENGTH_SHORT).show()

                // A. Transforma URI em Arquivo (igual Cadastro)
                val imagemPart = prepararImagemParaUpload(uri)

                if (imagemPart != null) {
                    // B. Envia para o endpoint de Upload de Arquivo
                    val responseUpload = RetrofitClient.apiService.uploadImagem(imagemPart)

                    if (responseUpload.isSuccessful) {
                        // Limpa a URL recebida
                        val novaUrl = responseUpload.body()?.toString()?.replace("\"", "")?.trim()

                        if (!novaUrl.isNullOrEmpty()) {
                            // C. Chama o endpoint novo para salvar no banco
                            atualizarUrlNoBanco(novaUrl)
                        }
                    } else {
                        Toast.makeText(this@PerfilActivity, "Erro ao enviar arquivo", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("PerfilActivity", "Erro no upload", e)
                Toast.makeText(this@PerfilActivity, "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. CHAMA O ENDPOINT (@PUT)
    private suspend fun atualizarUrlNoBanco(novaUrl: String) {
        try {
            val body = mapOf("imagemUrl" to novaUrl)
            val response = RetrofitClient.apiService.atualizarImagemPerfil(userProfileId, body)

            if (response.isSuccessful) {
                Toast.makeText(this, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao salvar alteração", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("PerfilActivity", "Erro ao atualizar banco", e)
        }
    }

    // 5. FUNÇÃO AUXILIAR (Cópia do CadastroActivity)
    private fun prepararImagemParaUpload(uri: Uri): MultipartBody.Part? {
        try {
            val fileDir = applicationContext.filesDir
            val file = File(fileDir, "perfil_temp.jpg")
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData("file", file.name, requestFile)
        } catch (e: Exception) {
            return null
        }
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



        // 1. Limpeza: Remove aspas e espaços que podem vir sujos da API
        val urlLimpa = perfil.imagemUrl?.replace("\"", "")?.trim()

        Log.d("DEBUG_IMAGEM", "Nome: ${perfil.nome}")
        Log.d("DEBUG_IMAGEM", "URL Original: '${perfil.imagemUrl}'")
        Log.d("DEBUG_IMAGEM", "URL Limpa: '$urlLimpa'")

        if (!urlLimpa.isNullOrEmpty()) {

            val urlCompleta = if (urlLimpa.startsWith("http")) {
                urlLimpa
            } else {

                "https://runconnect-api.onrender.com/uploads/$urlLimpa"
            }

            Log.d("DEBUG_IMAGEM", "Carregando no Glide: $urlCompleta")

            com.bumptech.glide.Glide.with(this)
                .load(urlCompleta)
                .apply(com.bumptech.glide.request.RequestOptions.circleCropTransform())
                .placeholder(R.drawable.logo) // <--- MUDADO PARA LOGO DO APP
                .error(R.drawable.logo)       // <--- MUDADO PARA LOGO DO APP
                .into(binding.imgPerfilAvatar)
        } else {
            Log.d("DEBUG_IMAGEM", "Sem URL. Usando logo padrão.")
            binding.imgPerfilAvatar.setImageResource(R.drawable.logo) // <--- MUDADO PARA LOGO DO APP
        }



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
