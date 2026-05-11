package com.co.ucentral.gestionResiduos.back.quiz.dto;

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
public class QuizAdminResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Long contentId;
    private Integer pointsPerQuestion;
    private LocalDateTime createdAt;
    private List<QuestionAdmin> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAdmin {
        private Long id;
        private String text;
        private Integer correctIndex;
        private List<String> options;
    }
}