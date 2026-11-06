package com.co.ucentral.gestionResiduos.back.achievement;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "logros")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_logro")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "condicion", nullable = false, unique = true)
    private String condicion;

    @Column(name = "puntos_otorgados")
    private int puntosOtorgados;

    @Column(name = "icono")
    private String icono;
}