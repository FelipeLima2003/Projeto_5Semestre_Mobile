package com.example.projetointegrador

import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.Manifest
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BuscaGeralActivity : BaseActivity(), FontSizeDialogFragment.FontSizeListener {

    private var loggedInUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busca_geral)

        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)

        if (loggedInUserId == -1) {
            Toast.makeText(this, "Erro: Usuário não identificado.", Toast.LENGTH_SHORT).show()
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


        val optionsMenuIcon: ImageView = findViewById(R.id.options_menu_icon)
        optionsMenuIcon.setOnClickListener { view ->
            showOptionsMenu(view)
        }

        val fabIniciarCorrida: FloatingActionButton = findViewById(R.id.fab_iniciar_corrida)
        fabIniciarCorrida.setOnClickListener { view ->
            pedirPermissoesDeLocalizacao()
        }

    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                Toast.makeText(this, "Permissão concedida. Iniciando corrida...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, CorridaActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "A permissão de localização é essencial para rastrear sua corrida.", Toast.LENGTH_LONG).show()
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
