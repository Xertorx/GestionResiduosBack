package com.co.ucentral.gestionResiduos.back.notification;

import com.co.ucentral.gestionResiduos.back.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Historial de notificaciones enviadas.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Tipo: COLLECTION, CAMPAIGN */
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Estado de envío: SENT, FAILED, PENDING */
    @Column(name = "status", nullable = false)
    private String status;

    /** Canal usado: EMAIL, WHATSAPP, TELEGRAM */
    @Column(name = "channel")
    private String channel;

    /** Número de intentos de envío */
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "sent_at")
    private Timestamp sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        if (status == null) status = "PENDING";
    }
}

