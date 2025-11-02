package com.example.projetointegrador

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class FontSizeDialogFragment : DialogFragment() {

    private val MIN_FONT_SCALE = 0.8f
    private val MAX_FONT_SCALE = 1.8f
    private val MAX_PROGRESS = 50

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_font_size, null)

            builder.setView(view)
                .setTitle("Tamanho da Fonte")
                .setPositiveButton("Aplicar") { dialog, id ->
                    restartActivityToApplyChanges()
                }
                .setNegativeButton("Cancelar") { dialog, id ->
                    dialog.cancel()
                }

            setupSeekBar(view)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setupSeekBar(view: View) {
        val exampleTextView: TextView = view.findViewById(R.id.dialog_text_example)
        val fontSizeSeekBar: SeekBar = view.findViewById(R.id.dialog_seekbar_font_size)

        val currentScale = FontSizeManager.getFontScale(requireContext())
        val initialProgress = ((currentScale - MIN_FONT_SCALE) / (MAX_FONT_SCALE - MIN_FONT_SCALE) * MAX_PROGRESS).toInt()
        fontSizeSeekBar.progress = initialProgress

        applyScaleToTextView(exampleTextView, currentScale)

        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newScale = MIN_FONT_SCALE + (progress / MAX_PROGRESS.toFloat()) * (MAX_FONT_SCALE - MIN_FONT_SCALE)

                applyScaleToTextView(exampleTextView, newScale)
                FontSizeManager.setFontScale(requireContext(), newScale)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun applyScaleToTextView(textView: TextView, scale: Float) {
        val baseSpSize = 18f
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseSpSize * scale)
    }

    private fun restartActivityToApplyChanges() {
        val intent = requireActivity().intent
        requireActivity().finish()
        startActivity(intent)
    }

    companion object {
        const val TAG = "FontSizeDialog"
    }
}