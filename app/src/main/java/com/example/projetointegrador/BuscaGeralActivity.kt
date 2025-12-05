package com.example.projetointegrador

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class BuscaGeralActivity : BaseActivity(), FontSizeDialogFragment.FontSizeListener {

    private var loggedInUserId: Int = -1
    private lateinit var containerParticipantes: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busca_geral)

        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)

        if (loggedInUserId == -1) {
            Toast.makeText(this, getString(R.string.erro_login_id_ausente), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        containerParticipantes = findViewById(R.id.container_lista_participantes)

        setupListeners()
        buscarParticipantes()
    }

    private fun setupListeners() {
        val iconVoltar: ImageView = findViewById(R.id.img_back_icon)
        val textVoltar: TextView = findViewById(R.id.text_voltar)
        iconVoltar.setOnClickListener { finish() }
        textVoltar.setOnClickListener { finish() }


        val btnEvento1: Button = findViewById(R.id.btn_inscrever_evento1)
        val checkEvento1: CheckBox = findViewById(R.id.check_inscrito_evento1)

        btnEvento1.setOnClickListener {
            confirmarInscricao("Maratona do Rio", "15 de Dezembro • 12km", btnEvento1, checkEvento1)
        }


        val btnEvento2: Button = findViewById(R.id.btn_inscrever_evento2)
        val checkEvento2: CheckBox = findViewById(R.id.check_inscrito_evento2)

        btnEvento2.setOnClickListener {
            confirmarInscricao("Corrida Noturna SP", "20 de Dezembro • 5km", btnEvento2, checkEvento2)
        }


        val optionsMenuIcon: ImageView = findViewById(R.id.options_menu_icon)
        optionsMenuIcon.setOnClickListener { view ->
            showOptionsMenu(view)
        }

        val fabIniciarCorrida: FloatingActionButton = findViewById(R.id.fab_iniciar_corrida)
        fabIniciarCorrida.setOnClickListener { view ->
            pedirPermissoesDeLocalizacao()
        }

    }

    private fun confirmarInscricao(nomeEvento: String, detalhesEvento: String, botao: Button, checkBox: CheckBox) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_inscricao_titulo))
            .setMessage(getString(R.string.dialog_inscricao_msg, nomeEvento))
            .setPositiveButton(getString(R.string.dialog_inscricao_sim)) { _, _ ->

                salvarEventoLocalmente(nomeEvento, detalhesEvento)


                atualizarVisualInscrito(botao, checkBox)
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun atualizarVisualInscrito(botao: Button, checkBox: CheckBox) {
        checkBox.visibility = View.VISIBLE
        checkBox.isChecked = true


        checkBox.text = getString(R.string.status_inscrito)
        botao.text = getString(R.string.status_inscrito)

        botao.isEnabled = false
        botao.alpha = 0.6f
    }

    private fun salvarEventoLocalmente(nome: String, detalhes: String) {
        val prefs = getSharedPreferences("DadosApp", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("EVENTO_NOME", nome)
        editor.putString("EVENTO_DETALHES", detalhes)
        editor.apply()

        Toast.makeText(this, getString(R.string.msg_inscricao_sucesso), Toast.LENGTH_SHORT).show()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                Toast.makeText(this, getString(R.string.permissao_concedida_corrida), Toast.LENGTH_SHORT).show()
                val intent = Intent(this, CorridaActivity::class.java)
                intent.putExtra("LOGGED_IN_USER_ID", loggedInUserId)
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.permissao_essencial_corrida), Toast.LENGTH_LONG).show()
            }
        }

    private fun pedirPermissoesDeLocalizacao() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showOptionsMenu(anchorView: android.view.View) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.main_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_change_font_size -> {
                    FontSizeDialogFragment().show(supportFragmentManager, "FontSizeDialog")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onFontSizeSelected(scale: Float) {
        applyAndSaveFontSize(scale)
    }

    private fun buscarParticipantes() {
        lifecycleScope.launch {
            try {

                val response = RetrofitClient.apiService.getUsuarios()
                if (response.isSuccessful && response.body() != null) {
                    val todosUsuarios = response.body()!!

                    val participantes = todosUsuarios.filter { it.id != loggedInUserId }
                    
                    if (participantes.isNotEmpty()) {
                        mostrarParticipantes(participantes)
                    }
                } else {
                    Log.e("BuscaGeral", "Erro ao buscar participantes: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("BuscaGeral", "Falha na rede ao buscar participantes", e)
            }
        }
    }

    private fun mostrarParticipantes(participantes: List<UsuarioPublicoResponse>) {
        containerParticipantes.removeAllViews() // Remove os placeholders estáticos

        for (usuario in participantes) {
            val itemView = criarItemParticipante(usuario)
            containerParticipantes.addView(itemView)
        }
    }

    private fun criarItemParticipante(usuario: UsuarioPublicoResponse): View {
        val context = this
        

        val cardView = CardView(context)
        val sizePx = dpToPx(50)
        val marginPx = dpToPx(12)
        
        val layoutParams = LinearLayout.LayoutParams(sizePx, sizePx)
        layoutParams.marginEnd = marginPx
        cardView.layoutParams = layoutParams
        
        cardView.radius = sizePx / 2f // 25dp para raio
        cardView.cardElevation = 0f
        cardView.setContentPadding(0, 0, 0, 0)
        

        val imageView = ImageView(context)
        val imgParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.layoutParams = imgParams
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        
        cardView.addView(imageView)
        

        val urlString = usuario.imagemUrl?.replace("\"", "")?.trim()
        if (!urlString.isNullOrEmpty()) {
             val urlFinal = if (urlString.startsWith("http")) {
                urlString
            } else {
                "https://runconnect-api.onrender.com/uploads/$urlString"
            }
            
            val token = AppPreferences.getToken(context) ?: ""
            val glideUrl = GlideUrl(
                urlFinal, 
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            )

            Glide.with(context)
                .load(glideUrl)
                .placeholder(R.drawable.logo) 
                .error(R.drawable.logo)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.logo)
        }

        cardView.setOnClickListener {
             val intent = Intent(context, PerfilActivity::class.java)
             intent.putExtra("USER_PROFILE_ID", usuario.id)
             intent.putExtra("LOGGED_IN_USER_ID", loggedInUserId)
             startActivity(intent)
        }
        
        return cardView
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
