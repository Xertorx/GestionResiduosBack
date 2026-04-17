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
@CrossOrigin(
        origins = {"http://localhost:4200", "http://localhost:4000"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        allowCredentials = "true"
)
public class EducationContentController {

    private final EducationContentService service;

    @Autowired
    public EducationContentController(EducationContentService service) {
        this.service = service;
    }

    // ── HU21: El admin sube contenido multimedia ──
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadContent(
            @ModelAttribute EducationContentRequestDTO dto,
            @RequestParam("file") MultipartFile file) {
        try {
            EducationContent savedContent = service.saveContent(dto, file);
            return new ResponseEntity<>(savedContent, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error al procesar el archivo: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // ── HU20: El usuario obtiene la lista de contenidos ──
    @GetMapping
    public ResponseEntity<List<EducationContent>> getAllContents() {
        return ResponseEntity.ok(service.getAllContents());
    }

    // ── Obtener un contenido por ID ──
    @GetMapping("/{id}")
    public ResponseEntity<?> getContentById(@PathVariable Long id) {
        return service.getContentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Eliminar contenido por ID ──
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable Long id) {
        try {
            service.deleteContent(id);
            return ResponseEntity.ok("Contenido eliminado correctamente");
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error al eliminar: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
