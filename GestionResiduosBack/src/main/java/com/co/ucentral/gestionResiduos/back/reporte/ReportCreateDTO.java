package com.co.ucentral.gestionResiduos.back.reporte;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para crear reportes - Soporta múltiples tipos de reportes (HU10, HU11, HU15)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateDTO {

    @NotNull(message = "El tipo de reporte es obligatorio")
    private String type; // punto_critico | incumplimiento_calendario

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoryId;

    @NotNull(message = "La descripción es obligatoria")
    private String description;

    // HU10: Reporte de puntos críticos
    private Double latitude;      // Obligatorio para punto_critico
    private Double longitude;     // Obligatorio para punto_critico

    // HU10: Foto/Imagen
    private MultipartFile image; // Obligatorio para punto_critico

    // HU15: Reporte de incumplimiento en calendario
    private Integer calendarId; // Obligatorio para incumplimiento_calendario

    // Metadatos
    private String device;   // mobile, web
    private String ip;            // IP del usuario
}

