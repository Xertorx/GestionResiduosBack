package com.co.ucentral.gestionResiduos.back.notification.channel;

/**
 * Interfaz Strategy para canales de notificación.
 * Cada implementación (Email, WhatsApp, Telegram) sabe cómo enviar mensajes por su canal.
 */
public interface NotificationChannel {

    /**
     * Nombre del canal: EMAIL, WHATSAPP, TELEGRAM
     */
    String getChannelName();

    /**
     * Enviar notificación por este canal.
     * @param destination dirección del destinatario (email, teléfono, chatId)
     * @param title título de la notificación
     * @param message cuerpo del mensaje
     * @throws Exception si el envío falla
     */
    void send(String destination, String title, String message) throws Exception;

    /**
     * Verificar si el canal está configurado/disponible
     */
    boolean isAvailable();
}

