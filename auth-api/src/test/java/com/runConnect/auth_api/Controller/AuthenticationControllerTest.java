package com.runConnect.auth_api.controller;

import com.runConnect.auth_api.dto.CadastroRequestDTO;
import com.runConnect.auth_api.dto.LoginRequestDTO;
import com.runConnect.auth_api.dto.LoginResponseDTO;
import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.model.enums.Genero;
import com.runConnect.auth_api.repository.UsuarioRepository;
import com.runConnect.auth_api.services.TokenService;
import com.runConnect.auth_api.util.TestDataFactory;
import com.runConnect.auth_api.controller.AuthenticationController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // ← único, na classe principal
@DisplayName("AuthenticationController — Testes Unitários")
class AuthenticationControllerTest {

    // Mocks declarados uma única vez — herdados por todos os @Nested
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationController authenticationController;

    // -------------------------------------------------------------------------
    // Factory — DTO com senha customizável, demais campos válidos e completos
    // -------------------------------------------------------------------------
    private CadastroRequestDTO dtoComSenha(String senha) {
        return new CadastroRequestDTO(
                "Corredor Teste",
                "teste@runconnect.com",
                senha,
                "123.456.789-00",
                "(11) 99999-9999",
                LocalDate.of(1993, 8, 25),
                null, // imagemUrl — opcional, intencionalmente ausente
                Genero.MASCULINO);
    }

    // =========================================================================
    // Cenário 4 — Senhas inválidas
    // =========================================================================

    @Nested
    @DisplayName("Cenário 4 — Senhas inválidas (devem ser rejeitadas)")
    class SenhasInvalidas {

