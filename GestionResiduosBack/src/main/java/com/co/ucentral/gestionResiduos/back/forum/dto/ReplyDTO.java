package com.co.ucentral.gestionResiduos.back.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDTO {
    private Long id;
    private String texto;
    private String usuarioNombre;
    private String fechaCreacion;
}

