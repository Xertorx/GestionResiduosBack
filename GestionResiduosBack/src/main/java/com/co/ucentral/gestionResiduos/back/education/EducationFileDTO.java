package com.co.ucentral.gestionResiduos.back.education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationFileDTO {
    private String fileUrl;
    private String fileType;
}