        @ParameterizedTest(name = "Senha muito curta: \"{0}\"")
        @DisplayName("Deve rejeitar senhas com menos de 8 caracteres")
        @ValueSource(strings = {
                "", // vazia
                "Ab1!", // 4 chars — tem número e especial, mas curta demais
                "Ab1!xyz" // 7 chars — quase lá, mas ainda inválida
        })
        void cadastrar_SenhaCurta_DeveRejeitar(String senha) {
            when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationController.cadastrar(dtoComSenha(senha)))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                        assertThat(rse.getReason()).containsIgnoringCase("senha");
                    });

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @ParameterizedTest(name = "Sem número: \"{0}\"")
        @DisplayName("Deve rejeitar senhas sem nenhum número")
        @ValueSource(strings = {
                "SemNumero!", // letras + especial, sem número
                "AbcdEfgh@", // 9 chars, sem número
                "Corredor!!!" // longa, mas sem número
        })
        void cadastrar_SenhaSemNumero_DeveRejeitar(String senha) {
            when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationController.cadastrar(dtoComSenha(senha)))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                        assertThat(rse.getReason()).containsIgnoringCase("senha");
                    });

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @ParameterizedTest(name = "Sem caractere especial: \"{0}\"")
        @DisplayName("Deve rejeitar senhas sem nenhum caractere especial")
        @ValueSource(strings = {
                "Senha1234", // letras + números, sem especial
                "Corredor99", // 10 chars, sem especial
                "AbCdEf12" // 8 chars exatos, sem especial
        })
        void cadastrar_SenhaSemCaractereEspecial_DeveRejeitar(String senha) {
            when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationController.cadastrar(dtoComSenha(senha)))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                        assertThat(rse.getReason()).containsIgnoringCase("senha");
                    });

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @ParameterizedTest(name = "Apenas um tipo de char: \"{0}\"")
        @DisplayName("Deve rejeitar senhas com apenas um tipo de caractere")
        @ValueSource(strings = {
                "abcdefgh", // só letras minúsculas
                "ABCDEFGH", // só letras maiúsculas
                "12345678", // só números
                "!@#$%^&*" // só especiais
        })
        void cadastrar_SenhaMonotona_DeveRejeitar(String senha) {
            when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationController.cadastrar(dtoComSenha(senha)))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                        assertThat(rse.getReason()).containsIgnoringCase("senha");
                    });

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    @Nested
    @DisplayName("Cenário 4 — Senhas válidas (devem ser aceitas)")
    class SenhasValidas {

        @ParameterizedTest(name = "Senha válida: \"{0}\"")
        @DisplayName("Deve aceitar senhas com 8+ chars, número e caractere especial")
        @ValueSource(strings = {
                "Senha@123", // caso típico
                "Run#2024!", // contexto do app
                "C0rr3d0r!", // substituição de letras por números
                "abc123!@#xyz", // longa com vários especiais
                "Minha$Senha9" // 12 chars, todos os requisitos
        })
        void cadastrar_SenhaForte_DeveAceitar(String senha) {
            when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(senha)).thenReturn("hash_seguro");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            ResponseEntity<Void> resposta = authenticationController.cadastrar(dtoComSenha(senha));

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
            verify(passwordEncoder, times(1)).encode(senha);
        }
    }

    // Cenário 5 — Campos obrigatórios nulos

    @Nested
    @DisplayName("Cenário 5 — Campos obrigatórios nulos (devem ser rejeitados)")
    class ValidarCamposObrigatorios {

        @Test
        @DisplayName("Não deve salvar quando nome é nulo")
        void cadastrar_NomeNulo_NaoDevePersistir() {
            CadastroRequestDTO dto = new CadastroRequestDTO(
                    null, // ← campo ausente
                    "semdado@runconnect.com",
                    "Senha@789",
                    "100.100.100-00",
                    "(11) 77777-7777",
                    LocalDate.of(1995, 1, 1),
                    null,
                    Genero.MASCULINO);
            assertCampoNuloNaoSalva(dto);
        }

        @Test
        @DisplayName("Não deve salvar quando e-mail é nulo")
        void cadastrar_EmailNulo_NaoDevePersistir() {
            CadastroRequestDTO dto = new CadastroRequestDTO(
                    "Sem Email",
                    null, // ← campo ausente
                    "Senha@789",
                    "200.200.200-00",
                    "(11) 66666-6666",
                    LocalDate.of(1995, 1, 1),
                    null,
                    Genero.MASCULINO);
            assertCampoNuloNaoSalva(dto);
        }

        @Test
        @DisplayName("Não deve salvar quando senha é nula")
        void cadastrar_SenhaNula_NaoDevePersistir() {
            CadastroRequestDTO dto = new CadastroRequestDTO(
                    "Sem Senha",
                    "semsenha@runconnect.com",
                    null, // ← campo ausente
                    "300.300.300-00",
                    "(11) 55555-5555",
                    LocalDate.of(1995, 1, 1),
                    null,
                    Genero.MASCULINO);
            when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            assertCampoNuloNaoSalva(dto);
        }

        @Test
        @DisplayName("Não deve salvar quando CPF é nulo")
        void cadastrar_CpfNulo_NaoDevePersistir() {
            CadastroRequestDTO dto = new CadastroRequestDTO(
                    "Sem CPF",
                    "semcpf@runconnect.com",
                    "Senha@789",
                    null, // ← campo ausente
                    "(11) 44444-4444",
                    LocalDate.of(1995, 1, 1),
                    null,
                    Genero.MASCULINO);
            when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            // when(passwordEncoder.encode(any())).thenReturn("hash");
            assertCampoNuloNaoSalva(dto);
        }

        @Test
        @DisplayName("Não deve salvar quando telefone é nulo")
        void cadastrar_TelefoneNulo_NaoDevePersistir() {
            CadastroRequestDTO dto = new CadastroRequestDTO(
                    "Sem Telefone",
                    "semtelefone@runconnect.com",
                    "Senha@789",
                    "400.400.400-00",
                    null, // ← campo ausente
                    LocalDate.of(1995, 1, 1),
                    null,
                    Genero.MASCULINO);
            when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            // when(passwordEncoder.encode(any())).thenReturn("hash");
            assertCampoNuloNaoSalva(dto);
        }

        @Test
        @DisplayName("Não deve salvar quando data de nascimento é nula")
        void cadastrar_DataNascimentoNula_NaoDevePersistir() {
            CadastroRequestDTO dto = new CadastroRequestDTO(
                    "Sem Data",
                    "semdata@runconnect.com",
                    "Senha@789",
                    "500.500.500-00",
                    "(11) 33333-3333",
                    null, // ← campo ausente
                    null,
                    Genero.MASCULINO);
            when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            
            assertCampoNuloNaoSalva(dto);
        }

        @Test
        @DisplayName("Não deve salvar quando gênero é nulo")
        void cadastrar_GeneroNulo_NaoDevePersistir() {
            CadastroRequestDTO dto = new CadastroRequestDTO(
                    "Sem Genero",
                    "semgenero@runconnect.com",
                    "Senha@789",
                    "600.600.600-00",
                    "(11) 22222-2222",
                    LocalDate.of(1995, 1, 1),
                    null,
                    null // ← campo ausente
            );
            when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            // when(passwordEncoder.encode(any())).thenReturn("hash");
            assertCampoNuloNaoSalva(dto);
        }
    }

    // Cadastro bem-sucedido
    //

    @Nested
    @DisplayName("Caminho feliz — cadastro bem-sucedido")
    class CadastroBemSucedido {

        @Test
        @DisplayName("Deve salvar com sucesso sem imagem (campo opcional ausente)")
        void cadastrar_DadosCompletos_SemImagem_DevePersistir() {
            CadastroRequestDTO dto = new CadastroRequestDTO(
                    "Corredor Completo",
                    "completo@runconnect.com",
                    "Senha@2024",
                    "555.555.555-55",
                    "(11) 33333-3333",
                    LocalDate.of(1993, 8, 25),
                    null, // imagemUrl — opcional
                    Genero.MASCULINO);

            when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(dto.senha())).thenReturn("hash_seguro");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            ResponseEntity<Void> resposta = authenticationController.cadastrar(dto);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
            verify(passwordEncoder, times(1)).encode(dto.senha());
        }

        @Test
        @DisplayName("Deve salvar com sucesso com imagem (campo opcional preenchido)")
        void cadastrar_DadosCompletos_ComImagem_DevePersistir() {
            CadastroRequestDTO dto = new CadastroRequestDTO(
                    "Corredor Com Foto",
                    "comfoto@runconnect.com",
                    "Senha@2024",
                    "666.666.666-66",
                    "(11) 88888-8888",
                    LocalDate.of(1990, 3, 10),
                    "http://img.com/foto.png", // imagemUrl presente
                    Genero.FEMININO);

            when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(dto.senha())).thenReturn("hash_seguro");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            ResponseEntity<Void> resposta = authenticationController.cadastrar(dto);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar CONFLICT quando e-mail já cadastrado")
        void cadastrar_EmailJaCadastrado_LancaConflict() {
            CadastroRequestDTO dto = new CadastroRequestDTO(
                    "Maria Existente",
                    "maria@runconnect.com",
                    "Senha@2024",
                    "111.111.111-11",
                    "(11) 77777-7777",
                    LocalDate.of(1988, 3, 15),
                    null,
                    Genero.FEMININO);

            when(usuarioRepository.findByEmail(dto.email()))
                    .thenReturn(Optional.of(new Usuario()));

            assertThatThrownBy(() -> authenticationController.cadastrar(dto))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(rse.getReason()).containsIgnoringCase("e-mail");
                    });

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    // Helper privado — centraliza o assert de campo nulo

    private void assertCampoNuloNaoSalva(CadastroRequestDTO dto) {
        try {
            authenticationController.cadastrar(dto);
            verify(usuarioRepository, never()).save(argThat(u -> u.getNome() == null ||
                    u.getEmail() == null ||
                    u.getSenha() == null ||
                    u.getCpf() == null ||
                    u.getTelefone() == null ||
                    u.getDataNascimento() == null ||
                    u.getGenero() == null));
        } catch (Exception e) {
            assertThat(e).isNotNull();
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    // Login
    // =========================================================================

    @Nested
    @DisplayName("Login — autenticação do usuário")
    class Login {

        @Test
        @DisplayName("Deve retornar 200 e o token quando credenciais são válidas")
        void login_CredenciaisValidas_Retorna200ComToken() {
            // Arrange
            LoginRequestDTO dto = new LoginRequestDTO("ana@runconnect.com", "Senha@123");

            Usuario usuario = TestDataFactory.criarUsuarioValido(1, "Ana Corredora", "ana@runconnect.com");

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(usuario, null,
                    usuario.getAuthorities());

            when(authenticationManager.authenticate(any())).thenReturn(authToken);
            when(tokenService.gerarToken(usuario)).thenReturn("jwt_token_gerado");

            // Act
            ResponseEntity<LoginResponseDTO> resposta = authenticationController.login(dto);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().token()).isEqualTo("jwt_token_gerado");
            assertThat(resposta.getBody().email()).isEqualTo("ana@runconnect.com");
            assertThat(resposta.getBody().nome()).isEqualTo("Ana Corredora");
            verify(authenticationManager, times(1)).authenticate(any());
            verify(tokenService, times(1)).gerarToken(usuario);
        }

    }
}