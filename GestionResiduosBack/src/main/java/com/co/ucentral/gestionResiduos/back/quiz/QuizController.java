package com.co.ucentral.gestionResiduos.back.quiz;

import com.co.ucentral.gestionResiduos.back.quiz.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // ── ADMIN: Crear quiz para un contenido ──
    @PostMapping("/content/{contentId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> create(@PathVariable Long contentId,
                                    @RequestBody QuizRequestDTO dto) {
        try {
            return new ResponseEntity<>(quizService.createForContent(contentId, dto), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // ── ADMIN: Obtener quiz (con respuestas correctas) por contenido ──
    @GetMapping("/content/{contentId}/admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> getAdminByContent(@PathVariable Long contentId) {
        try {
            return ResponseEntity.ok(quizService.getAdminByContent(contentId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // ── USUARIO: Obtener quiz para JUGAR (sin respuestas correctas) ──
    @GetMapping("/content/{contentId}")
    public ResponseEntity<?> getPlayByContent(@PathVariable Long contentId) {
        try {
            return ResponseEntity.ok(quizService.getPlayByContent(contentId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // ── Verificación rápida: ¿este contenido tiene quiz? ──
    @GetMapping("/content/{contentId}/exists")
    public ResponseEntity<Map<String, Boolean>> exists(@PathVariable Long contentId) {
        return ResponseEntity.ok(Map.of("exists", quizService.existsForContent(contentId)));
    }

    // ── ADMIN: Editar ──
    @PutMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> update(@PathVariable Long quizId,
                                    @RequestBody QuizRequestDTO dto) {
        try {
            return ResponseEntity.ok(quizService.update(quizId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // ── ADMIN: Eliminar ──
    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> delete(@PathVariable Long quizId) {
        try {
            quizService.delete(quizId);
            return ResponseEntity.ok(Map.of("message", "Quiz eliminado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // ── USUARIO: Enviar respuestas y obtener resultado + puntos ──
    @PostMapping("/{quizId}/attempt")
    public ResponseEntity<?> submit(@PathVariable Long quizId,
                                    @RequestBody SubmitAttemptDTO dto,
                                    Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(quizService.submitAttempt(quizId, email, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}