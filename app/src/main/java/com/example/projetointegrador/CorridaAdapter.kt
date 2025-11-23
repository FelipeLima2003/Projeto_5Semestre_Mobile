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

            txtDistancia.text = String.format("Distância: %.2f km", corrida.distancia)

            val tempoMs = corrida.tempoFinal
            val hours = TimeUnit.MILLISECONDS.toHours(tempoMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(tempoMs) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(tempoMs) % 60
            txtTempo.text = String.format("Tempo: %02d:%02d:%02d", hours, minutes, seconds)

            txtData.text = formatarData(corrida.dataCorrida)
        }

        private fun formatarData(dataString: String?): String {

            if (dataString.isNullOrEmpty()) {
                return "Data desconhecida"
            }

            val formatosPossiveis = listOf(

                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            )

            var data: Date? = null
            for (formato in formatosPossiveis) {
                try {
                    data = formato.parse(dataString)
                    if (data != null) break
                } catch (e: Exception) {

                }
            }

            return if (data != null) {

                val formatter = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("pt", "BR"))
                formatter.format(data)
            } else {

                try {
                    dataString.split("T")[0]
                } catch (e: Exception) {
                    dataString
                }
            }
        }
    }
}
