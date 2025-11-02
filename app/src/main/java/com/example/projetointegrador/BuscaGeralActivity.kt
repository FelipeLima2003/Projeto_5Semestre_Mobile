package com.example.projetointegrador

import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu // <<< IMPORTAR
import android.widget.TextView
import android.widget.Toast

//           HERDA DE BaseActivity            IMPLEMENTA A INTERFACE DO DIÁLOGO
//               vvvvvvvvvvvv                    vvvvvvvvvvvvvvvvvv
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
        // Botão de voltar
        val iconVoltar: ImageView = findViewById(R.id.img_back_icon)
        val textVoltar: TextView = findViewById(R.id.text_voltar)
        iconVoltar.setOnClickListener { finish() }
        textVoltar.setOnClickListener { finish() }

        // Botão do menu de opções (três pontos)
        val optionsMenuIcon: ImageView = findViewById(R.id.options_menu_icon)
        optionsMenuIcon.setOnClickListener { view ->
            showOptionsMenu(view)
        }
    }

    private fun showOptionsMenu(anchorView: android.view.View) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.main_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_change_font_size -> {
                    // Abre o diálogo para mudar a fonte
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
