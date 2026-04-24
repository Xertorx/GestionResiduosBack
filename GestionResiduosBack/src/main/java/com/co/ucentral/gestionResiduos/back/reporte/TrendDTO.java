package com.co.ucentral.gestionResiduos.back.reporte;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrendDTO {
    private String date;
    private int count;
}

