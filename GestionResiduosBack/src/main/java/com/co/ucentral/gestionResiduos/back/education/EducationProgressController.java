package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/education")
public class EducationProgressController {

    private final EducationProgressService progressService;

    public EducationProgressController(EducationProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * GET /api/v1/education/{contentId}/progress
     * Obtener el progreso del usuario autenticado en un contenido.
     */
    @GetMapping("/{contentId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProgress(@PathVariable Long contentId, Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(progressService.getProgress(email, contentId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/education/{contentId}/sections/{sectionId}/complete
     * Marcar una sección como completada por el ciudadano autenticado.
     */
    @PostMapping("/{contentId}/sections/{sectionId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> completeSection(
            @PathVariable Long contentId,
            @PathVariable Long sectionId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(progressService.completeSection(email, contentId, sectionId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/education/{contentId}/complete
     * Marcar el contenido completo (después del quiz).
     * Requiere que el usuario haya completado todas las secciones.
     */
    @PostMapping("/{contentId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> completeContent(@PathVariable Long contentId, Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(progressService.completeContent(email, contentId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}

