package com.runConnect.auth_api.Controller;

import com.runConnect.auth_api.controller.AuthenticationController;
import com.runConnect.auth_api.dto.CadastroRequestDTO;
import com.runConnect.auth_api.dto.UsuarioPublicoDTO;
import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.model.enums.Genero;
import com.runConnect.auth_api.repository.SeguidoresRepository;
import com.runConnect.auth_api.repository.UsuarioRepository;
import com.runConnect.auth_api.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class AuthenticationControllerTest {

    // Validar senha curta

    @Nested
    @DisplayName("Cenário 4 — Validar senha curta")
    class ValidarSenhaCurta {

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

        @ExtendWith(MockitoExtension.class)
        @DisplayName("Cenário 4 — Validação de força de senha")
        class ValidarForcaSenhaTest {

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

            
            // Factory — DTO base com todos os campos obrigatórios preenchidos
            // (imagemUrl ausente pois é opcional)
            // -------------------------------------------------------------------------
            private CadastroRequestDTO dtoComSenha(String senha) {
                return new CadastroRequestDTO(
                        "Corredor Teste",
                        "teste@runconnect.com",
                        senha,
                        "123.456.789-00",
                        "(11) 99999-9999",
                        LocalDate.of(1993, 8, 25),
                        null,
                        Genero.MASCULINO);
            }

            // Senhas INVÁLIDAS — devem ser rejeitadas
            

            @Nested
            @DisplayName("Senhas que devem ser rejeitadas")
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
                @DisplayName("Deve rejeitar senhas com apenas letras ou apenas números")
                @ValueSource(strings = {
                        "abcdefgh", // só letras minúsculas
                        "ABCDEFGH", // só letras maiúsculas
                        "12345678", // só números
                        "!@#$%^&*" // só especiais — sem letra ou número legível
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

          
            // Senhas VÁLIDAS — devem ser aceitas
        
            @Nested
            @DisplayName("Senhas que devem ser aceitas")
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
                    // Arrange
                    when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
                    when(passwordEncoder.encode(senha)).thenReturn("hash_seguro");
                    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

                    // Act
                    ResponseEntity<Void> resposta = authenticationController.cadastrar(dtoComSenha(senha));

                    // Assert
                    assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    verify(usuarioRepository, times(1)).save(any(Usuario.class));
                    verify(passwordEncoder, times(1)).encode(senha);
                }
            }
        }

        // 5 — Validar campo obrigatório vazio

        @ExtendWith(MockitoExtension.class)
        @DisplayName("Cenário 5 — Validar campo obrigatório vazio ou nulo")
        class ValidarCampoObrigatorioTest {

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

     
            // Factory — DTO base completamente válido (imagem ausente, pois é opcional)
          

            private static CadastroRequestDTO dtoPadrao() {
                return new CadastroRequestDTO(
                        "Corredor Válido",
                        "valido@runconnect.com",
                        "senha_segura_123",
                        "555.555.555-55",
                        "(11) 99999-9999",
                        LocalDate.of(1993, 8, 25),
                        null, // imagemUrl — opcional, intencionalmente ausente
                        Genero.MASCULINO);
            }

            // Confirma que dados completos funcionam SEM imagem

            @Test
            @DisplayName("Deve salvar com sucesso quando todos os campos obrigatórios estão presentes (sem imagem)")
            void cadastrar_DadosObrigatoriosCompletos_SemImagem_DevePersistirComSucesso() {
                // Arrange
                CadastroRequestDTO dto = dtoPadrao();

                when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
                when(passwordEncoder.encode(dto.senha())).thenReturn("hash_seguro");
                when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

                // Act
                ResponseEntity<Void> resposta = authenticationController.cadastrar(dto);

                // Assert
                assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                verify(usuarioRepository, times(1)).save(any(Usuario.class));
                verify(passwordEncoder, times(1)).encode(dto.senha());
            }

            // Confirma que dados completos funcionam Com imagem

            @Test
            @DisplayName("Deve salvar com sucesso quando imagem é fornecida (campo opcional preenchido)")
            void cadastrar_ComImagemFornecida_TambemDevePersistir() {
                // Arrange — mesma lógica, mas com imagemUrl preenchida para garantir
                // que o campo opcional não causa efeito colateral quando presente
                CadastroRequestDTO dto = new CadastroRequestDTO(
                        "Corredor Com Foto",
                        "comfoto@runconnect.com",
                        "senha_segura_456",
                        "666.666.666-66",
                        "(11) 88888-8888",
                        LocalDate.of(1990, 3, 10),
                        "http://img.com/foto.png", // imagemUrl presente
                        Genero.FEMININO);

                when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
                when(passwordEncoder.encode(dto.senha())).thenReturn("hash_seguro");
                when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

                // Act
                ResponseEntity<Void> resposta = authenticationController.cadastrar(dto);

                // Assert
                assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                verify(usuarioRepository, times(1)).save(any(Usuario.class));
            }

            // Campos obrigatórios — um teste por campo nulo

            @Test
            @DisplayName("Não deve salvar quando nome é nulo")
            void cadastrar_NomeNulo_NaoDevePersistir() {
                CadastroRequestDTO dto = new CadastroRequestDTO(
                        null, // ← campo ausente
                        "semdado@runconnect.com",
                        "senha_segura_789",
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
                        "senha_segura_789",
                        "200.200.200-00",
                        "(11) 66666-6666",
                        LocalDate.of(1995, 1, 1),
                        null,
                        Genero.MASCULINO);
                when(usuarioRepository.findByEmail(null)).thenReturn(Optional.empty());
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
                        "senha_segura_789",
                        null, // ← campo ausente
                        "(11) 44444-4444",
                        LocalDate.of(1995, 1, 1),
                        null,
                        Genero.MASCULINO);
                when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
                when(passwordEncoder.encode(any())).thenReturn("hash");
                assertCampoNuloNaoSalva(dto);
            }

            @Test
            @DisplayName("Não deve salvar quando telefone é nulo")
            void cadastrar_TelefoneNulo_NaoDevePersistir() {
                CadastroRequestDTO dto = new CadastroRequestDTO(
                        "Sem Telefone",
                        "semtelefone@runconnect.com",
                        "senha_segura_789",
                        "400.400.400-00",
                        null, // ← campo ausente
                        LocalDate.of(1995, 1, 1),
                        null,
                        Genero.MASCULINO);
                when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
                when(passwordEncoder.encode(any())).thenReturn("hash");
                assertCampoNuloNaoSalva(dto);
            }

            @Test
            @DisplayName("Não deve salvar quando data de nascimento é nula")
            void cadastrar_DataNascimentoNula_NaoDevePersistir() {
                CadastroRequestDTO dto = new CadastroRequestDTO(
                        "Sem Data",
                        "semdata@runconnect.com",
                        "senha_segura_789",
                        "500.500.500-00",
                        "(11) 33333-3333",
                        null, // ← campo ausente
                        null,
                        Genero.MASCULINO);
                when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
                when(passwordEncoder.encode(any())).thenReturn("hash");
                assertCampoNuloNaoSalva(dto);
            }

            @Test
            @DisplayName("Não deve salvar quando gênero é nulo")
            void cadastrar_GeneroNulo_NaoDevePersistir() {
                CadastroRequestDTO dto = new CadastroRequestDTO(
                        "Sem Genero",
                        "semgenero@runconnect.com",
                        "senha_segura_789",
                        "600.600.600-00",
                        "(11) 22222-2222",
                        LocalDate.of(1995, 1, 1),
                        null,
                        null // ← campo ausente
                );
                when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
                when(passwordEncoder.encode(any())).thenReturn("hash");
                assertCampoNuloNaoSalva(dto);
            }

            private void assertCampoNuloNaoSalva(CadastroRequestDTO dto) {
                try {
                    authenticationController.cadastrar(dto);
                    // Se não lançou exceção: verifica que save() não foi chamado
                    // com nenhum campo obrigatório nulo na entidade
                    verify(usuarioRepository, never()).save(argThat(u -> u.getNome() == null ||
                            u.getEmail() == null ||
                            u.getSenha() == null ||
                            u.getCpf() == null ||
                            u.getTelefone() == null ||
                            u.getDataNascimento() == null ||
                            u.getGenero() == null));
                } catch (Exception e) {
                    // Qualquer exceção indica rejeição correta pelo sistema
                    assertThat(e).isNotNull();
                    verify(usuarioRepository, never()).save(any(Usuario.class));
                }
            }
        }
    }
}
