package com.runConnect.auth_api.controller;
import com.runConnect.auth_api.controller.UsuarioController;
import com.runConnect.auth_api.dto.UsuarioPublicoDTO;
import com.runConnect.auth_api.model.Seguidores;
import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.repository.SeguidoresRepository;
import com.runConnect.auth_api.repository.UsuarioRepository;
import com.runConnect.auth_api.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // ← único, na classe principal
@DisplayName("UsuarioController — Testes Unitários")
class UsuarioControllerTest {

    // Mocks declarados uma única vez — herdados por todos os @Nested
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SeguidoresRepository seguidoresRepository;

    @InjectMocks
    private UsuarioController usuarioController;

    // Usuários reutilizados nos testes
    private final Usuario u1 = TestDataFactory.criarUsuarioValido(1, "Ana Corredora", "ana@runconnect.com");
    private final Usuario u2 = TestDataFactory.criarUsuarioValido(2, "Carlos Velocista", "carlos@runconnect.com");

    // Cenário 1 e 2 — Buscar usuário por ID

    @Nested
    @DisplayName("Cenário 1 e 2 — Buscar usuário por ID")
    class BuscarUsuarioPorId {

        @Test
        @DisplayName("Deve retornar 200 e os dados do usuário quando o ID existe")
        void buscarUsuarioPorId_Existente_Retorna200ComDados() {
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(u1));

