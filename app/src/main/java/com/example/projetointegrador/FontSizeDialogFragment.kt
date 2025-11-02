// Em app/src/main/java/com/example/projetointegrador/FontSizeDialogFragment.kt
package com.example.projetointegrador

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class FontSizeDialogFragment : DialogFragment() {

    interface FontSizeListener {
        fun onFontSizeSelected(scale: Float)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_font_size, null)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group)

        builder.setView(view)
            .setPositiveButton("Aplicar") { _, _ ->
                val scale = when (radioGroup.checkedRadioButtonId) {
                    R.id.font_size_small -> 0.85f
                    R.id.font_size_large -> 1.40f
                    else -> 1.0f
                }

                // 1. Notifica a Activity sobre a nova escala de fonte.
                (activity as? FontSizeListener)?.onFontSizeSelected(scale)

                // 2. Fecha o próprio diálogo.
                // A Activity será recriada DEPOIS que o diálogo já foi fechado.
                dialog?.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
        return builder.create()
    }
}
