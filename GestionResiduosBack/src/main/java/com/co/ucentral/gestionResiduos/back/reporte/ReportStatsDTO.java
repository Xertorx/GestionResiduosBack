package com.co.ucentral.gestionResiduos.back.reporte;

import lombok.Data;
import java.util.List;

@Data
public class ReportStatsDTO {
    private int total;
    private List<StatusCountDTO> byStatus;
    private List<TrendDTO> trend;
    private double resolvedPercentage;
    private double pendingPercentage;
    private double avgResolutionTime;
}

