package com.co.ucentral.gestionResiduos.back.forum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.co.ucentral.gestionResiduos.back.forum.entity.TopicStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private TopicStatus estado;
}

