package com.co.ucentral.gestionResiduos.back.quiz;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class QuizOption {

    @Column(name = "option_text", nullable = false, length = 500)
    private String text;
}