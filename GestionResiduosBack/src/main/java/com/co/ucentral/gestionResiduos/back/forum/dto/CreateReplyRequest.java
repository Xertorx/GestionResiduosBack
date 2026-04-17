package com.co.ucentral.gestionResiduos.back.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReplyRequest {

    @NotBlank(message = "El texto de la respuesta es obligatorio")
    @Size(max = 2000, message = "La respuesta no puede exceder 2000 caracteres")
    private String texto;
}

