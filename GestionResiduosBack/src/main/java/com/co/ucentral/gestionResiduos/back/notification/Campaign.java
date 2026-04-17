package com.co.ucentral.gestionResiduos.back.notification;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/**
 * Campañas ambientales que se envían como notificación a usuarios.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    @NotBlank(message = "El título es obligatorio")
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "El mensaje es obligatorio")
    private String message;

    /** Localidad objetivo (null = todas) */
    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    /** ACTIVO, INACTIVO, FINALIZADA */
    @Column(name = "status", nullable = false)
    private String status;

    /** Si ya se envió la notificación masiva */
    @Column(name = "notified", nullable = false)
    private boolean notified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date(System.currentTimeMillis());
        if (status == null) status = "ACTIVO";
    }
}

