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
public class CommentDTO {
    private Long id;
    private String texto;
    private String usuarioNombre;
    private String fechaCreacion;
    private List<ReplyDTO> respuestas;
}

