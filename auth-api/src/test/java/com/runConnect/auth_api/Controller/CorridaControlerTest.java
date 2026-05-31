package com.runConnect.auth_api.controller;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.runConnect.auth_api.controller.CorridaController;
import com.runConnect.auth_api.dto.CorridaRequestDTO;
import com.runConnect.auth_api.dto.CorridaResponseDTO;
import com.runConnect.auth_api.model.Corrida;
import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.repository.CorridaRepository;
import com.runConnect.auth_api.repository.UsuarioRepository;
import com.runConnect.auth_api.util.TestDataFactory;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // ← adicionar esta linha
@DisplayName("CorridaController — Testes Unitários")
public class CorridaControlerTest {
    
    @Mock
    private CorridaRepository corridaRepository;
 
    @Mock
    private UsuarioRepository usuarioRepository;
 
    @InjectMocks
    private CorridaController corridaController;
 
    // Factory — cria uma Corrida válida para reuso nos testes
    private Corrida criarCorridaValida(Integer id, Usuario usuario) {
        Corrida c = new Corrida();
        c.setId(id);
        c.setUsuario(usuario);
        c.setDistancia(new BigDecimal("5.00"));
        c.setTempoInicial(Timestamp.valueOf("2024-01-01 08:00:00"));
        c.setTempoFinal(Timestamp.valueOf("2024-01-01 08:30:00"));
        return c;
    }
 
    private CorridaRequestDTO criarCorridaRequestDTO(Integer usuarioId) {
        return new CorridaRequestDTO(
                usuarioId,
                new BigDecimal("5.00"),
                Timestamp.from(Instant.parse("2024-01-01T08:00:00Z")),
                Timestamp.from(Instant.parse("2024-01-01T08:30:00Z"))
        );
    }
 
    // registrarCorrida — POST /corridas
 
    @Nested
    @DisplayName("registrarCorrida — POST /corridas")
        class RegistrarCorrida {
 
        @Test
        @DisplayName("Deve retornar 201 e salvar a corrida quando usuário existe")
        void registrarCorrida_UsuarioExistente_Retorna201() {
            // Arrange
            Usuario usuario = TestDataFactory.criarUsuarioValido(1, "Ana Corredora", "ana@runconnect.com");
            CorridaRequestDTO dto = criarCorridaRequestDTO(1);
            Corrida corridaSalva = criarCorridaValida(10, usuario);
 
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(corridaRepository.save(any(Corrida.class))).thenReturn(corridaSalva);
 
            // Act
            ResponseEntity<Corrida> resposta = corridaController.registrarCorrida(dto);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().getId()).isEqualTo(10);
            assertThat(resposta.getBody().getUsuario().getId()).isEqualTo(1);
            verify(corridaRepository, times(1)).save(any(Corrida.class));
        }
 
