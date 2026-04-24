package com.co.ucentral.gestionResiduos.back.education;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class FeedbackRequestDTO {
    @NotNull
    private Boolean useful;
}

