package com.co.ucentral.gestionResiduos.back.util;

import com.co.ucentral.gestionResiduos.back.exception.EmailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    private static final String HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>${titulo}</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <style>
    body { background: #f4f6f8; font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; }
    .container { max-width: 480px; margin: 40px auto; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px #0001; padding: 32px 24px; }
    .logo { display: block; margin: 0 auto 24px; width: 80px; }
    h1 { color: #2e7d32; font-size: 1.5rem; margin-bottom: 12px; }
    p { color: #444; font-size: 1rem; line-height: 1.6; }
    .button { display: block; width: fit-content; margin: 24px auto 0; background: #2e7d32 !important; color: #fff !important; text-decoration: none !important; padding: 12px 32px; border-radius: 6px; font-weight: 600; font-size: 1rem; border: none !important; }
    .footer { margin-top: 32px; text-align: center; color: #888; font-size: 0.9rem; }
  </style>
</head>
<body>
  <div class="container">
    <img class="logo" src="https://i.imgur.com/4M34hi2.png" alt="Logo Proyecto"/>
    <h1>¡Hola, ${nombre}!</h1>
    <p>
      Hemos recibido una solicitud para <b>${accion}</b> en tu cuenta.<br>
      Haz clic en el siguiente botón para continuar:
    </p>
    <a class="button" href="${enlace}" style="display:block;width:fit-content;margin:24px auto 0;background:#2e7d32 !important;color:#fff !important;text-decoration:none !important;padding:12px 32px;border-radius:6px;font-weight:600;font-size:1rem;border:none !important;">${textoBoton}</a>
    <p style="margin-top:24px;">
      Si no solicitaste esto, puedes ignorar este correo.<br>
      ¡Gracias por confiar en nuestro servicio!
    </p>
    <div class="footer">
      © ${año} Proyecto Gestión de Residuos
    </div>
  </div>
</body>
</html>
""";

    private String renderHtml(String nombre, String accion, String enlace, String textoBoton, String titulo) {
        String html = HTML_TEMPLATE
                .replace("${nombre}", nombre != null ? nombre : "Usuario")
                .replace("${accion}", accion)
                .replace("${enlace}", enlace)
                .replace("${textoBoton}", textoBoton)
                .replace("${año}", String.valueOf(java.time.Year.now().getValue()))
                .replace("${titulo}", titulo);
        return html;
    }

    private void enviarCorreoHtml(String destinatario, String asunto, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);
            mailSender.send(message);
            logger.info("Correo HTML enviado a: {} - Asunto: {}", destinatario, asunto);
        } catch (MessagingException e) {
            logger.error("Error al enviar correo HTML a: {}", destinatario, e);
            throw new EmailSendException("No se pudo enviar el correo", e);
        }
    }

    public void enviarConfirmacion(String destinatario, String nombre, String link) {
        String html = renderHtml(
                nombre,
                "activar tu cuenta",
                link,
                "Activar cuenta",
                "Activación de Cuenta"
        );
        enviarCorreoHtml(destinatario, "Confirma tu cuenta — Plataforma Gestión de Residuos", html);
    }

    public void enviarRecuperacion(String email, String nombre, String link) {
        String html = renderHtml(
                nombre,
                "restablecer tu contraseña",
                link,
                "Restablecer contraseña",
                "Recuperación de Contraseña"
        );
        enviarCorreoHtml(email, "Recuperación de contraseña - Proyecto Gestión de Residuos", html);
    }

    /**
     * Enviar notificación genérica (recolección, campañas, etc.)
     */
    public void enviarNotificacion(String destinatario, String asunto, String cuerpo) {
        // cuerpo puede ser HTML o texto plano, lo enviamos como HTML
        enviarCorreoHtml(destinatario, asunto, cuerpo);
    }
}