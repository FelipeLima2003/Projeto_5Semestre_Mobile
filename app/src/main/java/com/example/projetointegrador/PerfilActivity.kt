package com.example.projetointegrador

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.example.projetointegrador.databinding.ActivityPerfilBinding
import kotlinx.coroutines.launch
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
    private lateinit var corridaAdapter: CorridaAdapter

    // SELETOR DE FOTOS
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // 1. Feedback visual IMEDIATO (mostra a foto local enquanto envia)
            Glide.with(this)
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.imgPerfilAvatar)

            // 2. Inicia o processo de upload e salvamento
            uploadEAtualizarFoto(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userProfileId = intent.getIntExtra("USER_PROFILE_ID", -1)
        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)

        // Se userProfileId não veio, assume que é o próprio usuário logado
        if (userProfileId == -1) userProfileId = loggedInUserId

        if (loggedInUserId == -1) {
            Toast.makeText(this, getString(R.string.erro_login_invalido), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        isMyProfile = (userProfileId == loggedInUserId)

        setupUI()
        setupListeners()
        buscarDadosDoPerfil() // Carrega dados iniciais
        buscarHistoricoDeCorridas()
    }

    override fun onResume() {
        super.onResume()
        verificarEventosSalvos()
    }

    private fun setupUI() {
        binding.iconVoltar.setOnClickListener { finish() }
        binding.textVoltar.setOnClickListener { finish() }

        if (isMyProfile) {
            binding.btnEditarFoto.visibility = View.VISIBLE
            binding.btnEditarFoto.setOnClickListener { abrirGaleria() }
            binding.imgPerfilAvatar.setOnClickListener { abrirGaleria() }
        } else {
            binding.btnEditarFoto.visibility = View.GONE
            binding.imgPerfilAvatar.setOnClickListener(null)
        }
    }

    private fun setupListeners() {
        binding.btnSalvarPerfil.setOnClickListener {
            if (isMyProfile) salvarDescricao()
        }
        binding.btnExcluirPerfil.setOnClickListener {
            if (isMyProfile) mostrarDialogoDeConfirmacao()
        }
    }

    private fun abrirGaleria() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // --- FUNÇÃO CENTRAL DE UPLOAD ---
    private fun uploadEAtualizarFoto(uri: Uri) {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@PerfilActivity, getString(R.string.perfil_enviando_imagem), Toast.LENGTH_SHORT).show()

                // A. Prepara o arquivo
                val imagemPart = prepararImagemParaUpload(uri)

                if (imagemPart != null) {
                    // B. Faz o Upload Físico
                    val responseUpload = RetrofitClient.apiService.uploadImagem(imagemPart)

                    if (responseUpload.isSuccessful) {
                        // Pega o nome do arquivo retornado (ex: "foto123.jpg") e limpa aspas
                        val nomeImagemServidor = responseUpload.body()?.toString()?.replace("\"", "")?.trim()

                        if (!nomeImagemServidor.isNullOrEmpty()) {
                            Log.d("PerfilActivity", "Upload OK. Nome: $nomeImagemServidor")

                            // C. Atualiza o Banco de Dados (PUT)
                            val body = mapOf("imagemUrl" to nomeImagemServidor)

                            // O endpoint retorna o USUÁRIO ATUALIZADO
                            val responseUpdate = RetrofitClient.apiService.atualizarImagemPerfil(userProfileId, body)

                            if (responseUpdate.isSuccessful && responseUpdate.body() != null) {
                                Toast.makeText(this@PerfilActivity, getString(R.string.perfil_foto_sucesso), Toast.LENGTH_SHORT).show()

                                // D. Atualiza a tela DIRETAMENTE com o objeto retornado
                                // Isso evita delay e garante que estamos vendo o que foi salvo
                                preencherDadosNaTela(responseUpdate.body()!!)

                            } else {
                                Log.e("PerfilActivity", "Erro no PUT: ${responseUpdate.code()}")
                                Toast.makeText(this@PerfilActivity, getString(R.string.perfil_erro_salvar), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@PerfilActivity, getString(R.string.perfil_erro_servidor_img), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val msgErro = String.format(getString(R.string.perfil_erro_upload), responseUpload.code())
                        Toast.makeText(this@PerfilActivity, msgErro, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("PerfilActivity", "Exceção no processo", e)
                Toast.makeText(this@PerfilActivity, getString(R.string.erro_conexao), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepararImagemParaUpload(uri: Uri): MultipartBody.Part? {
        try {
            val fileDir = applicationContext.filesDir
            val file = File(fileDir, "temp_perfil.jpg")
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData("file", file.name, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun buscarDadosDoPerfil() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUsuarioById(userProfileId)
                if (response.isSuccessful) {
                    response.body()?.let { preencherDadosNaTela(it) }
                } else {
                    Toast.makeText(this@PerfilActivity, getString(R.string.perfil_erro_carregar), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PerfilActivity", "Erro Rede", e)
                Toast.makeText(this@PerfilActivity, getString(R.string.erro_conexao), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun preencherDadosNaTela(perfil: UsuarioPublicoResponse) {
        binding.txtPerfilNome.text = perfil.nome
        binding.txtPerfilGenero.text = perfil.genero.replaceFirstChar { it.titlecase() }

        // --- Lógica de Carregamento de Imagem (Atualizada) ---
        val urlString = perfil.imagemUrl?.replace("\"", "")?.trim()

        if (!urlString.isNullOrEmpty()) {
            val urlFinal = if (urlString.startsWith("http")) {
                urlString
            } else {
                "https://runconnect-api.onrender.com/uploads/$urlString"
            }

            Log.d("PerfilActivity", "Carregando: $urlFinal")

            // Assinatura única baseada no tempo para FORÇAR o Glide a ignorar cache antigo
            val signatureKey = System.currentTimeMillis().toString()

            Glide.with(this)
                .load(urlFinal)
                .apply(RequestOptions.circleCropTransform())
                .signature(ObjectKey(signatureKey)) // Força atualização visual
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Pode salvar o novo em cache
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(binding.imgPerfilAvatar)
        } else {
            binding.imgPerfilAvatar.setImageResource(R.drawable.logo)
        }

        // Controle de visualização (Meu Perfil vs Outros)
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
            binding.txtPerfilDescricao.text = perfil.descricao ?: getString(R.string.perfil_descricao_vazia)
        }
    }

    private fun buscarHistoricoDeCorridas() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getCorridasDoUsuario(userProfileId)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    setupRecyclerViewCorridas(response.body()!!)
                }
            } catch (e: Exception) {
                Log.e("PerfilActivity", "Erro histórico", e)
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

    private fun salvarDescricao() {
        val novaDescricao = binding.editPerfilDescricao.text.toString().trim()
        val request = UpdateDescricaoRequest(descricao = novaDescricao)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateDescricao(userProfileId, request)
                if (response.isSuccessful) {
                    Toast.makeText(this@PerfilActivity, getString(R.string.perfil_msg_salva), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PerfilActivity, getString(R.string.perfil_erro_salvar), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PerfilActivity, getString(R.string.erro_conexao), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarEventosSalvos() {
        if (!isMyProfile) return
        val prefs = getSharedPreferences("DadosApp", MODE_PRIVATE)
        val nomeEvento = prefs.getString("EVENTO_NOME", null)
        val detalhesEvento = prefs.getString("EVENTO_DETALHES", null)

        if (nomeEvento != null) {
            binding.cardProximoEvento.visibility = View.VISIBLE
            binding.txtEventoNomePerfil.text = nomeEvento
            binding.txtEventoDetalhesPerfil.text = detalhesEvento
        }
    }

    private fun mostrarDialogoDeConfirmacao() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.perfil_dialog_excluir_titulo))
            .setMessage(getString(R.string.perfil_dialog_excluir_msg))
            .setPositiveButton(getString(R.string.perfil_dialog_excluir_confirmar)) { _, _ -> excluirConta() }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun excluirConta() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.excluirUsuario(loggedInUserId)
                if (response.isSuccessful) {
                    Toast.makeText(this@PerfilActivity, getString(R.string.perfil_conta_excluida), Toast.LENGTH_LONG).show()
                    AppPreferences.saveFontScale(this@PerfilActivity, 1.0f)
                    val intent = Intent(this@PerfilActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this@PerfilActivity, getString(R.string.erro_generico), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PerfilActivity, getString(R.string.erro_conexao), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
