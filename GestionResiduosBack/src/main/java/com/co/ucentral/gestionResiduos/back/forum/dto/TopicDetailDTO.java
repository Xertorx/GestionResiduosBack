package com.co.ucentral.gestionResiduos.back.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicDetailDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String autorNombre;
    private String fechaCreacion;
    private List<CommentDTO> comentarios;
}

