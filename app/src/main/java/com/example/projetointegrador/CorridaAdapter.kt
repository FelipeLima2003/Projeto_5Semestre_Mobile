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

            // 2. Calcular Duração e Data
            val tempoInicialStr = corrida.tempoInicial
            val tempoFinalStr = corrida.tempoFinal

            if (!tempoInicialStr.isNullOrEmpty() && !tempoFinalStr.isNullOrEmpty()) {
                val (dataInicio, dataFim) = parseDatas(tempoInicialStr, tempoFinalStr)

                if (dataInicio != null && dataFim != null) {
                    // Calcula a diferença em milissegundos
                    val duracaoMs = dataFim.time - dataInicio.time

                    // Formata a duração (HH:MM:SS)
                    val hours = TimeUnit.MILLISECONDS.toHours(duracaoMs)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(duracaoMs) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(duracaoMs) % 60
                    txtTempo.text = String.format("Tempo: %02d:%02d:%02d", hours, minutes, seconds)

                    // Formata a Data e Hora de Exibição (usando a data inicial que é quando ocorreu)
                    // Formato desejado: "20 de Novembro, 2025 às 15:30"
                    val displayFormatData = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("pt", "BR"))
                    val displayFormatHora = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
                    
                    val dataString = displayFormatData.format(dataInicio)
                    val horaString = displayFormatHora.format(dataInicio)
                    
                    // Capitaliza a primeira letra do mês (opcional, mas fica bonito: "de novembro" -> "de Novembro")
                    // O SimpleDateFormat em pt-BR geralmente retorna minúsculo.
                    
                    txtData.text = "$dataString às $horaString"
                    
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
            // Tenta formatos comuns (ISO e SQL padrão)
            // Se a string vier com 'T', usa o formato com T. Se vier espaço, usa o com espaço.
            // Adicionado .SSS para precisão de milissegundos se houver
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
