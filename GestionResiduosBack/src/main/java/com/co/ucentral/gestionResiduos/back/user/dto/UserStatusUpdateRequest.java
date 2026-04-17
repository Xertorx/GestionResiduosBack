package com.co.ucentral.gestionResiduos.back.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateRequest {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "VERIFICADO|INACTIVO", message = "El estado debe ser VERIFICADO o INACTIVO")
    private String status;
}

