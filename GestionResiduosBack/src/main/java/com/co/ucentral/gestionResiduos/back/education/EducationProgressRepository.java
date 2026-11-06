package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EducationProgressRepository extends JpaRepository<EducationProgress, Long> {

    /** Secciones completadas por el usuario en un contenido */
    List<EducationProgress> findByUserEmailAndContent_IdAndSectionIsNotNull(String userEmail, Long contentId);

    /** Verifica si el usuario ya completó una sección específica */
    boolean existsByUserEmailAndContent_IdAndSection_Id(String userEmail, Long contentId, Long sectionId);

    /** Verifica si el usuario ya marcó el contenido completo (section = null) */
    boolean existsByUserEmailAndContent_IdAndSectionIsNull(String userEmail, Long contentId);

    /** Cantidad de secciones completadas por usuario en un contenido */
    @Query("SELECT COUNT(p) FROM EducationProgress p WHERE p.userEmail = :email AND p.content.id = :contentId AND p.section IS NOT NULL")
    long countCompletedSections(@Param("email") String email, @Param("contentId") Long contentId);
}

