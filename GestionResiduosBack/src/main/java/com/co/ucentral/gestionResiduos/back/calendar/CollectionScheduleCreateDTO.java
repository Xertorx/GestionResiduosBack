package com.co.ucentral.gestionResiduos.back.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionScheduleCreateDTO {

    @NotNull(message = "El ID de la localidad es obligatorio")
    private Integer districtId;

    @NotBlank(message = "El tipo de residuo es obligatorio")
    private String residueType;

    @NotBlank(message = "El día de la semana es obligatorio")
    private String dayOfWeek;

    @NotBlank(message = "La hora de inicio es obligatoria")
    private String startTime;

    @NotBlank(message = "La hora de fin es obligatoria")
    private String endTime;

    private String description;
}

