package com.co.ucentral.gestionResiduos.back.quiz.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuizRequestDTO {
    private String title;
    private String description;
    private Integer pointsPerQuestion;
    private List<QuestionRequestDTO> questions = new ArrayList<>();
}