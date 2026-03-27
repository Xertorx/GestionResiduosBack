package com.co.ucentral.gestionResiduos.back.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void enviarConfirmacion(String destinatario, String link) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Confirma tu cuenta — Plataforma Gestión de Residuos");
            mensaje.setText(
                    "Hola, haz clic en el siguiente enlace para confirmar tu cuenta.\n\n"
                            + link + "\n\n"
                            + "Este enlace expira en 5 minutos."
            );
            mailSender.send(mensaje);
            logger.info("Correo de confirmación enviado a: {}", destinatario);
        } catch (Exception e) {
            logger.error("Error al enviar correo de confirmación a: {}", destinatario, e);
            throw new RuntimeException("No se pudo enviar el correo de confirmación", e);
        }
    }
    public void enviarRecuperacion(String email, String link) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Recuperación de contraseña - EcoBolivar");
        mensaje.setText(
                "Hola,\n\n" +
                        "Recibimos una solicitud para restablecer tu contraseña.\n\n" +
                        "Haz clic en el siguiente enlace para crear una nueva contraseña:\n" +
                        link + "\n\n" +
                        "Este enlace expirará en 15 minutos.\n\n" +
                        "Si no solicitaste este cambio, ignora este correo.\n\n" +
                        "Equipo EcoBolivar"
        );
        mailSender.send(mensaje);
        logger.info("Correo de recuperación enviado a: {}", email);
    }
}