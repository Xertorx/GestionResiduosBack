package com.co.ucentral.gestionResiduos.back.notification;

import com.co.ucentral.gestionResiduos.back.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/**
 * Preferencias de notificación por usuario.
 * Define qué alertas quiere recibir y con qué frecuencia.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Recibir alertas de recolección (24h antes) */
    @Column(name = "collection_alerts", nullable = false)
    private boolean collectionAlerts = true;

    /** Recibir notificaciones de campañas ambientales */
    @Column(name = "campaign_alerts", nullable = false)
    private boolean campaignAlerts = true;

    /** Tipos de residuo de interés (CSV: ORGANICO,RECICLABLE,ESPECIAL,RCD) */
    @Column(name = "residue_types_filter")
    private String residueTypesFilter; // null = todos

    /** Hora preferida para recibir notificaciones (HH:mm), default 18:00 (día anterior) */
    @Column(name = "preferred_time", nullable = false)
    private String preferredTime = "18:00";

    /** Activo/Inactivo global */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    // ========== Canales ==========

    /** Canal Email activo (default true) */
    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    /** Canal WhatsApp activo */
    @Column(name = "whatsapp_enabled", nullable = false)
    private boolean whatsappEnabled = false;

    /** Número de WhatsApp (si es diferente al phoneNumber del usuario) */
    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    /** Canal Telegram activo */
    @Column(name = "telegram_enabled", nullable = false)
    private boolean telegramEnabled = false;

    /** Chat ID de Telegram (el usuario lo obtiene al hablar con el bot) */
    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date(System.currentTimeMillis());
        updatedAt = new Date(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date(System.currentTimeMillis());
    }
}

