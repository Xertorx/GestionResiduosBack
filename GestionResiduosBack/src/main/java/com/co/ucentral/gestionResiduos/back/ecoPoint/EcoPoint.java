package com.co.ucentral.gestionResiduos.back.ecoPoint;

import java.sql.Date;
import java.util.List;

import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ecopoints")
public class EcoPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    @NotNull(message = "El nombre del EcoPoint es obligatorio")
    private String name;

    @Column(name = "address", nullable = false)
    @NotNull(message = "La dirección del EcoPoint es obligatoria")
    private String address;

    @Column(name = "latitude", nullable = false)
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
    private Double longitude;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @NotNull(message = "El estado del EcoPoint es obligatorio (ACTIVO o INACTIVO)")
    private String status; // ACTIVO, INACTIVO

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "ecopoint_residue_types", joinColumns = @JoinColumn(name = "ecopoint_id"))
    @Column(name = "residue_type")
    private List<String> residueTypes; // ORGANICO, RECICLABLE, ESPECIAL, RCD

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    @NotNull(message = "El barrio (neighborhoodId) es obligatorio")
    private Neighborhood neighborhood;

    @Column(name = "opening_time")
    private String openingTime; // HH:mm formato

    @Column(name = "closing_time")
    private String closingTime; // HH:mm formato

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date(System.currentTimeMillis());
        updatedAt = new Date(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date(System.currentTimeMillis());
    }
}

