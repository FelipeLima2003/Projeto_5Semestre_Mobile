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
import kotlinx.coroutines.launch
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

                Glide.with(this)
                    .load(uri)
                    .into(binding.imgLogo)
            }
        }

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

        binding.btnAdicionarFoto.setOnClickListener {

            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.imgLogo.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.containerButtonConfirmar.setOnClickListener {
            realizarCadastro()
        }

        binding.editNascimento.addTextChangedListener(
            MaskUtils.apply(binding.editNascimento, "##/##/####")
        )

        binding.editCpf.addTextChangedListener(
            MaskUtils.apply(binding.editCpf, "###.###.###-##")
        )

        binding.editTelefone.addTextChangedListener(
            MaskUtils.apply(binding.editTelefone, "(##) #####-####")
        )

        val btnVerSenha = findViewById<ImageView>(R.id.btn_ver_senha)

        var eSenhaVisivel = false

        btnVerSenha.setOnClickListener {

            eSenhaVisivel = !eSenhaVisivel

            if (eSenhaVisivel) {

                binding.editSenha.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnVerSenha.alpha = 1.0f
            } else {

                binding.editSenha.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnVerSenha.alpha = 0.6f
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
        val selectedGeneroId = binding.rgGenero.checkedRadioButtonId
        val genero: Genero? = when (selectedGeneroId) {
            R.id.rb_masculino -> Genero.MASCULINO
            R.id.rb_feminino -> Genero.FEMININO
            R.id.rb_outro -> Genero.PREFIRO_NAO_INFORMAR
            else -> null
        }

        if (nome.isEmpty() || nascimentoStr.isEmpty() || cpf.isEmpty() || email.isEmpty() || telefone.isEmpty() || senha.isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.cadastro_erro_campos_vazios),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (genero == null) {
            Toast.makeText(this, getString(R.string.cadastro_erro_genero), Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (senha != confirmaSenha) {
            Toast.makeText(
                this,
                getString(R.string.cadastro_erro_senhas_diferentes),
                Toast.LENGTH_SHORT
            ).show()
            binding.editConfirmaSenha.error = getString(R.string.cadastro_erro_senhas_iguais_msg)
            return
        }

        val dataNascimentoFormatada = try {
            val partes = nascimentoStr.split("/")
            if (partes.size != 3 || partes[0].length != 2 || partes[1].length != 2 || partes[2].length != 4) {
                // CORREÇÃO AQUI: Usando string traduzível em vez de texto fixo
                throw IllegalArgumentException(getString(R.string.cadastro_erro_data_formato))
            }

            "${partes[0]}/${partes[1]}/${partes[2]}"

        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.cadastro_erro_data_formato), Toast.LENGTH_LONG)
                .show()
            binding.editNascimento.error = getString(R.string.cadastro_erro_data_msg)
            return
        }


        lifecycleScope.launch {
            var urlImagemServidor: String? = null


            if (imagemSelecionadaUri != null) {
                try {
                    Toast.makeText(this@CadastroActivity, getString(R.string.cadastro_upload_enviando), Toast.LENGTH_SHORT).show()
                    val imagemPart = prepararImagemParaUpload(imagemSelecionadaUri!!)

                    if (imagemPart != null) {
                        val responseUpload = RetrofitClient.apiService.uploadImagem(imagemPart)

                        if (responseUpload.isSuccessful) {

                            urlImagemServidor = responseUpload.body()?.toString()?.replace("\"", "")?.trim()
                            Log.d("Upload", "Imagem enviada: $urlImagemServidor")
                        } else {
                            Log.e("Upload", "Erro no upload: ${responseUpload.code()}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Upload", "Falha técnica no upload (mas seguindo cadastro)", e)

                }
            }


            val cadastroRequest = CadastroRequest(
                nome = nome,
                dataNascimento = dataNascimentoFormatada,
                cpf = cpf,
                email = email,
                telefone = telefone,
                genero = genero,
                senha = senha,
                imagemUrl = urlImagemServidor
            )

            try {
                val response = RetrofitClient.apiService.cadastrar(cadastroRequest)

                if (response.isSuccessful) {

                    Log.d("CadastroActivity", "Sucesso: Código ${response.code()}")
                    Toast.makeText(
                        this@CadastroActivity,
                        getString(R.string.cadastro_sucesso),
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this@CadastroActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CadastroActivity", "Erro API: ${response.code()} - $errorBody")
                    Toast.makeText(
                        this@CadastroActivity,
                        "${getString(R.string.cadastro_erro_prefixo)} $errorBody",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: java.io.EOFException) {

                Log.w("CadastroActivity", "EOFException detectado (Sucesso com corpo vazio)")
                Toast.makeText(
                    this@CadastroActivity,
                    getString(R.string.cadastro_sucesso),
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this@CadastroActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("CadastroActivity", "Erro Rede", e)
                Toast.makeText(
                    this@CadastroActivity,
                    "${getString(R.string.erro_conexao)}: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }


    private fun prepararImagemParaUpload(uri: Uri): MultipartBody.Part? {
        try {
            val fileDir = applicationContext.filesDir
            val file = File(fileDir, "imagem_perfil_temp.jpg")

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
}
