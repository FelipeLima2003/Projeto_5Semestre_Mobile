// package com.runConnect.auth_api.services;

// import java.time.Instant;
// import java.time.LocalDateTime;
// import java.time.ZoneOffset;

// import org.springframework.stereotype.Service;

// import com.runConnect.auth_api.model.Usuario;

// import lombok.Value;

// @Service
// public class TokenService {
    
//     @Value("$api.security.token.secret}")
//     private String secret;

//     public String gerarToken(Usuario usuario) {
       
//         try {
//             Algorithm algoritmo = Algorithm.HMAC256(secret);
//             return JWT.create()
//                 .withIssuer("runconnect-api")
//                 .withSubject(usuario.getEmail())
//                 .withExpiresAt(getExpirationDate())
//                 .sign(algoritmo);
//         } catch (Exception e) {
           
//         }
//     }

//     public String validarToken(String token){
//         try {
//             Algorithm algoritmo = Algorithm.HMAC256(secret);
//             return JWT.require(algoritmo)
//                 .withIssuer("runconnect-api")
//                 .build()
//                 .verify(token)
//                 .getSubject();
//         }catch (JWTVerificationException  exception) {
//             return "";
//         }
//     }

//     private Instant getExpirationDate() {
//         return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
//     }

// }
