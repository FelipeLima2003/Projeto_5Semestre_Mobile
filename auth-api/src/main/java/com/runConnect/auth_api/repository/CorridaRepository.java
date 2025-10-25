package com.runConnect.auth_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.runConnect.auth_api.model.Corrida;

@Repository
public interface CorridaRepository extends JpaRepository<Corrida, Integer> {
      @Query("SELECT c FROM Corrida c WHERE c.usuario.id IN (SELECT s.id.seguidoId FROM Seguidores s WHERE s.id.seguidorId = :meuId) ORDER BY c.tempoFinal DESC")
    List<Corrida> findFeedCorridas(Integer meuId);
}