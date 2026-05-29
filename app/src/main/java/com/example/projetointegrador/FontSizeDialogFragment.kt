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
        val initialScale = AppPreferences.getFontScale(requireContext())
        currentScale = initialScale

        seekBar.progress = scaleToProgress(initialScale)
        valueText.text = "${(initialScale * 100).toInt()}%"


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                currentScale = progressToScale(progress)
                valueText.text = "${(currentScale * 100).toInt()}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        builder.setView(view)
            .setPositiveButton("Aplicar") { _, _ ->

                (activity as? FontSizeListener)?.onFontSizeSelected(currentScale)
                dialog?.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
        return builder.create()
    }

    private fun progressToScale(progress: Int): Float {

        val minScale = 0.85f
        val maxScale = 1.40f
        return minScale + (maxScale - minScale) * (progress / 100.0f)
    }

    private fun scaleToProgress(scale: Float): Int {
        val minScale = 0.85f
        val maxScale = 1.40f
        if (scale <= minScale) return 0
        if (scale >= maxScale) return 100
        return ((scale - minScale) / (maxScale - minScale) * 100).toInt()
    }
}
