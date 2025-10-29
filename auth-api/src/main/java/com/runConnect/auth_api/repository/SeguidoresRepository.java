package com.runConnect.auth_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.runConnect.auth_api.model.Seguidores;
import com.runConnect.auth_api.model.SeguidoresId;
import com.runConnect.auth_api.model.Usuario;

public interface SeguidoresRepository extends JpaRepository<Seguidores, SeguidoresId> {
    
    // Método para encontrar uma relação específica (útil para verificar se já segue)
    Optional<Seguidores> findById_SeguidorIdAndId_SeguidoId(Integer seguidorId, Integer seguidoId);

    // Lista de IDs dos usuarios que uma pessoa SEGUE
    @Query("SELECT s.seguido FROM Seguidores s WHERE s.seguidor.id = :usuarioId")
    List<Usuario> findSeguindoByUsuarioId(Integer usuarioId);

    // Lista de IDs dos usuarios que SEGUEM uma pessoa
    @Query("SELECT s.seguidor FROM Seguidores s WHERE s.seguido.id = :usuarioId")
    List<Usuario> findSeguidoresByUsuarioId(Integer usuarioId);

    // Contagem de quantos um usuarios segue
    long countById_SeguidorId(Integer seguidorId);

    // Contagem de quantos seguem um usuarios
    long countById_SeguidoId(Integer seguidoId);
}