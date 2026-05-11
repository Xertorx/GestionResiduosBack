package com.co.ucentral.gestionResiduos.back.achievement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String condicion;
    private int puntosOtorgados;
    private String icono;
    private boolean unlocked;
    private String fechaObtenido;
}