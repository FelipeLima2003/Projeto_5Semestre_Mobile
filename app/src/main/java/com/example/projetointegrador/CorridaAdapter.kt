package com.example.projetointegrador

import CorridaResponse
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CorridaAdapter(
    private var corridas: List<CorridaResponse>
) : RecyclerView.Adapter<CorridaAdapter.CorridaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CorridaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_corrida, parent, false)
        return CorridaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CorridaViewHolder, position: Int) {
        holder.bind(corridas[position])
    }

    override fun getItemCount(): Int = corridas.size

    class CorridaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtData: TextView = itemView.findViewById(R.id.txt_item_data)
        private val txtDistancia: TextView = itemView.findViewById(R.id.txt_item_distancia)
        private val txtTempo: TextView = itemView.findViewById(R.id.txt_item_tempo)

        fun bind(corrida: CorridaResponse) {
            // 1. Distância
            txtDistancia.text = String.format("Distância: %.2f km", corrida.distancia)

            // 2. Calcular Duração e Data
            val tempoInicialStr = corrida.tempoInicial
            val tempoFinalStr = corrida.tempoFinal

            if (tempoInicialStr != null && tempoFinalStr != null) {
                val (dataInicio, dataFim) = parseDatas(tempoInicialStr.toString(), tempoFinalStr.toString())

                if (dataInicio != null && dataFim != null) {
                    // Calcula a diferença em milissegundos
                    val duracaoMs = dataFim.time - dataInicio.time

                    // Formata HH:MM:SS
                    val hours = TimeUnit.MILLISECONDS.toHours(duracaoMs)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(duracaoMs) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(duracaoMs) % 60
                    txtTempo.text = String.format("Tempo: %02d:%02d:%02d", hours, minutes, seconds)

                    // Formata a Data de Exibição (usando a data final)
                    val displayFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("pt", "BR"))
                    txtData.text = displayFormat.format(dataFim)
                } else {
                    txtTempo.text = "Tempo: --:--"
                    txtData.text = "Data desconhecida"
                }
            } else {
                txtTempo.text = "Tempo: --:--"
                txtData.text = "Data desconhecida"
            }
        }

        private fun parseDatas(inicio: String, fim: String): Pair<Date?, Date?> {
            // Tenta formatos com 'T' (ISO) e com espaço (SQL padrão)
            val formatos = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            )

            var dInicio: Date? = null
            var dFim: Date? = null

            for (fmt in formatos) {
                try {
                    if (dInicio == null) dInicio = fmt.parse(inicio)
                    if (dFim == null) dFim = fmt.parse(fim)
                } catch (e: Exception) { }
            }
            return Pair(dInicio, dFim)
        }
    }
}
