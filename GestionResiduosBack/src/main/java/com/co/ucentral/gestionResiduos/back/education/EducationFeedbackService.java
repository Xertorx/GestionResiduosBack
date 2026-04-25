package com.co.ucentral.gestionResiduos.back.education;

import com.co.ucentral.gestionResiduos.back.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EducationFeedbackService {

    private final EducationContentRepository contentRepository;
    private final EducationFeedbackRepository feedbackRepository;

    @Transactional
    public boolean saveFeedback(Long contentId, boolean useful, String userEmail) {
        EducationContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado con ID: " + contentId));

        boolean already = false;

        // Si el usuario está autenticado (email disponible), verificar si ya votó
        if (userEmail != null && !userEmail.isBlank()) {
            // normalizar email a minúsculas para evitar duplicados por case
            String normalizedEmail = userEmail.trim().toLowerCase();
            already = feedbackRepository.existsByContent_IdAndUserEmailIgnoreCase(contentId, normalizedEmail);
            if (!already) {
                EducationFeedback feedback = new EducationFeedback();
                feedback.setContent(content);
                feedback.setUseful(useful);
                feedback.setUserEmail(normalizedEmail);
                feedbackRepository.save(feedback);
            }
        } else {
            // Caso raro: si no hay email, persistimos el feedback (aunque POST ahora requiere autenticación)
            EducationFeedback feedback = new EducationFeedback();
            feedback.setContent(content);
            feedback.setUseful(useful);
            feedback.setUserEmail(null);
            feedbackRepository.save(feedback);
        }

        // No devolver estadísticas aquí; el endpoint de estadísticas es /{id}/feedback/stats
        return already;
    }

    public Map<String, Object> getFeedbackStats(Long contentId) {
        contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado con ID: " + contentId));

        long usefulCount = feedbackRepository.countByContent_IdAndUseful(contentId, true);
        long notUsefulCount = feedbackRepository.countByContent_IdAndUseful(contentId, false);

        Map<String, Object> stats = new HashMap<>();
        stats.put("useful", usefulCount);
        stats.put("notUseful", notUsefulCount);
        stats.put("total", usefulCount + notUsefulCount);

        return stats;
    }
}


