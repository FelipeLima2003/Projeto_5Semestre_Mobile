package com.example.projetointegrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuarioAdapter(
    private var usuarios: List<UsuarioResponse>,
    private val onFollowClick: (UsuarioResponse) -> Unit,
    private val onProfileClick: (UsuarioResponse) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.bind(usuario, onFollowClick, onProfileClick)
    }

    override fun getItemCount(): Int = usuarios.size

    fun updateList(novaLista: List<UsuarioResponse>) {
        this.usuarios = novaLista
        notifyDataSetChanged()
    }

    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val userHandle: TextView = itemView.findViewById(R.id.userHandle)
        private val followButton: View = itemView.findViewById(R.id.containerButtonSeguir)
        private val profileButton: View = itemView.findViewById(R.id.containerButtonPerfil)
        fun bind(
            usuario: UsuarioResponse,
            onFollowClick: (UsuarioResponse) -> Unit,
            onProfileClick: (UsuarioResponse) -> Unit
        ) {
            userName.text = usuario.nome
            userHandle.text = "@${usuario.nome.toLowerCase().replace(" ", "")}"

            followButton.setOnClickListener { onFollowClick(usuario) }
            profileButton.setOnClickListener { onProfileClick(usuario) }
        }
    }
}
