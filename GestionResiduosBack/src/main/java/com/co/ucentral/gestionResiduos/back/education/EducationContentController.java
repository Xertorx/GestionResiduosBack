package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/education")
public class EducationContentController {

    private final EducationContentService service;

    @Autowired
    public EducationContentController(EducationContentService service) {
        this.service = service;
    }

    // ── HU21: el admin sube MÚLTIPLES archivos ──
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> uploadContent(
            @ModelAttribute EducationContentRequestDTO dto,
            @RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debes enviar al menos un archivo."));
            }
            EducationContentResponseDTO saved = service.saveContent(dto, files);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar los archivos", "detalle", e.getMessage()));
        }
    }

    // ── HU20: el usuario obtiene la lista de contenidos ──
    @GetMapping
    public ResponseEntity<List<EducationContentResponseDTO>> getAllContents() {
        return ResponseEntity.ok(service.getAllContents());
    }

    // ── Obtener un contenido por ID ──
    @GetMapping("/{id}")
    public ResponseEntity<?> getContentById(@PathVariable Long id) {
        return service.getContentById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Contenido no encontrado con id: " + id)));
    }

    // ── Secciones: listar secciones de un contenido ──
    @GetMapping("/{id}/sections")
    public ResponseEntity<?> getSections(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getSectionsByContent(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── Crear sección dentro de un contenido (admin) ──
    @PostMapping(value = "/{id}/sections", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> createSection(
            @PathVariable Long id,
            @ModelAttribute EducationSectionRequestDTO dto,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        try {
            EducationSectionResponseDTO saved = service.addSection(id, dto, files);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear sección", "detalle", e.getMessage()));
        }
    }

    // ── Actualizar sección (admin) ──
    @PutMapping("/sections/{sectionId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> updateSection(@PathVariable Long sectionId, @RequestBody EducationSectionRequestDTO dto) {
        try {
            return ResponseEntity.ok(service.updateSection(sectionId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar sección", "detalle", e.getMessage()));
        }
    }

    // ── Eliminar sección (admin) ──
    @DeleteMapping("/sections/{sectionId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> deleteSection(@PathVariable Long sectionId) {
        try {
            service.deleteSection(sectionId);
            return ResponseEntity.ok(Map.of("mensaje", "Sección eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar sección", "detalle", e.getMessage()));
        }
    }

    // ── Editar metadata (título, descripción, categoría) ──
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> updateContent(@PathVariable Long id, @RequestBody EducationContentRequestDTO dto) {
        try {
            return ResponseEntity.ok(service.updateContent(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar", "detalle", e.getMessage()));
        }
    }

    // ── Eliminar contenido por ID ──
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> deleteContent(@PathVariable Long id) {
        try {
            service.deleteContent(id);
            return ResponseEntity.ok(Map.of("mensaje", "Contenido eliminado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar", "detalle", e.getMessage()));
        }
    }
}