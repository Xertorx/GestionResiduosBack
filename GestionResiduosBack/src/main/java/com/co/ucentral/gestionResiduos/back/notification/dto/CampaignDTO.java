package com.co.ucentral.gestionResiduos.back.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignDTO {
    private Long id;
    private String title;
    private String message;
    private Integer districtId;
    private String districtName;
    private Date startDate;
    private Date endDate;
    private String status;
    private boolean notified;
    private Date createdAt;
}

