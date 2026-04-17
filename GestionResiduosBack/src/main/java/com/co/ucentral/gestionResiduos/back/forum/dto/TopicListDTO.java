package com.co.ucentral.gestionResiduos.back.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicListDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String autorNombre;
    private String fechaCreacion;
    private int cantidadComentarios;
}

