package com.co.ucentral.gestionResiduos.back.quiz.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubmitAttemptDTO {
    private List<Answer> answers = new ArrayList<>();

    @Data
    public static class Answer {
        private Long questionId;
        private Integer selectedIndex;
    }
}