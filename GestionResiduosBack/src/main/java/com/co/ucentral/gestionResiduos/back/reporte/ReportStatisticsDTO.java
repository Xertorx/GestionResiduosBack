package com.co.ucentral.gestionResiduos.back.reporte;

import lombok.Data;

/**
 * DTO para estadísticas de reportes (HU28)
 */
@Data
public class ReportStatisticsDTO {
    private int total;
    private int pending;
    private int inReview;
    private int resolved;
    private int rejected;
    private int criticalPoints;
    private int calendarNonCompliance;
}

