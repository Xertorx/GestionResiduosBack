package com.co.ucentral.gestionResiduos.back.education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationSectionResponseDTO {
    private Long id;
    private String title;
    private String description;
    private List<EducationFileDTO> files;
}

