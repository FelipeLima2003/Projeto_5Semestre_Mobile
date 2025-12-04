package com.example.projetointegrador

import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.Manifest
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BuscaGeralActivity : BaseActivity(), FontSizeDialogFragment.FontSizeListener {

    private var loggedInUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busca_geral)

        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)

        if (loggedInUserId == -1) {
            Toast.makeText(this, getString(R.string.erro_login_id_ausente), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        val iconVoltar: ImageView = findViewById(R.id.img_back_icon)
        val textVoltar: TextView = findViewById(R.id.text_voltar)
        iconVoltar.setOnClickListener { finish() }
        textVoltar.setOnClickListener { finish() }

        // Configuração Evento 1
        val btnEvento1: Button = findViewById(R.id.btn_inscrever_evento1)
        val checkEvento1: CheckBox = findViewById(R.id.check_inscrito_evento1)

        btnEvento1.setOnClickListener {
            confirmarInscricao("Maratona do Rio", "15 de Dezembro • 12km", btnEvento1, checkEvento1)
        }

        // Configuração Evento 2
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
            .setTitle(getString(R.string.dialog_inscricao_titulo)) // "Confirmar Inscrição"
            .setMessage(getString(R.string.dialog_inscricao_msg, nomeEvento)) // "Deseja participar... %s?"
            .setPositiveButton(getString(R.string.dialog_inscricao_sim)) { _, _ -> // "Sim, vamos nessa!"
                // 1. Salva os dados
                salvarEventoLocalmente(nomeEvento, detalhesEvento)

                // 2. Atualiza visualmente a tela
                atualizarVisualInscrito(botao, checkBox)
            }
            .setNegativeButton(getString(R.string.cancelar), null) // "Cancelar" (já existia)
            .show()
    }

    private fun atualizarVisualInscrito(botao: Button, checkBox: CheckBox) {
        checkBox.visibility = View.VISIBLE
        checkBox.isChecked = true

        // Traduz o texto do checkbox e do botão
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
}
