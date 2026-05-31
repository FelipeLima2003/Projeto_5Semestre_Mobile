package com.example.projetointegrador

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.projetointegrador.databinding.ActivityCadastroBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class CadastroActivity : BaseActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private var imagemSelecionadaUri: Uri? = null

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                imagemSelecionadaUri = uri
                Glide.with(this).load(uri).into(binding.imgLogo)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<ImageView>(R.id.icon_voltar).setOnClickListener { finish() }
        findViewById<TextView>(R.id.text_voltar).setOnClickListener { finish() }

        binding.containerButtonConfirmar.setOnClickListener { realizarCadastro() }

        binding.btnAdicionarFoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.imgLogo.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.editNascimento.addTextChangedListener(MaskUtils.apply(binding.editNascimento, "##/##/####"))
        binding.editCpf.addTextChangedListener(MaskUtils.apply(binding.editCpf, "###.###.###-##"))
        binding.editTelefone.addTextChangedListener(MaskUtils.apply(binding.editTelefone, "(##) #####-####"))

        val btnVerSenha = findViewById<ImageView>(R.id.btn_ver_senha)
        var eSenhaVisivel = false
        btnVerSenha.setOnClickListener {
            eSenhaVisivel = !eSenhaVisivel
            binding.editSenha.inputType = if (eSenhaVisivel) {
                btnVerSenha.alpha = 1.0f
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                btnVerSenha.alpha = 0.6f
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.editSenha.setSelection(binding.editSenha.text.length)
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

        Log.d("CadastroActivity", "Iniciando cadastro: email=$email")

        var temErro = false
        val camposObrigatorios = listOf(
            binding.editNome, binding.editNascimento, binding.editCpf,
            binding.editEmail, binding.editTelefone, binding.editSenha
        )

        camposObrigatorios.forEach { field ->
            if (field.text.isNullOrBlank()) {
                field.error = getString(R.string.cadastro_erro_campos_vazios)
                temErro = true
            } else {
                field.error = null
            }
        }

        if (temErro) {
            Toast.makeText(this, getString(R.string.cadastro_erro_campos_vazios), Toast.LENGTH_SHORT).show()
            return
        }

        val selectedGeneroId = binding.rgGenero.checkedRadioButtonId
        val genero = when (selectedGeneroId) {
            R.id.rb_masculino -> Genero.MASCULINO
            R.id.rb_feminino -> Genero.FEMININO
            R.id.rb_outro -> Genero.PREFIRO_NAO_INFORMAR
            else -> null
        }

        if (genero == null) {
            Toast.makeText(this, getString(R.string.cadastro_erro_genero), Toast.LENGTH_SHORT).show()
            return
        }

        if (senha != confirmaSenha) {
            binding.editConfirmaSenha.error = getString(R.string.cadastro_erro_senhas_diferentes)
            return
        }

        val dataNascimentoFormatada = try {
            val partes = nascimentoStr.split("/")
            if (partes.size != 3) throw Exception()
            "${partes[0]}/${partes[1]}/${partes[2]}"
        } catch (e: Exception) {
            binding.editNascimento.error = getString(R.string.cadastro_erro_data_msg)
            return
        }

        lifecycleScope.launch {
            var urlImagemServidor: String? = null
            imagemSelecionadaUri?.let { uri ->
                prepararImagemParaUpload(uri)?.let { part ->
                    try {
                        val res = RetrofitClient.apiService.uploadImagem(part)
                        if (res.isSuccessful) {
                            urlImagemServidor = res.body()?.toString()?.replace("\"", "")?.trim()
                        }
                    } catch (e: Exception) {
                        Log.e("Cadastro", "Erro upload imagem", e)
                    }
                }
            }

            val request = CadastroRequest(nome, dataNascimentoFormatada, cpf, email, telefone, genero, senha, urlImagemServidor)
            try {
                val response = RetrofitClient.apiService.cadastrar(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@CadastroActivity, getString(R.string.cadastro_sucesso), Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@CadastroActivity, MainActivity::class.java))
                    finish()
                } else {
                    val code = response.code()
                    val errorBody = withContext(Dispatchers.IO) {
                        response.errorBody()?.string() ?: ""
                    }
                    Log.e("CadastroActivity", "Erro API: code=$code body=$errorBody")

                    // Adicionado código 403 para tratar como duplicidade conforme solicitado
                    val isDuplicate = code == 409 || code == 403 ||
                            errorBody.contains("email", ignoreCase = true) ||
                            errorBody.contains("cadastrado", ignoreCase = true) ||
                            errorBody.contains("duplicate", ignoreCase = true)

                    if (isDuplicate) {
                        val msg = getString(R.string.cadastro_erro_email_duplicado)
                        binding.editEmail.error = msg
                        binding.editEmail.requestFocus()
                        Toast.makeText(this@CadastroActivity, msg, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@CadastroActivity, "${getString(R.string.cadastro_erro_prefixo)} $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Cadastro", "Erro Rede", e)
                Toast.makeText(this@CadastroActivity, getString(R.string.erro_conexao), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun prepararImagemParaUpload(uri: Uri): MultipartBody.Part? {
        return try {
            val file = File(filesDir, "temp_img.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", file.name, requestFile)
        } catch (e: Exception) { null }
    }
}
