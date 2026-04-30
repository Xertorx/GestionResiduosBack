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
public class QuizResultDTO {
    private Integer correctAnswers;
    private Integer totalQuestions;
    private Integer pointsEarned;
    private Integer userTotalPoints;
    private boolean firstAttempt;
    private List<QuestionResult> perQuestion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResult {
        private Long questionId;
        private Integer selectedIndex;
        private Integer correctIndex;
        private boolean wasCorrect;
    }
}