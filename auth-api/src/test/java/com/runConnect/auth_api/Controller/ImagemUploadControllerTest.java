package com.runConnect.auth_api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImagemUploadController — Testes Unitários")
class ImagemUploadControllerTest {

    @InjectMocks
    private ImagemUploadController imagemUploadController;

    // Cria um diretório temporário no sistema que é apagado automaticamente após os testes
    @TempDir
    Path tempDir; 

    @BeforeEach
    void setUp() {
        // Injeta o diretório temporário na variável @Value("${file.upload-dir}")
        ReflectionTestUtils.setField(imagemUploadController, "uploadDir", tempDir.toString());
        
        // Mock do contexto da requisição HTTP (necessário para o ServletUriComponentsBuilder não dar erro)
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o ficheiro estiver vazio")
    void uploadImagem_FicheiroVazio_RetornaBadRequest() {
        // Arrange: Cria um ficheiro simulado com 0 bytes
        MultipartFile file = new MockMultipartFile("file", new byte[0]);

        // Act
        ResponseEntity<String> response = imagemUploadController.uploadImagem(file);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("selecione um ficheiro");
    }

    @Test
    @DisplayName("Deve fazer o upload com sucesso e retornar 200 OK com a URI")
    void uploadImagem_FicheiroValido_RetornaOkComUri() {
        // Arrange: Cria um ficheiro simulado com dados
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "perfil-teste.png", 
                "image/png", 
                "conteudo-falso-de-imagem".getBytes()
        );

        // Act
        ResponseEntity<String> response = imagemUploadController.uploadImagem(file);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("/uploads/");
        assertThat(response.getBody()).contains(".png");
    }

    @Test
    @DisplayName("Deve retornar 500 Internal Server Error quando ocorrer erro de Leitura/Escrita (IOException)")
    void uploadImagem_ErroDeIO_RetornaInternalServerError() throws IOException {
        // Arrange: Usamos o mock() tradicional para forçar o ficheiro a lançar uma exceção
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("erro.jpg");
        when(mockFile.getInputStream()).thenThrow(new IOException("Simulação de disco cheio"));

        // Act
        ResponseEntity<String> response = imagemUploadController.uploadImagem(mockFile);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Não foi possível fazer o upload");
    }
}