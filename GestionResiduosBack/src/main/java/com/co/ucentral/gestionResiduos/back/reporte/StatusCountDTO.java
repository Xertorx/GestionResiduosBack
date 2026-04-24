package com.co.ucentral.gestionResiduos.back.reporte;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusCountDTO {
    private String status;
    private int count;
}

