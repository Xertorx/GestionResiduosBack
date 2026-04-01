package com.co.ucentral.gestionResiduos.back.reporte;

import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Report Entity y DTOs
 */
@Component
public class ReportMapper {

    /**
     * Convierte Report Entity a ReportDTO (para listados y detalles)
     */
    public ReportDTO toDTO(Report report) {
        if (report == null) {
            return null;
        }

        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setType(report.getType());
        dto.setCategoryId(report.getCategoryId());
        dto.setDescription(report.getDescription());
        dto.setLatitude(report.getLatitude());
        dto.setLongitude(report.getLongitude());
        dto.setImageUrl(report.getImageUrl());
        dto.setCalendarId(report.getCalendarId());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());
        dto.setResolvedAt(report.getResolvedAt());
        
        if (report.getUser() != null) {
            dto.setUserId((long)report.getUser().getDocumentNumber());
            dto.setUserName(report.getUser().getNames() + " " + report.getUser().getLastName());
        }

        return dto;
    }

    /**
     * Convierte Report Entity a ReportDTO (versión pública sin datos sensibles)
     */
    public ReportDTO toDTOPublico(Report report) {
        if (report == null) {
            return null;
        }

        ReportDTO dto = toDTO(report);
        // Aquí se pueden omitir campos sensibles si es necesario
        return dto;
    }
}

