package com.co.ucentral.gestionResiduos.back.reporte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

/**
 * DTO para responder reportes - Usado en listados y detalles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private Long id;
    private String type;
    private Integer categoryId;
    private String categoryName;
    private String description;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private Integer calendarId;
    private String status;
    private Long userId;
    private String userName;
    private Date createdAt;
    private Date updatedAt;
    private Date resolvedAt;
}

