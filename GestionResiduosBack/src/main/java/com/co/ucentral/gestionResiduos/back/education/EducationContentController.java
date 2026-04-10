package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/education")
public class EducationContentController {

    private final EducationContentService service;

    @Autowired
    public EducationContentController(EducationContentService service) {
        this.service = service;
    }

    // Endpoint para HU21: El admin sube contenido multimedia
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadContent(
            @ModelAttribute EducationContentRequestDTO dto,
            @RequestParam("file") MultipartFile file) {
        try {
            EducationContent savedContent = service.saveContent(dto, file);
            return new ResponseEntity<>(savedContent, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al procesar el archivo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para HU20: El usuario obtiene la lista de contenidos
    @GetMapping
    public ResponseEntity<List<EducationContent>> getAllContents() {
        return ResponseEntity.ok(service.getAllContents());
    }
}