package com.co.ucentral.gestionResiduos.back.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionScheduleDTO {
    private Long id;
    private Integer districtId;
    private String districtName;
    private String residueType;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String description;
    private String status;
}

