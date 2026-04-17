package com.co.ucentral.gestionResiduos.back.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private String channel;
    private String status;
    private Timestamp sentAt;
    private Timestamp createdAt;
}

