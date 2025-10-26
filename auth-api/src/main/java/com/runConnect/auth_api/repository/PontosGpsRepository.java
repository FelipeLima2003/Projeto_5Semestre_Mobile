package com.runConnect.auth_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.runConnect.auth_api.model.PontosGps;

@Repository
public interface PontosGpsRepository extends JpaRepository<PontosGps, Integer> {
    
}