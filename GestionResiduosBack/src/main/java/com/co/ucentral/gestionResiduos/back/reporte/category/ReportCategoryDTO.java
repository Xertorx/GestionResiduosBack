package com.co.ucentral.gestionResiduos.back.reporte.category;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportCategoryDTO {

    private Integer id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String name;

    private String description;

    private String status;
}

