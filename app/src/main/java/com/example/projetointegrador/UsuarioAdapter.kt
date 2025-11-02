// Em app/src/main/java/com/example/projetointegrador/UsuarioAdapter.kt
package com.example.projetointegrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuarioAdapter(
    private var usuarios: List<UsuarioResponse>,
    private val onFollowClick: (UsuarioResponse) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    // ... (onCreateViewHolder, onBindViewHolder, getItemCount e UsuarioViewHolder permanecem os mesmos) ...

    // *** NOVO MÉTODO PARA ATUALIZAR A LISTA ***
    fun updateList(novaLista: List<UsuarioResponse>) {
        this.usuarios = novaLista
        notifyDataSetChanged() // Notifica o RecyclerView que a lista inteira mudou
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.bind(usuario, onFollowClick)
    }

    override fun getItemCount(): Int = usuarios.size

    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val userHandle: TextView = itemView.findViewById(R.id.userHandle)
        private val followButton: View = itemView.findViewById(R.id.containerButtonSeguir)

        fun bind(usuario: UsuarioResponse, onFollowClick: (UsuarioResponse) -> Unit) {
            userName.text = usuario.nome
            userHandle.text = "@${usuario.nome.toLowerCase().replace(" ", "")}"
            followButton.setOnClickListener { onFollowClick(usuario) }
        }
    }
}
