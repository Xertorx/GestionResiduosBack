package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationFeedbackRepository extends JpaRepository<EducationFeedback, Long> {
    long countByContent_IdAndUseful(Long contentId, boolean useful);
    boolean existsByContent_IdAndUserEmail(Long contentId, String userEmail);
    boolean existsByContent_IdAndUserEmailIgnoreCase(Long contentId, String userEmail);
}


