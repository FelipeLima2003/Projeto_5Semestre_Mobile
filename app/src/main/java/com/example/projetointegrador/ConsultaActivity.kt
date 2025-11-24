package com.example.projetointegrador

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.databinding.ActivityConsultaBinding
import kotlinx.coroutines.launch

class ConsultaActivity : BaseActivity() {

    private lateinit var binding: ActivityConsultaBinding
    private lateinit var usuarioAdapter: UsuarioAdapter

    private val listaCompletaDeUsuarios = mutableListOf<UsuarioPublicoResponse>()

    private var loggedInUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)

        if (loggedInUserId == -1) {
            Toast.makeText(this, "Erro: ID do usuário não encontrado. Faça o login novamente.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupListeners()
        buscarUsuarios()
    }

    private fun setupListeners() {
        binding.iconVoltar.setOnClickListener {
            finish()
        }
        binding.textVoltar.setOnClickListener {
            finish()
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        binding.containerSobreNos.setOnClickListener {
            val intent = Intent(this, SobreNosActivity::class.java)
            intent.putExtra("LOGGED_IN_USER_ID", loggedInUserId)
            startActivity(intent)
        }

        binding.containerEventos.setOnClickListener {
            val intent = Intent(this, BuscaGeralActivity::class.java)
            intent.putExtra("LOGGED_IN_USER_ID", loggedInUserId)
            startActivity(intent)
        }

        binding.containerMeuPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("USER_PROFILE_ID", loggedInUserId)
            intent.putExtra("LOGGED_IN_USER_ID", loggedInUserId)
            startActivity(intent)
        }
    }


    private fun filtrarLista(textoBusca: String) {
        if (textoBusca.isEmpty()) {
            if (::usuarioAdapter.isInitialized) {
                usuarioAdapter.updateList(listaCompletaDeUsuarios)
            }
            return
        }

        val listaFiltrada = listaCompletaDeUsuarios.filter { usuario ->
            val handle = "@${usuario.nome.replace(" ", "")}"
            usuario.nome.contains(textoBusca, ignoreCase = true) || handle.contains(textoBusca, ignoreCase = true)
        }


        if (::usuarioAdapter.isInitialized) {
            usuarioAdapter.updateList(listaFiltrada)
        }
    }

    private fun buscarUsuarios() {
        lifecycleScope.launch {
            try {

                val response = RetrofitClient.apiService.getUsuarios()

                if (response.isSuccessful) {
                    val listaDaApi = response.body()
                    if (!listaDaApi.isNullOrEmpty()) {
                        listaCompletaDeUsuarios.clear()
                        listaCompletaDeUsuarios.addAll(listaDaApi.filter { it.id != loggedInUserId })
                        setupRecyclerView(listaCompletaDeUsuarios)
                    } else {
                        Toast.makeText(this@ConsultaActivity, "Nenhum usuário encontrado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ConsultaActivity", "Erro ao buscar usuários: ${response.code()}")
                    Toast.makeText(this@ConsultaActivity, "Erro ao carregar lista", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ConsultaActivity", "Falha na chamada de rede", e)
                Toast.makeText(this@ConsultaActivity, "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun abrirPerfil(usuario: UsuarioPublicoResponse) {
        Toast.makeText(this, "Abrindo perfil de ${usuario.nome}", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, PerfilActivity::class.java)
        intent.putExtra("USER_PROFILE_ID", usuario.id)
        intent.putExtra("LOGGED_IN_USER_ID", loggedInUserId)
        startActivity(intent)
    }

    private fun setupRecyclerView(usuarios: List<UsuarioPublicoResponse>) {

        if (!::usuarioAdapter.isInitialized) {
            usuarioAdapter = UsuarioAdapter(
                usuarios,
                onFollowClick = { usuarioClicado ->
                    seguirUsuario(usuarioClicado)
                },
                onProfileClick = { usuarioClicado ->
                    abrirPerfil(usuarioClicado)
                }
            )
            binding.recyclerViewUsuarios.apply {
                layoutManager = LinearLayoutManager(this@ConsultaActivity)
                adapter = usuarioAdapter
            }
        } else {
            usuarioAdapter.updateList(usuarios)
        }
    }

    private fun seguirUsuario(usuarioASeguir: UsuarioPublicoResponse) {
        if (loggedInUserId == -1) { return }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.seguirUsuario(
                    idUsuarioASerSeguido = usuarioASeguir.id,
                    idDoSeguidor = loggedInUserId
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@ConsultaActivity, "Agora você está seguindo ${usuarioASeguir.nome}", Toast.LENGTH_SHORT).show()

                    val itemParaRemover = listaCompletaDeUsuarios.find { it.id == usuarioASeguir.id }
                    if (itemParaRemover != null) {
                        listaCompletaDeUsuarios.remove(itemParaRemover)
                    }
                    filtrarLista(binding.searchEditText.text.toString())
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ConsultaActivity", "Erro ao seguir usuário (${response.code()}): $errorBody")
                    if (response.code() == 409 || (response.code() == 400 && errorBody?.contains("already follows", ignoreCase = true) == true)) {
                        Toast.makeText(this@ConsultaActivity, "Você já segue ${usuarioASeguir.nome}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ConsultaActivity, "Não foi possível seguir o usuário.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ConsultaActivity", "Falha de rede ou parsing ao seguir", e)
                Toast.makeText(this@ConsultaActivity, "Falha na conexão.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
