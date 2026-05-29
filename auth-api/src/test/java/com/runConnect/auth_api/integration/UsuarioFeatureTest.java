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
import java.util.HashMap;
import java.util.Map;
 
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
@SpringBootTest                          
@AutoConfigureMockMvc                   
@ActiveProfiles("test")                  
@DisplayName("Testes de Integração — Usuário")
public class UsuarioFeatureTest {
    
     @Autowired
    private MockMvc mockMvc;             // ← simula requisições HTTP reais
 
    @Autowired
    private UsuarioRepository usuarioRepository;
 
    @Autowired
    private PasswordEncoder passwordEncoder;
 
    private ObjectMapper objectMapper;
 
    // -------------------------------------------------------------------------
    // Setup e limpeza — roda antes e depois de cada teste
    // -------------------------------------------------------------------------
 
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
 
        // Limpa o banco antes de cada teste para evitar conflitos
        usuarioRepository.deleteAll();
    }
 
    @AfterEach
    void tearDown() {
        // Limpa o banco após cada teste — sem dados sujos
        usuarioRepository.deleteAll();
    }
 
    // -------------------------------------------------------------------------
    // Factory — mapa de dados para cadastro válido
    // -------------------------------------------------------------------------
    private Map<String, Object> dadosCadastroValido(String email) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", "Corredor Teste");
        dados.put("email", email);
        dados.put("senha", "Senha@123");
        dados.put("cpf", "123.456.789-00");
        dados.put("telefone", "(11) 99999-9999");
        dados.put("dataNascimento", "01/01/1993");
        dados.put("genero", "MASCULINO");
        dados.put("imagemUrl", null);
        return dados;
    }
 
    // =========================================================================
    // Teste 1 — Cadastro de usuário válido
    // =========================================================================
 
    @Nested
    @DisplayName("Teste 1 — Cadastro de usuário válido")
    class CadastroUsuarioValido {
 
        @Test
        @DisplayName("Deve retornar 201 quando todos os dados são válidos")
        void cadastrar_DadosValidos_Retorna201() throws Exception {
            // Arrange
            String payload = objectMapper.writeValueAsString(dadosCadastroValido("valido@runconnect.com"));
 
            // Act + Assert
            mockMvc.perform(post("/auth/cadastrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated()); // 201
        }
 
        @Test
        @DisplayName("Deve persistir o usuário no banco após cadastro válido")
        void cadastrar_DadosValidos_PersisteBancoDeDados() throws Exception {
            // Arrange
            String payload = objectMapper.writeValueAsString(dadosCadastroValido("persistir@runconnect.com"));
 
            // Act
            mockMvc.perform(post("/auth/cadastrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated());
 
            // Assert — verifica diretamente no banco
            assert usuarioRepository.findByEmail("persistir@runconnect.com").isPresent();
        }
 
        @Test
        @DisplayName("Deve criptografar a senha antes de salvar no banco")
        void cadastrar_DadosValidos_SenhaCriptografada() throws Exception {
            // Arrange
            String payload = objectMapper.writeValueAsString(dadosCadastroValido("senha@runconnect.com"));
 
            // Act
            mockMvc.perform(post("/auth/cadastrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated());
 
            // Assert — senha no banco não pode ser igual à senha original
            Usuario usuario = usuarioRepository.findByEmail("senha@runconnect.com").orElseThrow();
            assert passwordEncoder.matches("Senha@123", usuario.getSenha());
            assert !usuario.getSenha().equals("Senha@123");
        }
    }
 
    // =========================================================================
    // Teste 2 — Cadastro com email duplicado
    // =========================================================================
 
    @Nested
    @DisplayName("Teste 2 — Cadastro com email duplicado")
    class CadastroEmailDuplicado {
 
        @Test
        @DisplayName("Deve retornar 409 quando o e-mail já está cadastrado")
        void cadastrar_EmailDuplicado_Retorna409() throws Exception {
            // Arrange — cadastra o primeiro usuário direto no banco
            Usuario usuarioExistente = new Usuario();
            usuarioExistente.setNome("Usuário Existente");
            usuarioExistente.setEmail("duplicado@runconnect.com");
            usuarioExistente.setSenha(passwordEncoder.encode("Senha@123"));
            usuarioExistente.setCpf("999.999.999-99");
            usuarioExistente.setTelefone("(11) 88888-8888");
            usuarioExistente.setDataNascimento(LocalDate.of(1990, 1, 1));
            usuarioExistente.setGenero(Genero.MASCULINO);
            usuarioRepository.save(usuarioExistente);
 
            // Tenta cadastrar com o mesmo email
            String payload = objectMapper.writeValueAsString(
                    dadosCadastroValido("duplicado@runconnect.com"));
 
            // Act + Assert
            mockMvc.perform(post("/auth/cadastrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isConflict()); // 409
        }
 
        @Test
        @DisplayName("Não deve criar um segundo usuário com o mesmo e-mail")
        void cadastrar_EmailDuplicado_NaoCriaSegundoUsuario() throws Exception {
            // Arrange — cadastra via API
            String payload = objectMapper.writeValueAsString(
                    dadosCadastroValido("unico@runconnect.com"));
 
            mockMvc.perform(post("/auth/cadastrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated());
 
            // Tenta cadastrar novamente com o mesmo email
            mockMvc.perform(post("/auth/cadastrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isConflict());
 
            // Assert — apenas 1 usuário no banco
            assert usuarioRepository.findAll().size() == 1;
        }
    }
 
    // =========================================================================
    // Teste 3 — Atualizar dados válidos
    // =========================================================================
 
    @Nested
    @DisplayName("Teste 3 — Atualizar dados válidos")
    class AtualizarDadosValidos {
 
        @Test
        @DisplayName("Deve retornar 200 e atualizar a descrição do usuário")
        void atualizarDescricao_DadosValidos_Retorna200() throws Exception {
            // Arrange — cria usuário direto no banco
            Usuario usuario = new Usuario();
            usuario.setNome("Corredor Atualizar");
            usuario.setEmail("atualizar@runconnect.com");
            usuario.setSenha(passwordEncoder.encode("Senha@123"));
            usuario.setCpf("111.111.111-11");
            usuario.setTelefone("(11) 77777-7777");
            usuario.setDataNascimento(LocalDate.of(1995, 5, 10));
            usuario.setGenero(Genero.FEMININO);
            Usuario usuarioSalvo = usuarioRepository.save(usuario);
 
            Map<String, String> body = new HashMap<>();
            body.put("descricao", "Nova descrição atualizada via teste de integração");
            String payload = objectMapper.writeValueAsString(body);
 
            // Act + Assert
            mockMvc.perform(put("/usuario/" + usuarioSalvo.getId() + "/descricao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())                                          // 200
                    .andExpect(jsonPath("$.descricao", notNullValue()))
                    .andExpect(jsonPath("$.descricao").value("Nova descrição atualizada via teste de integração"))
                    .andExpect(jsonPath("$.nome").value("Corredor Atualizar"));
        }
 
        @Test
        @DisplayName("Deve persistir a nova descrição no banco de dados")
        void atualizarDescricao_DadosValidos_PersisteBanco() throws Exception {
            // Arrange
            Usuario usuario = new Usuario();
            usuario.setNome("Corredor Persistir");
            usuario.setEmail("persistirdescricao@runconnect.com");
            usuario.setSenha(passwordEncoder.encode("Senha@123"));
            usuario.setCpf("222.222.222-22");
            usuario.setTelefone("(11) 66666-6666");
            usuario.setDataNascimento(LocalDate.of(1988, 3, 15));
            usuario.setGenero(Genero.MASCULINO);
            Usuario usuarioSalvo = usuarioRepository.save(usuario);
 
            Map<String, String> body = new HashMap<>();
            body.put("descricao", "Descrição persistida no banco");
            String payload = objectMapper.writeValueAsString(body);
 
            // Act
            mockMvc.perform(put("/usuario/" + usuarioSalvo.getId() + "/descricao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk());
 
            // Assert — verifica diretamente no banco
            Usuario usuarioAtualizado = usuarioRepository.findById(usuarioSalvo.getId()).orElseThrow();
            assert usuarioAtualizado.getDescricao().equals("Descrição persistida no banco");
        }
 
        @Test
        @DisplayName("Deve retornar 404 ao atualizar descrição de usuário inexistente")
        void atualizarDescricao_UsuarioInexistente_Retorna404() throws Exception {
            // Arrange
            Map<String, String> body = new HashMap<>();
            body.put("descricao", "Descrição qualquer");
            String payload = objectMapper.writeValueAsString(body);
 
            // Act + Assert
            mockMvc.perform(put("/usuario/99999/descricao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isNotFound()); // 404
        }
    }

}
