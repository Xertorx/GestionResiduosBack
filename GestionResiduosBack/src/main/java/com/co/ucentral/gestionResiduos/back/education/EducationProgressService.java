package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EducationProgressService {

    private final EducationProgressRepository progressRepository;
    private final EducationContentRepository contentRepository;
    private final EducationSectionRepository sectionRepository;

    public EducationProgressService(EducationProgressRepository progressRepository,
                                    EducationContentRepository contentRepository,
                                    EducationSectionRepository sectionRepository) {
        this.progressRepository = progressRepository;
        this.contentRepository = contentRepository;
        this.sectionRepository = sectionRepository;
    }

    /**
     * Marcar una sección como completada por el usuario autenticado.
     * Si ya la completó, retorna el registro existente sin duplicar.
     */
    @Transactional
    public EducationProgressDTO.SectionProgressDTO completeSection(String userEmail, Long contentId, Long sectionId) {
        EducationContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado: " + contentId));

        EducationSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Sección no encontrada: " + sectionId));

        if (progressRepository.existsByUserEmailAndContent_IdAndSection_Id(userEmail, contentId, sectionId)) {
            // Ya completada — retornar estado actual
            return EducationProgressDTO.SectionProgressDTO.builder()
                    .sectionId(sectionId)
                    .sectionTitle(section.getTitle())
                    .completed(true)
                    .completedAt(null)
                    .build();
        }

        EducationProgress progress = EducationProgress.builder()
                .userEmail(userEmail)
                .content(content)
                .section(section)
                .build();

        progressRepository.save(progress);

        return EducationProgressDTO.SectionProgressDTO.builder()
                .sectionId(sectionId)
                .sectionTitle(section.getTitle())
                .completed(true)
                .completedAt(progress.getCompletedAt())
                .build();
    }

    /**
     * Marcar el contenido completo (por ej. después del quiz).
     * Solo se permite si el usuario ya completó todas las secciones.
     */
    @Transactional
    public EducationProgressDTO completeContent(String userEmail, Long contentId) {
        EducationContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado: " + contentId));

        List<EducationSection> sections = sectionRepository.findByContentId(contentId);
        long totalSections = sections.size();
        long completedSections = progressRepository.countCompletedSections(userEmail, contentId);

        // Si hay secciones y no las completó todas, no puede marcar el contenido como completo
        if (totalSections > 0 && completedSections < totalSections) {
            throw new RuntimeException(
                    "Debes completar todas las secciones antes de finalizar el contenido. " +
                    "Completadas: " + completedSections + "/" + totalSections
            );
        }

        boolean alreadyCompleted = progressRepository.existsByUserEmailAndContent_IdAndSectionIsNull(userEmail, contentId);
        if (!alreadyCompleted) {
            EducationProgress progress = EducationProgress.builder()
                    .userEmail(userEmail)
                    .content(content)
                    .section(null)
                    .build();
            progressRepository.save(progress);
        }

        return getProgress(userEmail, contentId);
    }

    /**
     * Obtener el progreso completo de un usuario en un contenido.
     */
    public EducationProgressDTO getProgress(String userEmail, Long contentId) {
        EducationContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado: " + contentId));

        List<EducationSection> sections = sectionRepository.findByContentId(contentId);
        List<EducationProgress> completedSectionRecords =
                progressRepository.findByUserEmailAndContent_IdAndSectionIsNotNull(userEmail, contentId);

        boolean contentCompleted = progressRepository.existsByUserEmailAndContent_IdAndSectionIsNull(userEmail, contentId);
        long totalSections = sections.size();
        long completedCount = completedSectionRecords.size();

        int percentage = totalSections > 0
                ? (int) Math.round((completedCount * 100.0) / totalSections)
                : (contentCompleted ? 100 : 0);

        List<EducationProgressDTO.SectionProgressDTO> sectionDTOs = sections.stream().map(s -> {
            EducationProgress rec = completedSectionRecords.stream()
                    .filter(p -> p.getSection() != null && p.getSection().getId().equals(s.getId()))
                    .findFirst().orElse(null);
            return EducationProgressDTO.SectionProgressDTO.builder()
                    .sectionId(s.getId())
                    .sectionTitle(s.getTitle())
                    .completed(rec != null)
                    .completedAt(rec != null ? rec.getCompletedAt() : null)
                    .build();
        }).collect(Collectors.toList());

        return EducationProgressDTO.builder()
                .contentId(contentId)
                .contentTitle(content.getTitle())
                .contentCompleted(contentCompleted)
                .totalSections(totalSections)
                .completedSections(completedCount)
                .progressPercentage(percentage)
                .sections(sectionDTOs)
                .build();
    }
}

