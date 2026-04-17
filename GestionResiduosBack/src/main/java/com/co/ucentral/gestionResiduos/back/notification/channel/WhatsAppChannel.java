package com.co.ucentral.gestionResiduos.back.notification.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Canal de WhatsApp usando WhatsApp Business Cloud API (Meta).
 * Requiere configurar en application.properties:
 *   app.whatsapp.enabled=true
 *   app.whatsapp.api-url=https://graph.facebook.com/v18.0/{PHONE_NUMBER_ID}/messages
 *   app.whatsapp.token=Bearer {ACCESS_TOKEN}
 */
@Component
@Slf4j
public class WhatsAppChannel implements NotificationChannel {

    @Value("${app.whatsapp.enabled:false}")
    private boolean enabled;

    @Value("${app.whatsapp.api-url:}")
    private String apiUrl;

    @Value("${app.whatsapp.token:}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getChannelName() {
        return "WHATSAPP";
    }

    @Override
    public void send(String destination, String title, String message) throws Exception {
        if (!isAvailable()) {
            throw new IllegalStateException("Canal WhatsApp no está configurado");
        }

        // Formatear número: quitar espacios, guiones, agregar código de país si falta
        String phone = destination.replaceAll("[\\s\\-\\+]", "");
        if (!phone.startsWith("57")) {
            phone = "57" + phone; // Código Colombia
        }

        // Payload para WhatsApp Business API (mensaje de texto)
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", phone,
                "type", "text",
                "text", Map.of("body", title + "\n\n" + message)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("WhatsApp enviado a: {}", phone);
            } else {
                throw new RuntimeException("WhatsApp API respondió: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error enviando WhatsApp a {}: {}", phone, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled && apiUrl != null && !apiUrl.isBlank() && token != null && !token.isBlank();
    }
}

