package com.co.ucentral.gestionResiduos.back.notification;

import com.co.ucentral.gestionResiduos.back.Geography.District.District;
import com.co.ucentral.gestionResiduos.back.Geography.District.DistrictRepository;
import com.co.ucentral.gestionResiduos.back.exception.ResourceNotFoundException;
import com.co.ucentral.gestionResiduos.back.notification.channel.MultiChannelDispatcher;
import com.co.ucentral.gestionResiduos.back.notification.dto.*;
import com.co.ucentral.gestionResiduos.back.reporte.Report;
import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationPreferenceRepository prefRepository;
    private final NotificationRepository notificationRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final MultiChannelDispatcher multiChannelDispatcher;

    // ========== Preferencias ==========

    public NotificationPreferenceDTO getPreferences(String email) {
        User user = findUser(email);
        NotificationPreference pref = prefRepository.findByUserDocumentNumber(user.getDocumentNumber())
                .orElseGet(() -> createDefaultPreferences(user));
        return toPreferenceDTO(pref);
    }

    public NotificationPreferenceDTO updatePreferences(String email, NotificationPreferenceDTO dto) {
        User user = findUser(email);
        NotificationPreference pref = prefRepository.findByUserDocumentNumber(user.getDocumentNumber())
                .orElseGet(() -> createDefaultPreferences(user));

        pref.setCollectionAlerts(dto.isCollectionAlerts());
        pref.setCampaignAlerts(dto.isCampaignAlerts());
        pref.setResidueTypesFilter(dto.getResidueTypesFilter());
        if (dto.getPreferredTime() != null) {
            pref.setPreferredTime(dto.getPreferredTime());
        }
        pref.setEnabled(dto.isEnabled());

        // Canales
        pref.setEmailEnabled(dto.isEmailEnabled());
        pref.setWhatsappEnabled(dto.isWhatsappEnabled());
        pref.setWhatsappNumber(dto.getWhatsappNumber());
        pref.setTelegramEnabled(dto.isTelegramEnabled());
        pref.setTelegramChatId(dto.getTelegramChatId());

        prefRepository.save(pref);
        log.info("Preferencias actualizadas para: {}", email);
        return toPreferenceDTO(pref);
    }

    private NotificationPreference createDefaultPreferences(User user) {
        NotificationPreference pref = new NotificationPreference();
        pref.setUser(user);
        pref.setCollectionAlerts(true);
        pref.setCampaignAlerts(true);
        pref.setPreferredTime("18:00");
        pref.setEnabled(true);
        pref.setEmailEnabled(true);
        pref.setWhatsappEnabled(false);
        pref.setTelegramEnabled(false);
        return prefRepository.save(pref);
    }

    // ========== Historial de notificaciones ==========

    public List<NotificationDTO> getMyNotifications(String email) {
        User user = findUser(email);
        return notificationRepository.findByUserDocumentNumberOrderByCreatedAtDesc(user.getDocumentNumber())
                .stream()
                .map(this::toNotificationDTO)
                .collect(Collectors.toList());
    }

    // ========== Campañas (admin) ==========

    public List<CampaignDTO> getAllCampaigns() {
        return campaignRepository.findAll().stream().map(this::toCampaignDTO).collect(Collectors.toList());
    }

    public List<CampaignDTO> getActiveCampaigns() {
        Date today = Date.valueOf(LocalDate.now());
        return campaignRepository.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual("ACTIVO", today, today)
                .stream().map(this::toCampaignDTO).collect(Collectors.toList());
    }

    public CampaignDTO getCampaignById(Long id) {
        return toCampaignDTO(campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada con ID: " + id)));
    }

    public CampaignDTO createCampaign(CampaignCreateDTO dto) {
        Campaign campaign = new Campaign();
        campaign.setTitle(dto.getTitle());
        campaign.setMessage(dto.getMessage());
        campaign.setDistrictId(dto.getDistrictId());
        campaign.setStartDate(dto.getStartDate());
        campaign.setEndDate(dto.getEndDate());
        campaign.setStatus("ACTIVO");
        campaign.setNotified(false);

        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaña creada: {} (ID: {})", saved.getTitle(), saved.getId());
        return toCampaignDTO(saved);
    }

    public CampaignDTO updateCampaign(Long id, CampaignCreateDTO dto) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada con ID: " + id));

        if (dto.getTitle() != null) campaign.setTitle(dto.getTitle());
        if (dto.getMessage() != null) campaign.setMessage(dto.getMessage());
        campaign.setDistrictId(dto.getDistrictId());
        if (dto.getStartDate() != null) campaign.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) campaign.setEndDate(dto.getEndDate());

        return toCampaignDTO(campaignRepository.save(campaign));
    }

    public CampaignDTO changeCampaignStatus(Long id, String status) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada con ID: " + id));
        campaign.setStatus(status);
        return toCampaignDTO(campaignRepository.save(campaign));
    }

    public void deleteCampaign(Long id) {
        if (!campaignRepository.existsById(id)) {
            throw new ResourceNotFoundException("Campaña no encontrada con ID: " + id);
        }
        campaignRepository.deleteById(id);
    }

    // ==========================================================
    // HU27: Notificación al ciudadano por cambio de estado
    // ==========================================================

    /**
     * Envía notificación al ciudadano cuando su reporte cambia de estado.
     * Se dispara desde ReportService.changeStatus()
     *
     * @param report Reporte con el estado recién actualizado
     */
    public void notifyReportStatusChange(Report report) {
        if (report == null || report.getUser() == null) {
            log.warn("HU27: No se puede notificar, reporte o usuario nulo");
            return;
        }

        User user = report.getUser();
        log.info("HU27: Notificando cambio de estado a usuario {} (reporte #{})",
                user.getEmail(), report.getId());

        // 1. Obtener o crear preferencias del usuario
        NotificationPreference pref = prefRepository.findByUserDocumentNumber(user.getDocumentNumber())
                .orElseGet(() -> createDefaultPreferences(user));

        // 2. Verificar si las notificaciones están globalmente activas
        if (!pref.isEnabled()) {
            log.info("HU27: Usuario {} tiene notificaciones desactivadas. Se omite envío.", user.getEmail());
            return;
        }

        // 3. Construir título y cuerpo HTML del correo
        String statusLabel = humanizeStatus(report.getStatus());
        String title = "Actualización de tu reporte #" + report.getId() + " - " + statusLabel;
        String htmlBody = buildStatusChangeEmail(user, report, statusLabel);

        // 4. Enviar a través del dispatcher multicanal (con fallback)
        try {
            multiChannelDispatcher.dispatch(user, pref, "REPORT_STATUS_CHANGE", title, htmlBody);
            log.info("HU27: Notificación de cambio de estado enviada al reporte #{}", report.getId());
        } catch (Exception e) {
            log.error("HU27: Error enviando notificación para reporte #{}: {}",
                    report.getId(), e.getMessage());
            // No relanzamos la excepción: el cambio de estado debe completarse aunque falle el envío
        }
    }

    /**
     * Traduce el estado técnico a un texto legible para el ciudadano.
     */
    private String humanizeStatus(String status) {
        if (status == null) return "Sin estado";
        return switch (status) {
            case "pendiente" -> "Pendiente";
            case "en_revision" -> "En revisión";
            case "resuelto" -> "Resuelto";
            case "rechazado" -> "Rechazado";
            default -> status;
        };
    }

    /**
     * Devuelve el color hexadecimal asociado al estado (para el badge del correo).
     */
    private String colorForStatus(String status) {
        if (status == null) return "#6b7280";
        return switch (status) {
            case "pendiente" -> "#f59e0b";   // ámbar
            case "en_revision" -> "#3b82f6"; // azul
            case "resuelto" -> "#10b981";    // verde
            case "rechazado" -> "#ef4444";   // rojo
            default -> "#6b7280";
        };
    }

    /**
     * Construye el HTML del correo de cambio de estado.
     * Sigue el mismo estilo visual de EmailService (verde #2e7d32, contenedor con sombra).
     */
    private String buildStatusChangeEmail(User user, Report report, String statusLabel) {
        String statusColor = colorForStatus(report.getStatus());
        String fullName = (user.getNames() != null ? user.getNames() : "")
                + (user.getLastName() != null ? " " + user.getLastName() : "");
        if (fullName.isBlank()) fullName = "Ciudadano";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm");
        String updatedAtFormatted = report.getUpdatedAt() != null
                ? sdf.format(report.getUpdatedAt())
                : sdf.format(new java.util.Date());

        String reportTypeLabel = "punto_critico".equals(report.getType())
                ? "Punto crítico"
                : "Incumplimiento de calendario";

        int year = java.time.Year.now().getValue();

        String html = "<!DOCTYPE html>"
                + "<html lang='es'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<title>Actualización de tu reporte</title>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>"
                + "<style>"
                + "body { background: #f4f6f8; font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; }"
                + ".container { max-width: 520px; margin: 40px auto; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px #0001; padding: 32px 24px; }"
                + ".logo { display: block; margin: 0 auto 24px; width: 80px; }"
                + "h1 { color: #2e7d32; font-size: 1.5rem; margin-bottom: 12px; text-align: center; }"
                + "p { color: #444; font-size: 1rem; line-height: 1.6; }"
                + ".status-badge { display: inline-block; padding: 8px 20px; border-radius: 999px; color: #fff; font-weight: 600; font-size: 0.95rem; }"
                + ".info-box { background: #f9fafb; border-left: 4px solid #2e7d32; border-radius: 6px; padding: 16px 20px; margin: 20px 0; }"
                + ".info-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 0.95rem; }"
                + ".info-label { color: #6b7280; font-weight: 500; }"
                + ".info-value { color: #111827; font-weight: 600; }"
                + ".footer { margin-top: 32px; text-align: center; color: #888; font-size: 0.85rem; border-top: 1px solid #eee; padding-top: 16px; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<img class='logo' src='https://i.imgur.com/4M34hi2.png' alt='Gestión de Residuos'/>"
                + "<h1>¡Hola, " + fullName.trim() + "!</h1>"
                + "<p style='text-align:center;'>El estado de tu reporte ha sido actualizado. Te mantenemos informado:</p>"
                + "<div style='text-align:center; margin: 24px 0;'>"
                + "<span class='status-badge' style='background: " + statusColor + ";'>" + statusLabel + "</span>"
                + "</div>"
                + "<div class='info-box'>"
                + "<div class='info-row'><span class='info-label'>Reporte:</span><span class='info-value'>#" + report.getId() + "</span></div>"
                + "<div class='info-row'><span class='info-label'>Tipo:</span><span class='info-value'>" + reportTypeLabel + "</span></div>"
                + "<div class='info-row'><span class='info-label'>Nuevo estado:</span><span class='info-value' style='color: " + statusColor + ";'>" + statusLabel + "</span></div>"
                + "<div class='info-row'><span class='info-label'>Fecha de actualización:</span><span class='info-value'>" + updatedAtFormatted + "</span></div>"
                + "</div>"
                + "<p style='text-align:center; margin-top: 24px;'>Puedes revisar el detalle de tu reporte ingresando a tu cuenta en la plataforma.</p>"
                + "<div class='footer'>© " + year + " Proyecto Gestión de Residuos<br>Este es un mensaje automático, por favor no respondas a este correo.</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        return html;
    }

    // ========== Helpers ==========

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private NotificationPreferenceDTO toPreferenceDTO(NotificationPreference pref) {
        return NotificationPreferenceDTO.builder()
                .id(pref.getId())
                .collectionAlerts(pref.isCollectionAlerts())
                .campaignAlerts(pref.isCampaignAlerts())
                .residueTypesFilter(pref.getResidueTypesFilter())
                .preferredTime(pref.getPreferredTime())
                .enabled(pref.isEnabled())
                .emailEnabled(pref.isEmailEnabled())
                .whatsappEnabled(pref.isWhatsappEnabled())
                .whatsappNumber(pref.getWhatsappNumber())
                .telegramEnabled(pref.isTelegramEnabled())
                .telegramChatId(pref.getTelegramChatId())
                .build();
    }

    private NotificationDTO toNotificationDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .channel(n.getChannel())
                .status(n.getStatus())
                .sentAt(n.getSentAt())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private CampaignDTO toCampaignDTO(Campaign c) {
        String districtName = null;
        if (c.getDistrictId() != null) {
            districtName = districtRepository.findById(c.getDistrictId())
                    .map(District::getName).orElse(null);
        }
        return CampaignDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .message(c.getMessage())
                .districtId(c.getDistrictId())
                .districtName(districtName)
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .status(c.getStatus())
                .notified(c.isNotified())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
