package com.co.ucentral.gestionResiduos.back.education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationProgressDTO {
    private Long contentId;
    private String contentTitle;
    private boolean contentCompleted;
    private LocalDateTime contentCompletedAt;
    private long totalSections;
    private long completedSections;
    private int progressPercentage;
    private List<SectionProgressDTO> sections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionProgressDTO {
        private Long sectionId;
        private String sectionTitle;
        private boolean completed;
        private LocalDateTime completedAt;
    }
}

