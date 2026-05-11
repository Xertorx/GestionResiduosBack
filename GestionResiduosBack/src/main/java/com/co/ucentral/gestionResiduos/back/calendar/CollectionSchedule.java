package com.co.ucentral.gestionResiduos.back.calendar;

import com.co.ucentral.gestionResiduos.back.Geography.District.District;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/**
 * Entidad para calendario de recolección de residuos por localidad.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "collection_schedules")
public class CollectionSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "district_id", nullable = false)
    @NotNull(message = "La localidad es obligatoria")
    private District district;

    @Column(name = "residue_type", nullable = false)
    @NotBlank(message = "El tipo de residuo es obligatorio")
    private String residueType; // ORGANICO, RECICLABLE, ESPECIAL, RCD

    @Column(name = "day_of_week", nullable = false)
    @NotBlank(message = "El día de la semana es obligatorio")
    private String dayOfWeek; // LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO

    @Column(name = "start_time", nullable = false)
    @NotBlank(message = "La hora de inicio es obligatoria")
    private String startTime; // HH:mm

    @Column(name = "end_time", nullable = false)
    @NotBlank(message = "La hora de fin es obligatoria")
    private String endTime; // HH:mm

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVO, INACTIVO

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date(System.currentTimeMillis());
        updatedAt = new Date(System.currentTimeMillis());
        if (status == null) status = "ACTIVO";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date(System.currentTimeMillis());
    }
}

