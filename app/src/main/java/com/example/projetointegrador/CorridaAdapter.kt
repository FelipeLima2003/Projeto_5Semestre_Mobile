package com.example.projetointegrador

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

            // 2. Formatar Dados
            val tempoInicialStr = corrida.tempoInicial
            val tempoFinalStr = corrida.tempoFinal

            if (!tempoInicialStr.isNullOrEmpty() && !tempoFinalStr.isNullOrEmpty()) {
                val (dataInicio, dataFim) = parseDatas(tempoInicialStr, tempoFinalStr)

                if (dataInicio != null && dataFim != null) {
                    val duracaoMs = dataFim.time - dataInicio.time
                    val hours = TimeUnit.MILLISECONDS.toHours(duracaoMs)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(duracaoMs) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(duracaoMs) % 60
                    txtTempo.text = String.format("Tempo: %02d:%02d:%02d", hours, minutes, seconds)

                    val displayFormatData = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("pt", "BR"))
                    val displayFormatHora = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
                    val dataString = displayFormatData.format(dataInicio)
                    val horaString = displayFormatHora.format(dataInicio)

                    txtData.text = "$dataString às $horaString"
                } else {
                    // Fallback
                    txtTempo.text = "Tempo: --:--"
                    txtData.text = tempoInicialStr // Tenta mostrar o cru pelo menos
                }
            } else {
                txtTempo.text = "Tempo: --:--"
                txtData.text = "Data desconhecida"
            }
        }

        private fun parseDatas(inicio: String, fim: String): Pair<Date?, Date?> {
            // O Servidor retorna: "04/12/2025 19:33:19" (dd/MM/yyyy HH:mm:ss)
            val formatos = listOf(
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()), // Prioridade para formato BR
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            )

            var dInicio: Date? = null
            var dFim: Date? = null

            for (fmt in formatos) {
                try {
                    if (dInicio == null) dInicio = fmt.parse(inicio)
                } catch (e: Exception) { }

                try {
                    if (dFim == null) dFim = fmt.parse(fim)
                } catch (e: Exception) { }

                if (dInicio != null && dFim != null) break
            }
            return Pair(dInicio, dFim)
        }
    }
}
