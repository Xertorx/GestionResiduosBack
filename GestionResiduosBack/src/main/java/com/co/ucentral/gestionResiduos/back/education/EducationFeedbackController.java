package com.co.ucentral.gestionResiduos.back.education;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/education")
@RequiredArgsConstructor
public class EducationFeedbackController {

    private final EducationFeedbackService feedbackService;

    // Permitir feedback anónimo; si el usuario está autenticado, guardamos su email
    @PostMapping("/{id}/feedback")
    public ResponseEntity<Map<String, Object>> giveFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequestDTO dto,
            Authentication authentication) {

        String email = null;
        if (authentication != null && authentication.isAuthenticated()) {
            email = authentication.getName();
        }

        boolean already = feedbackService.saveFeedback(id, dto.getUseful(), email);

        Map<String, Object> resp = new java.util.HashMap<>();
        if (already) {
            resp.put("message", "Ya se registró el feedback");
        } else {
            resp.put("message", "Feedback registrado");
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}/feedback/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long id) {
        Map<String, Object> stats = feedbackService.getFeedbackStats(id);
        return ResponseEntity.ok(stats);
    }
}

