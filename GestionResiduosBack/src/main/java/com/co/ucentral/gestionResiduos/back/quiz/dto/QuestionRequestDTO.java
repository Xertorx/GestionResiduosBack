package com.co.ucentral.gestionResiduos.back.quiz.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionRequestDTO {
    private String text;
    private Integer correctIndex;
    private List<String> options = new ArrayList<>();
}