        @Test
        @DisplayName("Deve lançar RuntimeException quando usuário não existe")
        void registrarCorrida_UsuarioInexistente_LancaExcecao() {
            // Arrange
            CorridaRequestDTO dto = criarCorridaRequestDTO(999);
            when(usuarioRepository.findById(999)).thenReturn(Optional.empty());
 
            // Act + Assert
            assertThatThrownBy(() -> corridaController.registrarCorrida(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Usuário não encontrado");
 
            verify(corridaRepository, never()).save(any(Corrida.class));
        }
    }
 
    // buscarFeedCorridas — GET /corridas/feed
 
    @Nested
    @DisplayName("buscarFeedCorridas — GET /corridas/feed")
    class BuscarFeedCorridas {
 
        @Test
        @DisplayName("Deve retornar 200 e lista de corridas do feed quando usuário existe")
        void buscarFeedCorridas_UsuarioExistente_Retorna200ComLista() {
            // Arrange
            Usuario usuario = TestDataFactory.criarUsuarioValido(1, "Ana Corredora", "ana@runconnect.com");
            Corrida corrida = criarCorridaValida(10, usuario);
 
            when(usuarioRepository.existsById(1)).thenReturn(true);
            when(corridaRepository.findFeedCorridas(1)).thenReturn(List.of(corrida));
 
            // Act
            ResponseEntity<List<CorridaResponseDTO>> resposta = corridaController.buscarFeedCorridas(1);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull().hasSize(1);
            assertThat(resposta.getBody().get(0).usuarioId()).isEqualTo(1);
            verify(corridaRepository, times(1)).findFeedCorridas(1);
        }
 
        @Test
        @DisplayName("Deve retornar 200 e lista vazia quando usuário não segue ninguém")
        void buscarFeedCorridas_SemSeguidos_Retorna200ListaVazia() {
            // Arrange
            when(usuarioRepository.existsById(1)).thenReturn(true);
            when(corridaRepository.findFeedCorridas(1)).thenReturn(Collections.emptyList());
 
            // Act
            ResponseEntity<List<CorridaResponseDTO>> resposta = corridaController.buscarFeedCorridas(1);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull().isEmpty();
        }
 
        @Test
        @DisplayName("Deve retornar 404 quando usuário que pede o feed não existe")
        void buscarFeedCorridas_UsuarioInexistente_Retorna404() {
            // Arrange
            when(usuarioRepository.existsById(999)).thenReturn(false);
 
            // Act + Assert
            assertThatThrownBy(() -> corridaController.buscarFeedCorridas(999))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
 
            verify(corridaRepository, never()).findFeedCorridas(any());
        }
    }
 
    // =========================================================================
    // getCorridaPorId — GET /corridas/{id}
    // =========================================================================
 
    @Nested
    @DisplayName("getCorridaPorId — GET /corridas/{id}")
    class GetCorridaPorId {
 
        @Test
        @DisplayName("Deve retornar 200 e os dados da corrida quando o ID existe")
        void getCorridaPorId_Existente_Retorna200ComDados() {
            // Arrange
            Usuario usuario = TestDataFactory.criarUsuarioValido(1, "Ana Corredora", "ana@runconnect.com");
            Corrida corrida = criarCorridaValida(10, usuario);
 
            when(corridaRepository.findById(10)).thenReturn(Optional.of(corrida));
 
            // Act
            ResponseEntity<CorridaResponseDTO> resposta = corridaController.getCorridaPorId(10);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().id()).isEqualTo(10);
            assertThat(resposta.getBody().distancia()).isEqualByComparingTo(new BigDecimal("5.00"));
            assertThat(resposta.getBody().nomeUsuario()).isEqualTo("Ana Corredora");
            verify(corridaRepository, times(1)).findById(10);
        }
 
        @Test
        @DisplayName("Deve retornar 404 quando o ID da corrida não existe")
        void getCorridaPorId_Inexistente_Retorna404() {
            // Arrange
            when(corridaRepository.findById(999)).thenReturn(Optional.empty());
 
            // Act
            ResponseEntity<CorridaResponseDTO> resposta = corridaController.getCorridaPorId(999);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resposta.getBody()).isNull();
            verify(corridaRepository, times(1)).findById(999);
        }
    }
 
    // getCorridasPorUsuario — GET /corridas/usuario/{usuarioId}
 
    @Nested
    @DisplayName("getCorridasPorUsuario — GET /corridas/usuario/{usuarioId}")
    class GetCorridasPorUsuario {
 
        @Test
        @DisplayName("Deve retornar 200 e lista de corridas quando usuário existe")
        void getCorridasPorUsuario_UsuarioExistente_Retorna200ComLista() {
            // Arrange
            Usuario usuario = TestDataFactory.criarUsuarioValido(1, "Ana Corredora", "ana@runconnect.com");
            Corrida c1 = criarCorridaValida(10, usuario);
            Corrida c2 = criarCorridaValida(11, usuario);
 
            when(usuarioRepository.existsById(1)).thenReturn(true);
            when(corridaRepository.findByUsuarioIdOrderByTempoFinalDesc(1))
                    .thenReturn(List.of(c1, c2));
 
            // Act
            ResponseEntity<List<CorridaResponseDTO>> resposta = corridaController.getCorridasPorUsuario(1);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull().hasSize(2);
            assertThat(resposta.getBody())
                    .extracting(CorridaResponseDTO::usuarioId)
                    .containsOnly(1);
            verify(corridaRepository, times(1)).findByUsuarioIdOrderByTempoFinalDesc(1);
        }
 
        @Test
        @DisplayName("Deve retornar 200 e lista vazia quando usuário existe mas não tem corridas")
        void getCorridasPorUsuario_SemCorridas_Retorna200ListaVazia() {
            // Arrange
            when(usuarioRepository.existsById(1)).thenReturn(true);
            when(corridaRepository.findByUsuarioIdOrderByTempoFinalDesc(1))
                    .thenReturn(Collections.emptyList());
 
            // Act
            ResponseEntity<List<CorridaResponseDTO>> resposta = corridaController.getCorridasPorUsuario(1);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull().isEmpty();
        }
 
        @Test
        @DisplayName("Deve retornar 404 quando o usuário não existe")
        void getCorridasPorUsuario_UsuarioInexistente_Retorna404() {
            // Arrange
            when(usuarioRepository.existsById(999)).thenReturn(false);
 
            // Act
            ResponseEntity<List<CorridaResponseDTO>> resposta = corridaController.getCorridasPorUsuario(999);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(corridaRepository, never()).findByUsuarioIdOrderByTempoFinalDesc(any());
        }
    }
}
