package com.runConnect.auth_api.Controller;

import com.runConnect.auth_api.controller.UsuarioController;
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

@ExtendWith(MockitoExtension.class)
public class UsuarioControllerTest {
    // Método de teste na qual cria um usuáiro com todos os campos obrigatórios preenchidos.
    private Usuario criarUsuarioValido(Integer id,String nome, String email){
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("senha123");
        usuario.setDataNascimento(LocalDate.of(1990, 1, 1));
        usuario.setGenero(Genero.MASCULINO);
        usuario.setDescricao("Descrição do usuário");
        usuario.setImagemUrl("http://img.com/foto.png");
        return usuario;
    }

    // Buscar usuário (existente e não existente)
    @Nested
    @DisplayName("Testes para o método buscarUsuario por ID")
    class BuscarUsuarioPorId{
        @Mock
        private UsuarioRepository usuarioRepository;

        @Mock
        private SeguidoresRepository seguidoresRepository;

        @InjectMocks
        private UsuarioController usuarioController;

        private Usuario usuarioExistente;

        @BeforeEach
        void setUp() {
            usuarioExistente = criarUsuarioValido(1, "Ana Corredora", "ana@runconnect.com");
        }

        // Cenario - 01
        @Test
        @DisplayName("Deve retornar 200 e os dados do usuário quando o ID existe")
        void buscarUsuarioPorId_Existente_Retorna200ComDados() {
            // Arrange
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioExistente));
 
            // Act
            ResponseEntity<UsuarioPublicoDTO> resposta = usuarioController.buscarUsuarioPorId(1);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().id()).isEqualTo(1);
            assertThat(resposta.getBody().nome()).isEqualTo("Ana Corredora");
            assertThat(resposta.getBody().email()).isEqualTo("ana@runconnect.com");
 
            verify(usuarioRepository, times(1)).findById(1);
            
        }

        // Cenario - 02
        @Test
        @DisplayName("Deve retornar 404 quando o ID do usuário não existe")void buscarUsuarioPorId_Inexistente_Retorna404() {
            // Arrange
            when(usuarioRepository.findById(999)).thenReturn(Optional.empty());
 
            // Act
            ResponseEntity<UsuarioPublicoDTO> resposta = usuarioController.buscarUsuarioPorId(999);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resposta.getBody()).isNull();
 
            verify(usuarioRepository, times(1)).findById(999);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há usuários cadastrados")
        void buscarTodosUsuarios_SemCadastros_RetornaListaVazia() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());
 
            // Act
            ResponseEntity<List<UsuarioPublicoDTO>> resposta = usuarioController.buscarTodosUsuarios();
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull().isEmpty();
 
            verify(usuarioRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve retornar 200 e lista com todos os usuários cadastrados")
        void buscarTodosUsuarios_ComCadastros_RetornaListaPreenchida() {
            // Arrange
            Usuario u2 = criarUsuarioValido(2, "Carlos Velocista", "carlos@runconnect.com");
            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioExistente, u2));
 
            // Act
            ResponseEntity<List<UsuarioPublicoDTO>> resposta = usuarioController.buscarTodosUsuarios();
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(2);
            assertThat(resposta.getBody())
                    .extracting(UsuarioPublicoDTO::nome)
                    .containsExactly("Ana Corredora", "Carlos Velocista");
        }

    }

     // 3 — Excluir usuário existente
    @Nested
    @DisplayName("Cenário 3 — Excluir usuário")
    class DeletarUsuario {
 
        @Mock
        private UsuarioRepository usuarioRepository;
 
        @Mock
        private SeguidoresRepository seguidoresRepository;
 
        @InjectMocks
        private UsuarioController usuarioController;
 
        @Test
        @DisplayName("Deve retornar 204 e chamar deleteById quando o usuário existe")
        void deletarUsuario_Existente_Retorna204EDeleteEhChamado() {
            // Arrange
            when(usuarioRepository.existsById(1)).thenReturn(true);
            doNothing().when(usuarioRepository).deleteById(1);
 
            // Act
            ResponseEntity<Void> resposta = usuarioController.deletarUsuario(1);
 
            // Assert — status correto
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
 
            // Assert — garantia que deleteById foi chamado exatamente 1 vez com o ID correto
            verify(usuarioRepository, times(1)).deleteById(1);
            verify(usuarioRepository, never()).deleteById(argThat(id -> !id.equals(1)));
        }
 
        @Test
        @DisplayName("Deve retornar 404 e NÃO chamar deleteById quando o usuário não existe")
        void deletarUsuario_Inexistente_Retorna404ESemDelete() {
            // Arrange
            when(usuarioRepository.existsById(999)).thenReturn(false);
 
            // Act
            ResponseEntity<Void> resposta = usuarioController.deletarUsuario(999);
 
            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(usuarioRepository, never()).deleteById(anyInt());
        }
    }

    

     
}
