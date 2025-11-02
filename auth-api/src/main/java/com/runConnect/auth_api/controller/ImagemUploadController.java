
package com.runConnect.auth_api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/uploads")
public class ImagemUploadController {

   
    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/imagem")
    public ResponseEntity<String> uploadImagem(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor, selecione um ficheiro para fazer upload.");
        }

        try {
            
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

           
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            
            Path destinationFile = uploadPath.resolve(uniqueFileName).normalize().toAbsolutePath();

            
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

           
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/") 
                    .path(uniqueFileName)
                    .toUriString();

          
            return ResponseEntity.ok(fileDownloadUri);

        } catch (IOException ex) {
         
            return ResponseEntity.status(500).body("Não foi possível fazer o upload do ficheiro. Erro: " + ex.getMessage());
        }
    }
}
