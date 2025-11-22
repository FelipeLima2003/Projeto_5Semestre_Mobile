package com.runConnect.auth_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    SecurityFilter securityFilter;

     @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) 
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sem sessão (Stateless)
                .authorizeHttpRequests(authorize -> authorize
                       .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/cadastrar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuario").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuario/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuario/{idSeguido}/seguir").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuario/{idSeguido}/deixar-de-seguir").permitAll()
                        .anyRequest().authenticated()
                        .requestMatchers(HttpMethod.GET, "/usuario/{idSeguido}/seguidores").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuario/{idSeguido}/seguindo").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/usuario/{idSeguido}/descricao").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/usuario/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/corridas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/corridas/feed").permitAll()
                        .requestMatchers(HttpMethod.GET, "/corridas/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/corridas/usuario/{usuarioId}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/uploads/imagem").permitAll()

                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class) // Adiciona nosso filtro antes do padrão
                .build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}