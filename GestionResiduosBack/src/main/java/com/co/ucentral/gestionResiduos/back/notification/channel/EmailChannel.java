package com.co.ucentral.gestionResiduos.back.notification.channel;

import com.co.ucentral.gestionResiduos.back.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailChannel implements NotificationChannel {

    private final EmailService emailService;

    @Override
    public String getChannelName() {
        return "EMAIL";
    }

    @Override
    public void send(String destination, String title, String message) throws Exception {
        emailService.enviarNotificacion(destination, title, message);
    }

    @Override
    public boolean isAvailable() {
        return true; // Email siempre disponible
    }
}

