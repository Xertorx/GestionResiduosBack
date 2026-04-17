package com.co.ucentral.gestionResiduos.back.notification.channel;

import com.co.ucentral.gestionResiduos.back.notification.Notification;
import com.co.ucentral.gestionResiduos.back.notification.NotificationPreference;
import com.co.ucentral.gestionResiduos.back.notification.NotificationRepository;
import com.co.ucentral.gestionResiduos.back.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Dispatcher multicanal con fallback.
 * Envía notificación por los canales activos del usuario.
 * Si un canal falla, intenta el siguiente (fallback).
 * Orden de prioridad: WHATSAPP → TELEGRAM → EMAIL (email siempre como último recurso).
 */
@Component
@Slf4j
public class MultiChannelDispatcher {

    private final Map<String, NotificationChannel> channelMap;
    private final NotificationRepository notificationRepository;

    public MultiChannelDispatcher(List<NotificationChannel> channels, NotificationRepository notificationRepository) {
        this.channelMap = channels.stream()
                .collect(Collectors.toMap(NotificationChannel::getChannelName, Function.identity()));
        this.notificationRepository = notificationRepository;
        log.info("Canales de notificación registrados: {}", channelMap.keySet());
    }

    /**
     * Enviar notificación multicanal con fallback.
     * Intenta enviar por cada canal activo del usuario.
     * Registra un Notification por cada intento.
     */
    public void dispatch(User user, NotificationPreference pref, String type, String title, String message) {
        List<ChannelAttempt> attempts = buildChannelAttempts(user, pref);

        if (attempts.isEmpty()) {
            // Fallback: siempre intentar email si no hay canales configurados
            attempts.add(new ChannelAttempt("EMAIL", user.getEmail()));
        }

        boolean anySent = false;

        for (ChannelAttempt attempt : attempts) {
            NotificationChannel channel = channelMap.get(attempt.channelName);
            if (channel == null || !channel.isAvailable()) {
                log.warn("Canal {} no disponible, saltando", attempt.channelName);
                continue;
            }

            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setChannel(attempt.channelName);

            try {
                channel.send(attempt.destination, title, message);
                notification.setStatus("SENT");
                notification.setSentAt(new Timestamp(System.currentTimeMillis()));
                notificationRepository.save(notification);
                log.info("Notificación enviada por {} a {}", attempt.channelName, attempt.destination);
                anySent = true;
                break; // Éxito, no necesita fallback
            } catch (Exception e) {
                notification.setStatus("FAILED");
                notification.setRetryCount(1);
                notificationRepository.save(notification);
                log.warn("Fallo en canal {} para {}: {}. Intentando fallback...",
                        attempt.channelName, attempt.destination, e.getMessage());
            }
        }

        if (!anySent) {
            log.error("Todos los canales fallaron para usuario: {} ({})", user.getNames(), user.getEmail());
        }
    }

    /**
     * Construir lista ordenada de canales a intentar según preferencias.
     * Orden: WHATSAPP → TELEGRAM → EMAIL
     */
    private List<ChannelAttempt> buildChannelAttempts(User user, NotificationPreference pref) {
        List<ChannelAttempt> attempts = new ArrayList<>();

        // WhatsApp: usar número de whatsapp registrado o phoneNumber
        if (pref.isWhatsappEnabled()) {
            String whatsappNumber = pref.getWhatsappNumber() != null ? pref.getWhatsappNumber() : user.getPhoneNumber();
            if (whatsappNumber != null && !whatsappNumber.isBlank()) {
                attempts.add(new ChannelAttempt("WHATSAPP", whatsappNumber));
            }
        }

        // Telegram: usar chatId registrado
        if (pref.isTelegramEnabled()) {
            if (pref.getTelegramChatId() != null && !pref.getTelegramChatId().isBlank()) {
                attempts.add(new ChannelAttempt("TELEGRAM", pref.getTelegramChatId()));
            }
        }

        // Email: siempre como último recurso si está activo
        if (pref.isEmailEnabled()) {
            attempts.add(new ChannelAttempt("EMAIL", user.getEmail()));
        }

        return attempts;
    }

    /**
     * Reintento de una notificación fallida por un canal específico
     */
    public boolean retry(Notification notification) {
        NotificationChannel channel = channelMap.get(notification.getChannel());
        if (channel == null || !channel.isAvailable()) {
            return false;
        }

        try {
            // Determinar destino según canal
            User user = notification.getUser();
            String destination = switch (notification.getChannel()) {
                case "WHATSAPP" -> user.getPhoneNumber();
                case "TELEGRAM" -> null; // Necesitaría el chatId de preferencias
                default -> user.getEmail();
            };

            if (destination == null) return false;

            channel.send(destination, notification.getTitle(), notification.getMessage());
            notification.setStatus("SENT");
            notification.setSentAt(new Timestamp(System.currentTimeMillis()));
            notificationRepository.save(notification);
            return true;
        } catch (Exception e) {
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
            return false;
        }
    }

    private record ChannelAttempt(String channelName, String destination) {}
}

