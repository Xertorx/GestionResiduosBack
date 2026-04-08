package com.co.ucentral.gestionResiduos.back.reporte;

import com.co.ucentral.gestionResiduos.back.reporte.category.ReportCategory;
import com.co.ucentral.gestionResiduos.back.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

/**
 * Entidad para registrar reportes de puntos críticos, incumplimientos, etc.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reportes")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo", nullable = false)
    @NotNull(message = "El tipo de reporte es obligatorio")
    private String type; // punto_critico, incumplimiento_calendario

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull(message = "La categoría es obligatoria")
    private ReportCategory category;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    @NotNull(message = "La descripción es obligatoria")
    private String description;

    @Column(name = "latitud", nullable = false)
    @NotNull(message = "La latitud es obligatoria (HU10)")
    private Double latitude;

    @Column(name = "longitud", nullable = false)
    @NotNull(message = "La longitud es obligatoria (HU10)")
    private Double longitude;

    @Column(name = "imagen_url")
    private String imageUrl; // URL de la foto almacenada (obligatoria para HU10)

    @Column(name = "calendario_id")
    private Integer calendarId; // Obligatorio solo para tipo = incumplimiento_calendario (HU15)

    @Column(name = "estado", nullable = false)
    private String status; // pendiente, en_revision, resuelto, rechazado

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user; // Quién reportó (relacionado por documentNumber)

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "resuelto_at")
    private Date resolvedAt; // Cuándo fue resuelto

    @PrePersist
    protected void onCreate() {
        createdAt = new Date(System.currentTimeMillis());
        updatedAt = new Date(System.currentTimeMillis());
        if (status == null) {
            status = "pendiente";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date(System.currentTimeMillis());
    }
}

