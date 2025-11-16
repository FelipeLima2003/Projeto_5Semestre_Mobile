package com.example.projetointegrador

import android.app.Dialog
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class FontSizeDialogFragment : DialogFragment() {

    interface FontSizeListener {
        fun onFontSizeSelected(scale: Float)
    }

    private var currentScale: Float = 1.0f

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_font_size, null)

        val seekBar = view.findViewById<SeekBar>(R.id.font_size_seekbar)
        val valueText = view.findViewById<TextView>(R.id.font_size_value_text)

        // Carrega a escala de fonte atual para definir a posição inicial da seekbar
        val initialScale = AppPreferences.getFontScale(requireContext())
        currentScale = initialScale
        // Converte a escala inicial (ex: 1.15f) para um progresso na seekbar (ex: 75)
        seekBar.progress = scaleToProgress(initialScale)
        valueText.text = "${(initialScale * 100).toInt()}%"

        // Listener para atualizar o texto do valor e a escala enquanto o usuário arrasta
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Converte o progresso (0-100) para a escala de fonte (ex: 0.85f a 1.40f)
                currentScale = progressToScale(progress)
                valueText.text = "${(currentScale * 100).toInt()}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        builder.setView(view)
            .setPositiveButton("Aplicar") { _, _ ->
                // Notifica a Activity com a escala final escolhida
                (activity as? FontSizeListener)?.onFontSizeSelected(currentScale)
                dialog?.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
        return builder.create()
    }

    /**
     * Converte o progresso da SeekBar (0-100) para uma escala de fonte.
     * Ex: 0 -> 0.85f (Pequeno), 50 -> 1.0f (Médio), 100 -> 1.40f (Grande)
     */
    private fun progressToScale(progress: Int): Float {
        // Mapeia o intervalo [0, 100] para [0.85, 1.40]
        val minScale = 0.85f
        val maxScale = 1.40f
        return minScale + (maxScale - minScale) * (progress / 100.0f)
    }

    /**
     * Converte uma escala de fonte de volta para um progresso da SeekBar.
     * Usado para definir a posição inicial da barra.
     */
    private fun scaleToProgress(scale: Float): Int {
        val minScale = 0.85f
        val maxScale = 1.40f
        if (scale <= minScale) return 0
        if (scale >= maxScale) return 100
        return ((scale - minScale) / (maxScale - minScale) * 100).toInt()
    }
}
