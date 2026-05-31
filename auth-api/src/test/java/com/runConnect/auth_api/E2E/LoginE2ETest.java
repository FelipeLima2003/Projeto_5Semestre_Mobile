package com.runConnect.auth_api.E2E;
 
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
import org.springframework.test.web.servlet.MvcResult;
 
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes End-to-End — Login")
class LoginE2ETest {
 
    @Autowired
    private MockMvc mockMvc;
 
    @Autowired
    private UsuarioRepository usuarioRepository;
 
    @Autowired
    private PasswordEncoder passwordEncoder;
 
    private ObjectMapper objectMapper;
 
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        usuarioRepository.deleteAll();
 
        // Cria um usuário no banco para os testes de login
        Usuario usuario = new Usuario();
        usuario.setNome("Corredor E2E");
        usuario.setEmail("e2e@runconnect.com");
        usuario.setSenha(passwordEncoder.encode("Senha@123"));
        usuario.setCpf("123.456.789-00");
        usuario.setTelefone("(11) 99999-9999");
        usuario.setDataNascimento(LocalDate.of(1993, 8, 25));
        usuario.setGenero(Genero.MASCULINO);
        usuarioRepository.save(usuario);
    }
 
    @AfterEach
    void tearDown() {
        usuarioRepository.deleteAll();
    }
 
    // =========================================================================
    // E2E — Login com dados válidos
    // =========================================================================
 
    @Nested
    @DisplayName("E2E — Login com dados válidos")
    class LoginDadosValidos {
 
        @Test
        @DisplayName("Deve retornar 200 e token JWT quando credenciais são válidas")
        void login_CredenciaisValidas_Retorna200ComToken() throws Exception {
            // Arrange
            Map<String, String> credenciais = new HashMap<>();
            credenciais.put("email", "e2e@runconnect.com");
            credenciais.put("senha", "Senha@123");
            String payload = objectMapper.writeValueAsString(credenciais);
 
            // Act + Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())                          // 200
                    .andExpect(jsonPath("$.token", notNullValue()))       // token gerado
                    .andExpect(jsonPath("$.email").value("e2e@runconnect.com"))
                    .andExpect(jsonPath("$.nome").value("Corredor E2E"));
        }
 
        @Test
        @DisplayName("Deve retornar token JWT não nulo e não vazio")
        void login_CredenciaisValidas_TokenNaoVazio() throws Exception {
            // Arrange
            Map<String, String> credenciais = new HashMap<>();
            credenciais.put("email", "e2e@runconnect.com");
            credenciais.put("senha", "Senha@123");
            String payload = objectMapper.writeValueAsString(credenciais);
 
            // Act
            MvcResult result = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andReturn();
 
            // Assert — extrai o token da resposta e verifica
            String responseBody = result.getResponse().getContentAsString();
            Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
            String token = (String) responseMap.get("token");
 
            assertThat(token).isNotNull().isNotBlank();
            assertThat(token.split("\\.")).hasSize(3); // JWT tem 3 partes separadas por ponto
        }
 
        @Test
        @DisplayName("Deve retornar os dados do usuário junto com o token")
        void login_CredenciaisValidas_RetornaDadosUsuario() throws Exception {
            // Arrange
            Map<String, String> credenciais = new HashMap<>();
            credenciais.put("email", "e2e@runconnect.com");
            credenciais.put("senha", "Senha@123");
            String payload = objectMapper.writeValueAsString(credenciais);
 
            // Act + Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.nome").value("Corredor E2E"))
                    .andExpect(jsonPath("$.email").value("e2e@runconnect.com"))
                    .andExpect(jsonPath("$.token", notNullValue()));
        }
    }
 
    // =========================================================================
    // E2E — Login com senha inválida
    // =========================================================================
 
    @Nested
    @DisplayName("E2E — Login com senha inválida")
    class LoginSenhaInvalida {
 
        @Test
        @DisplayName("Deve retornar 403 quando a senha está errada")
        void login_SenhaErrada_Retorna403() throws Exception {
            // Arrange
            Map<String, String> credenciais = new HashMap<>();
            credenciais.put("email", "e2e@runconnect.com");
            credenciais.put("senha", "SenhaErrada@999");
            String payload = objectMapper.writeValueAsString(credenciais);
 
            // Act + Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isForbidden()); // 403
        }
 
        @Test
        @DisplayName("Deve retornar 403 quando o email não existe")
        void login_EmailInexistente_Retorna403() throws Exception {
            // Arrange
            Map<String, String> credenciais = new HashMap<>();
            credenciais.put("email", "inexistente@runconnect.com");
            credenciais.put("senha", "Senha@123");
            String payload = objectMapper.writeValueAsString(credenciais);
 
            // Act + Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isForbidden()); // 403
        }
 
        @Test
        @DisplayName("Não deve retornar token quando credenciais são inválidas")
        void login_CredenciaisInvalidas_SemToken() throws Exception {
            // Arrange
            Map<String, String> credenciais = new HashMap<>();
            credenciais.put("email", "e2e@runconnect.com");
            credenciais.put("senha", "SenhaErrada@999");
            String payload = objectMapper.writeValueAsString(credenciais);
 
            // Act + Assert — não deve ter campo token na resposta
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.token").doesNotExist());
        }
    }
}
 