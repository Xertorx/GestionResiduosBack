package com.co.ucentral.gestionResiduos.back.notification.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Canal de Telegram usando Telegram Bot API.
 * Requiere configurar en application.properties:
 *   app.telegram.enabled=true
 *   app.telegram.bot-token={BOT_TOKEN}
 *
 * El usuario debe registrar su chatId (obtenido al iniciar conversación con el bot).
 */
@Component
@Slf4j
public class TelegramChannel implements NotificationChannel {

    @Value("${app.telegram.enabled:false}")
    private boolean enabled;

    @Value("${app.telegram.bot-token:}")
    private String botToken;

    private static final String TELEGRAM_API = "https://api.telegram.org/bot%s/sendMessage";
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getChannelName() {
        return "TELEGRAM";
    }

    @Override
    public void send(String destination, String title, String message) throws Exception {
        if (!isAvailable()) {
            throw new IllegalStateException("Canal Telegram no está configurado");
        }

        String url = String.format(TELEGRAM_API, botToken);
        String fullMessage = "📢 *" + escapeMarkdown(title) + "*\n\n" + escapeMarkdown(message);

        Map<String, Object> payload = Map.of(
                "chat_id", destination,
                "text", fullMessage,
                "parse_mode", "MarkdownV2"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Telegram enviado a chatId: {}", destination);
            } else {
                throw new RuntimeException("Telegram API respondió: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error enviando Telegram a {}: {}", destination, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled && botToken != null && !botToken.isBlank();
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replaceAll("([_*\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }
}