            ResponseEntity<UsuarioPublicoDTO> resposta = usuarioController.buscarUsuarioPorId(1);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().id()).isEqualTo(1);
            assertThat(resposta.getBody().nome()).isEqualTo("Ana Corredora");
            assertThat(resposta.getBody().email()).isEqualTo("ana@runconnect.com");
            verify(usuarioRepository, times(1)).findById(1);
        }

        @Test
        @DisplayName("Deve retornar 404 quando o ID do usuário não existe")
        void buscarUsuarioPorId_Inexistente_Retorna404() {
            when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

            ResponseEntity<UsuarioPublicoDTO> resposta = usuarioController.buscarUsuarioPorId(999);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resposta.getBody()).isNull();
            verify(usuarioRepository, times(1)).findById(999);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há usuários cadastrados")
        void buscarTodosUsuarios_SemCadastros_RetornaListaVazia() {
            when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

            ResponseEntity<List<UsuarioPublicoDTO>> resposta = usuarioController.buscarTodosUsuarios();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull().isEmpty();
            verify(usuarioRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve retornar 200 e lista com todos os usuários cadastrados")
        void buscarTodosUsuarios_ComCadastros_RetornaListaPreenchida() {
            when(usuarioRepository.findAll()).thenReturn(List.of(u1, u2));

            ResponseEntity<List<UsuarioPublicoDTO>> resposta = usuarioController.buscarTodosUsuarios();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(2);
            assertThat(resposta.getBody())
                    .extracting(UsuarioPublicoDTO::nome)
                    .containsExactly("Ana Corredora", "Carlos Velocista");
        }
    }

    // Cenário 3 — Excluir usuário

    @Nested
    @DisplayName("Cenário 3 — Excluir usuário")
    class DeletarUsuario {

        @Test
        @DisplayName("Deve retornar 204 e chamar deleteById quando o usuário existe")
        void deletarUsuario_Existente_Retorna204EDeleteEhChamado() {
            when(usuarioRepository.existsById(1)).thenReturn(true);
            doNothing().when(usuarioRepository).deleteById(1);

            ResponseEntity<Void> resposta = usuarioController.deletarUsuario(1);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(usuarioRepository, times(1)).deleteById(1);
            verify(usuarioRepository, never()).deleteById(argThat(id -> !id.equals(1)));
        }

        @Test
        @DisplayName("Deve retornar 404 e NÃO chamar deleteById quando o usuário não existe")
        void deletarUsuario_Inexistente_Retorna404ESemDelete() {
            when(usuarioRepository.existsById(999)).thenReturn(false);

            ResponseEntity<Void> resposta = usuarioController.deletarUsuario(999);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(usuarioRepository, never()).deleteById(anyInt());
        }
    }

    // Seguir e deixar de seguir
    

    @Nested
    @DisplayName("Seguir e deixar de seguir usuário")
    class SeguirUsuario {

        @Test
        @DisplayName("Deve seguir com sucesso quando ambos os usuários existem")
        void seguirUsuario_Sucesso() {
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(u1));
            when(usuarioRepository.findById(2)).thenReturn(Optional.of(u2));
            when(seguidoresRepository.findById_SeguidorIdAndId_SeguidoId(1, 2))
                    .thenReturn(Optional.empty());
            when(seguidoresRepository.save(any(Seguidores.class))).thenAnswer(inv -> inv.getArgument(0));

            ResponseEntity<Void> resposta = usuarioController.seguirUsuario(2, 1);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(seguidoresRepository, times(1)).save(any(Seguidores.class));
        }

        @Test
        @DisplayName("Deve lançar 404 quando seguidor não existe")
        void seguirUsuario_SeguidorInexistente_Retorna404() {
            when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioController.seguirUsuario(2, 999))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(
                            ((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND));

            verify(seguidoresRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar 409 quando já segue o usuário")
        void seguirUsuario_JaSegue_LancaConflict() {
            Seguidores relacaoExistente = new Seguidores(u1, u2);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(u1));
            when(usuarioRepository.findById(2)).thenReturn(Optional.of(u2));
            when(seguidoresRepository.findById_SeguidorIdAndId_SeguidoId(1, 2))
                    .thenReturn(Optional.of(relacaoExistente));

            assertThatThrownBy(() -> usuarioController.seguirUsuario(2, 1))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(
                            ((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.CONFLICT));

            verify(seguidoresRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve deixar de seguir com sucesso quando a relação existe")
        void deixarDeSeguir_Sucesso() {
            Seguidores relacao = new Seguidores(u1, u2);
            when(seguidoresRepository.findById_SeguidorIdAndId_SeguidoId(1, 2))
                    .thenReturn(Optional.of(relacao));

            ResponseEntity<Void> resposta = usuarioController.deixarDeSeguirUsuario(2, 1);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(seguidoresRepository, times(1)).delete(relacao);
        }

        @Test
        @DisplayName("Deve lançar 404 ao deixar de seguir quando relação não existe")
        void deixarDeSeguir_RelacaoInexistente_Retorna404() {
            when(seguidoresRepository.findById_SeguidorIdAndId_SeguidoId(1, 2))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioController.deixarDeSeguirUsuario(2, 1))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(
                            ((ResponseStatusException) ex).getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND));

            verify(seguidoresRepository, never()).delete(any());
        }
    }

    // Listar seguidores e seguindo

    @Nested
    @DisplayName("Listar seguidores e seguindo")
    class ListarSeguidores {

        @Test
        @DisplayName("Deve retornar lista de seguidores quando usuário existe")
        void listarSeguidores_UsuarioExistente_RetornaLista() {
            when(usuarioRepository.existsById(1)).thenReturn(true);
            when(seguidoresRepository.findSeguidoresByUsuarioId(1)).thenReturn(List.of(u2));

            ResponseEntity<List<UsuarioPublicoDTO>> resposta = usuarioController.listarSeguidores(1);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);
            assertThat(resposta.getBody().get(0).nome()).isEqualTo("Carlos Velocista");
        }

        @Test
        @DisplayName("Deve retornar 404 ao listar seguidores de usuário inexistente")
        void listarSeguidores_UsuarioInexistente_Retorna404() {
            when(usuarioRepository.existsById(999)).thenReturn(false);

            ResponseEntity<List<UsuarioPublicoDTO>> resposta = usuarioController.listarSeguidores(999);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(seguidoresRepository, never()).findSeguidoresByUsuarioId(anyInt());
        }

        @Test
        @DisplayName("Deve retornar lista de seguindo quando usuário existe")
        void listarSeguindo_UsuarioExistente_RetornaLista() {
            when(usuarioRepository.existsById(1)).thenReturn(true);
            when(seguidoresRepository.findSeguindoByUsuarioId(1)).thenReturn(List.of(u2));

            ResponseEntity<List<UsuarioPublicoDTO>> resposta = usuarioController.listarSeguindo(1);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar 404 ao listar seguindo de usuário inexistente")
        void listarSeguindo_UsuarioInexistente_Retorna404() {
            when(usuarioRepository.existsById(999)).thenReturn(false);

            ResponseEntity<List<UsuarioPublicoDTO>> resposta = usuarioController.listarSeguindo(999);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(seguidoresRepository, never()).findSeguindoByUsuarioId(anyInt());
        }
    }

    // Atualizar descrição e imagem

    @Nested
    @DisplayName("Atualizar perfil do usuário")
    class AtualizarPerfil {

        @Test
        @DisplayName("Deve atualizar descrição com sucesso quando usuário existe")
        void atualizarDescricao_UsuarioExistente_Retorna200() {
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(u1));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            ResponseEntity<UsuarioPublicoDTO> resposta = usuarioController
                    .atualizarDescricao(1, Map.of("descricao", "Nova descrição"));

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve retornar 404 ao atualizar descrição de usuário inexistente")
        void atualizarDescricao_UsuarioInexistente_Retorna404() {
            when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

            ResponseEntity<UsuarioPublicoDTO> resposta = usuarioController
                    .atualizarDescricao(999, Map.of("descricao", "Nova descrição"));

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve atualizar imagem com sucesso quando usuário existe")
        void atualizarImagem_UsuarioExistente_Retorna200() {
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(u1));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            ResponseEntity<UsuarioPublicoDTO> resposta = usuarioController
                    .atualizarImagem(1, Map.of("imagemUrl", "http://nova-imagem.com/foto.png"));

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve retornar 404 ao atualizar imagem de usuário inexistente")
        void atualizarImagem_UsuarioInexistente_Retorna404() {
            when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

            ResponseEntity<UsuarioPublicoDTO> resposta = usuarioController
                    .atualizarImagem(999, Map.of("imagemUrl", "http://nova-imagem.com/foto.png"));

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }
}