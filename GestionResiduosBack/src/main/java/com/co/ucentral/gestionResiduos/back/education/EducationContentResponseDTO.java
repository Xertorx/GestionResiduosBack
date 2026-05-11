package com.co.ucentral.gestionResiduos.back.education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationContentResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private LocalDateTime createdAt;
    private List<EducationFileDTO> files;
    private List<EducationSectionResponseDTO> sections;
}

