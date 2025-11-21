package com.runConnect.auth_api.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.runConnect.auth_api.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    
    @Value("$api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario) {
       
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                .withIssuer("runconnect-api")
                .withSubject(usuario.getEmail())
                .withExpiresAt(getExpirationDate())
                .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar o token JWT", exception);
        }
    }



    public String validarToken(String token){
        try {
             Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                .withIssuer("runconnect-api")
                .build()
                .verify(token)
                .getSubject();
        }catch (JWTVerificationException  exception) {
            return "";
        }
    }

    // O token expira em 2 horas
    private Instant getExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

}