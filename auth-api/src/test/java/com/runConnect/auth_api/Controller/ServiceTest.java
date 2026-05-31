package com.runConnect.auth_api.controller;

import com.runConnect.auth_api.model.Usuario;

import com.runConnect.auth_api.repository.UsuarioRepository;
import com.runConnect.auth_api.util.TestDataFactory;
import com.runConnect.auth_api.services.AuthorizationService;
import com.runConnect.auth_api.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Services — Testes Unitários")
class ServicesTest {

    // =========================================================================
    // TokenService
    // =========================================================================

    @Nested
    @DisplayName("TokenService — gerarToken e validarToken")
    class TokenServiceTest {

        @InjectMocks
        private TokenService tokenService;

        private Usuario usuario;

        @BeforeEach
        void setUp() {
    
            ReflectionTestUtils.setField(tokenService, "secret", "chave-secreta-de-teste-para-junit-123456");
            usuario = TestDataFactory.criarUsuarioValido(1, "Ana Corredora", "ana@runconnect.com");
        }

        @Test
        @DisplayName("Deve gerar um token JWT não nulo e não vazio")
        void gerarToken_UsuarioValido_RetornaTokenNaoVazio() {
            String token = tokenService.gerarToken(usuario);

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Deve gerar um token JWT com 3 partes separadas por ponto")
        void gerarToken_UsuarioValido_TokenComTresPartes() {
            String token = tokenService.gerarToken(usuario);

            // JWT sempre tem formato: header.payload.signature
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Deve gerar tokens diferentes para usuários diferentes")
        void gerarToken_UsuariosDiferentes_TokensDiferentes() {
            Usuario u2 = TestDataFactory.criarUsuarioValido(2, "Carlos", "carlos@runconnect.com");

            String token1 = tokenService.gerarToken(usuario);
            String token2 = tokenService.gerarToken(u2);

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Deve validar token e retornar o email do usuário")
        void validarToken_TokenValido_RetornaEmail() {
            String token = tokenService.gerarToken(usuario);

            String email = tokenService.validarToken(token);

            assertThat(email).isEqualTo("ana@runconnect.com");
        }

        @Test
        @DisplayName("Deve retornar string vazia para token inválido")
        void validarToken_TokenInvalido_RetornaStringVazia() {
            String email = tokenService.validarToken("token.invalido.qualquer");

            assertThat(email).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar string vazia para token nulo")
        void validarToken_TokenNulo_RetornaStringVazia() {
            String email = tokenService.validarToken(null);

            assertThat(email).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar string vazia para token expirado ou adulterado")
        void validarToken_TokenAdulterado_RetornaStringVazia() {
            String token = tokenService.gerarToken(usuario);

            // Adultera o token trocando caracteres da assinatura
            String tokenAdulterado = token.substring(0, token.length() - 5) + "XXXXX";

            String email = tokenService.validarToken(tokenAdulterado);

            assertThat(email).isEmpty();
        }
    }

    // =========================================================================
    // AuthorizationService
    // =========================================================================

    @Nested
    @DisplayName("AuthorizationService — loadUserByUsername")
    class AuthorizationServiceTest {

        @Mock
        private UsuarioRepository usuarioRepository;

        @InjectMocks
        private AuthorizationService authorizationService;

        @Test
        @DisplayName("Deve retornar o usuário quando o email existe")
        void loadUserByUsername_EmailExistente_RetornaUsuario() {
            Usuario usuario = TestDataFactory.criarUsuarioValido(1, "Ana Corredora", "ana@runconnect.com");
            when(usuarioRepository.findByEmail("ana@runconnect.com")).thenReturn(Optional.of(usuario));

            var resultado = authorizationService.loadUserByUsername("ana@runconnect.com");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getUsername()).isEqualTo("ana@runconnect.com");
            verify(usuarioRepository, times(1)).findByEmail("ana@runconnect.com");
        }

        @Test
        @DisplayName("Deve lançar UsernameNotFoundException quando email não existe")
        void loadUserByUsername_EmailInexistente_LancaExcecao() {
            when(usuarioRepository.findByEmail("inexistente@runconnect.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    authorizationService.loadUserByUsername("inexistente@runconnect.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("Usuário não encontrado");

            verify(usuarioRepository, times(1)).findByEmail("inexistente@runconnect.com");
        }
    }
}