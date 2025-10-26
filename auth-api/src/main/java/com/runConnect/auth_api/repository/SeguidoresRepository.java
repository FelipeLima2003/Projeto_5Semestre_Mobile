package com.runConnect.auth_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.runConnect.auth_api.model.Seguidores;
import com.runConnect.auth_api.model.SeguidoresId;

public interface SeguidoresRepository extends JpaRepository<Seguidores, SeguidoresId> {
    
}