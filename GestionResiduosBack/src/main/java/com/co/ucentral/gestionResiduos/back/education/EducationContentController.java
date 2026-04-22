package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // ── HU21 mejorada: el admin sube MÚLTIPLES archivos ──
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> uploadContent(
            @ModelAttribute EducationContentRequestDTO dto,
            @RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return new ResponseEntity<>("Debes enviar al menos un archivo.", HttpStatus.BAD_REQUEST);
            }
            EducationContent savedContent = service.saveContent(dto, files);
            return new ResponseEntity<>(savedContent, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error al procesar los archivos: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // ── HU20: el usuario obtiene la lista de contenidos ──
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

    // ── NUEVO: editar metadata (título, descripción, categoría) ──
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> updateContent(
            @PathVariable Long id,
            @RequestBody EducationContentRequestDTO dto) {
        try {
            EducationContent updated = service.updateContent(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error al actualizar: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // ── Eliminar contenido por ID ──
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
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