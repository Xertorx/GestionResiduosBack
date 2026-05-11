package com.co.ucentral.gestionResiduos.back.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceDTO {
    private Long id;
    private boolean collectionAlerts;
    private boolean campaignAlerts;
    private String residueTypesFilter;
    private String preferredTime;
    private boolean enabled;

    // Canales
    private boolean emailEnabled;
    private boolean whatsappEnabled;
    private String whatsappNumber;
    private boolean telegramEnabled;
    private String telegramChatId;
}

