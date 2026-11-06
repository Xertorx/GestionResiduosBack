package com.co.ucentral.gestionResiduos.back.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizPlayResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Long contentId;
    private Integer pointsPerQuestion;
    private List<QuestionPlay> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionPlay {
        private Long id;
        private String text;
        private List<String> options;
    }
}