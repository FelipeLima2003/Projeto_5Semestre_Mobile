package com.runConnect.auth_api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.model.enums.Genero;
import com.runConnect.auth_api.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Teste de Integração — Desempenho do Banco de Dados")
class DesempenhoBancoDadosTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper;

    // Limites de tempo aceitáveis (em milissegundos)
    private static final long TEMPO_MAXIMO_INSERCAO_MS = 500;
    private static final long TEMPO_MAXIMO_BUSCA_MS = 300;
    private static final long TEMPO_MAXIMO_BUSCA_LISTA_MS = 500;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        usuarioRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        usuarioRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // Factory — cria usuário diretamente no banco
    // -------------------------------------------------------------------------
    private Usuario criarUsuarioBanco(String email, String cpf) {
        Usuario u = new Usuario();
        u.setNome("Corredor Desempenho");
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode("Senha@123"));
        u.setCpf(cpf);
        u.setTelefone("(11) 99999-9999");
        u.setDataNascimento(LocalDate.of(1993, 1, 1));
        u.setGenero(Genero.MASCULINO);
        return usuarioRepository.save(u);
    }

    // =========================================================================
    // Desempenho — Inserção
    // =========================================================================

    @Nested
    @DisplayName("Desempenho — Inserção no banco")
    class DesempenhoInsercao {

        @Test
        @DisplayName("Deve inserir um usuário em menos de 500ms")
        void inserirUsuario_TempoAceitavel() throws Exception {
            Map<String, Object> dados = new HashMap<>();
            dados.put("nome", "Corredor Velocidade");
            dados.put("email", "velocidade@runconnect.com");
            dados.put("senha", "Senha@123");
            dados.put("cpf", "111.111.111-11");
            dados.put("telefone", "(11) 99999-9999");
            dados.put("dataNascimento", "01/01/1993");
            dados.put("genero", "MASCULINO");
            dados.put("imagemUrl", null);

            String payload = objectMapper.writeValueAsString(dados);

            // Act — mede o tempo da requisição completa
            long inicio = System.currentTimeMillis();

            mockMvc.perform(post("/auth/cadastrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated());

            long tempo = System.currentTimeMillis() - inicio;

            // Assert
            System.out.println("⏱ Tempo de inserção: " + tempo + "ms");
            assertThat(tempo)
                    .as("Inserção deve completar em menos de %dms, mas levou %dms",
                            TEMPO_MAXIMO_INSERCAO_MS, tempo)
                    .isLessThanOrEqualTo(TEMPO_MAXIMO_INSERCAO_MS);
        }

        @Test
        @DisplayName("Deve inserir 10 usuários em menos de 2000ms")
        void inserirDezUsuarios_TempoAceitavel() {
            // Act — insere 10 usuários direto no repositório
            long inicio = System.currentTimeMillis();

            for (int i = 1; i <= 10; i++) {
                criarUsuarioBanco(
                        "usuario" + i + "@runconnect.com",
                        String.format("%03d.%03d.%03d-%02d", i, i, i, i)
                );
            }

            long tempo = System.currentTimeMillis() - inicio;

            // Assert
            System.out.println("⏱ Tempo para inserir 10 usuários: " + tempo + "ms");
            assertThat(usuarioRepository.count()).isEqualTo(10);
            assertThat(tempo)
                    .as("Inserção de 10 usuários deve completar em menos de 2000ms, mas levou %dms", tempo)
                    .isLessThanOrEqualTo(2000);
        }
    }

    // =========================================================================
    // Desempenho — Busca por ID
    // =========================================================================

    @Nested
    @DisplayName("Desempenho — Busca por ID")
    class DesempenhoBuscaPorId {

        @Test
        @DisplayName("Deve buscar um usuário por ID em menos de 300ms")
        void buscarUsuarioPorId_TempoAceitavel() throws Exception {
            // Arrange
            Usuario usuario = criarUsuarioBanco("buscar@runconnect.com", "222.222.222-22");

            // Act
            long inicio = System.currentTimeMillis();

            mockMvc.perform(get("/usuario/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            long tempo = System.currentTimeMillis() - inicio;

            // Assert
            System.out.println("⏱ Tempo de busca por ID: " + tempo + "ms");
            assertThat(tempo)
                    .as("Busca por ID deve completar em menos de %dms, mas levou %dms",
                            TEMPO_MAXIMO_BUSCA_MS, tempo)
                    .isLessThanOrEqualTo(TEMPO_MAXIMO_BUSCA_MS);
        }
    }

    // =========================================================================
    // Desempenho — Busca de lista
    // =========================================================================

    @Nested
    @DisplayName("Desempenho — Busca de lista de usuários")
    class DesempenhoBuscaLista {

        @Test
        @DisplayName("Deve listar 50 usuários em menos de 500ms")
        void listarCinquentaUsuarios_TempoAceitavel() throws Exception {
            // Arrange — insere 50 usuários no banco
            for (int i = 1; i <= 50; i++) {
                criarUsuarioBanco(
                        "lista" + i + "@runconnect.com",
                        String.format("%03d.%03d.%03d-%02d", i, i * 2, i * 3, i % 100)
                );
            }

            assertThat(usuarioRepository.count()).isEqualTo(50);

            // Act
            long inicio = System.currentTimeMillis();

            mockMvc.perform(get("/usuario")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            long tempo = System.currentTimeMillis() - inicio;

            // Assert
            System.out.println("⏱ Tempo para listar 50 usuários: " + tempo + "ms");
            assertThat(tempo)
                    .as("Listagem de 50 usuários deve completar em menos de %dms, mas levou %dms",
                            TEMPO_MAXIMO_BUSCA_LISTA_MS, tempo)
                    .isLessThanOrEqualTo(TEMPO_MAXIMO_BUSCA_LISTA_MS);
        }
    }
}