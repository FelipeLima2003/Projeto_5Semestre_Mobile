package com.example.projetointegrador

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

abstract class MaskUtils(private val mask: String) : TextWatcher {
    private var isUpdating: Boolean = false
    private var old = ""

    override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val str = unmask(s.toString())
        var mascara = ""
        if (isUpdating) {
            old = str
            isUpdating = false
            return
        }
        var i = 0
        for (m in mask.toCharArray()) {
            if (m != '#' && str.length > old.length) {
                mascara += m
                continue
            }
            try {
                mascara += str[i]
            } catch (e: Exception) {
                break
            }
            i++
        }
        isUpdating = true
        onTextChanged(mascara)
    }

    override fun afterTextChanged(editable: Editable) {}

    abstract fun onTextChanged(text: String)

    companion object {
        fun unmask(s: String): String {
            return s.replace("[.]".toRegex(), "").replace("[-]".toRegex(), "")
                .replace("[/]".toRegex(), "").replace("[(]".toRegex(), "")
                .replace("[)]".toRegex(), "").replace(" ".toRegex(), "")
        }

        fun apply(editText: EditText, mask: String): TextWatcher {
            return object : MaskUtils(mask) {
                override fun onTextChanged(text: String) {
                    editText.setText(text)
                    editText.setSelection(text.length)
                }
            }
        }
    }
}