package com.co.ucentral.gestionResiduos.back.notification;

import com.co.ucentral.gestionResiduos.back.calendar.CollectionSchedule;
import com.co.ucentral.gestionResiduos.back.calendar.CollectionScheduleRepository;
import com.co.ucentral.gestionResiduos.back.notification.channel.MultiChannelDispatcher;
import com.co.ucentral.gestionResiduos.back.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Tareas programadas para envío automático de notificaciones.
 *
 * Flujo de recolección:
 * 1. Cada día a las 18:00 se ejecuta el cron
 * 2. Calcula qué día es mañana → busca calendarios de ese día
 * 3. Para cada usuario con alertas activas, verifica si su localidad y tipo de residuo coinciden
 * 4. Envía email y registra en historial
 *
 * Flujo de campañas:
 * 1. Cada día a las 09:00 revisa campañas activas no notificadas
 * 2. Envía a usuarios que tengan alertas de campañas activas
 *
 * Reintentos:
 * 1. Cada hora revisa notificaciones FAILED con < 3 intentos y reintenta
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationPreferenceRepository prefRepository;
    private final NotificationRepository notificationRepository;
    private final CampaignRepository campaignRepository;
    private final CollectionScheduleRepository scheduleRepository;
    private final MultiChannelDispatcher dispatcher;

    private static final int MAX_RETRIES = 3;

    private static final Map<DayOfWeek, String> DAY_MAP = Map.of(
            DayOfWeek.MONDAY, "LUNES",
            DayOfWeek.TUESDAY, "MARTES",
            DayOfWeek.WEDNESDAY, "MIERCOLES",
            DayOfWeek.THURSDAY, "JUEVES",
            DayOfWeek.FRIDAY, "VIERNES",
            DayOfWeek.SATURDAY, "SABADO",
            DayOfWeek.SUNDAY, "DOMINGO"
    );

    /**
     * Alertas de recolección: todos los días a las 18:00
     * Notifica sobre la recolección de MAÑANA
     */
    @Scheduled(cron = "0 0 18 * * *")
    public void sendCollectionAlerts() {
        log.info("=== Ejecutando tarea: Alertas de recolección ===");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String tomorrowDay = DAY_MAP.get(tomorrow.getDayOfWeek());

        if (tomorrowDay == null) {
            log.warn("No se pudo mapear el día: {}", tomorrow.getDayOfWeek());
            return;
        }

        // Obtener preferencias activas con alertas de recolección
        List<NotificationPreference> prefs = prefRepository.findByEnabledTrue();
        int sent = 0;

        for (NotificationPreference pref : prefs) {
            if (!pref.isCollectionAlerts()) continue;

            User user = pref.getUser();
            if (user.getNeighborhoodId() == null) continue;

            int districtId = user.getNeighborhoodId().getDistrictId().getDistrictId();

            // Buscar calendarios de mañana para la localidad del usuario
            List<CollectionSchedule> schedules = scheduleRepository
                    .findByDistrictDistrictIdAndDayOfWeek(districtId, tomorrowDay);

            // Filtrar por tipo de residuo si el usuario tiene filtro
            if (pref.getResidueTypesFilter() != null && !pref.getResidueTypesFilter().isBlank()) {
                List<String> allowedTypes = Arrays.asList(pref.getResidueTypesFilter().split(","));
                schedules = schedules.stream()
                        .filter(s -> allowedTypes.contains(s.getResidueType()))
                        .filter(s -> "ACTIVO".equals(s.getStatus()))
                        .toList();
            } else {
                schedules = schedules.stream()
                        .filter(s -> "ACTIVO".equals(s.getStatus()))
                        .toList();
            }

            if (schedules.isEmpty()) continue;

            // Construir mensaje
            StringBuilder sb = new StringBuilder();
            sb.append("Hola ").append(user.getNames()).append(",\n\n");
            sb.append("Te recordamos que mañana (").append(tomorrowDay).append(") hay recolección en tu zona:\n\n");
            for (CollectionSchedule s : schedules) {
                sb.append("• ").append(s.getResidueType())
                        .append(" de ").append(s.getStartTime())
                        .append(" a ").append(s.getEndTime());
                if (s.getDescription() != null) sb.append(" - ").append(s.getDescription());
                sb.append("\n");
            }
            sb.append("\n¡No olvides sacar tus residuos!\nEquipo EcoBolivar");

            dispatcher.dispatch(user, pref, "COLLECTION",
                    "🗑️ Recolección mañana " + tomorrowDay,
                    sb.toString());
            sent++;
        }

        log.info("=== Alertas de recolección enviadas: {} ===", sent);
    }

    /**
     * Alertas de campañas: todos los días a las 09:00
     * Envía campañas activas que aún no se hayan notificado
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendCampaignAlerts() {
        log.info("=== Ejecutando tarea: Alertas de campañas ===");

        List<Campaign> campaigns = campaignRepository.findByStatusAndNotifiedFalse("ACTIVO");

        for (Campaign campaign : campaigns) {
            List<NotificationPreference> prefs = prefRepository.findByEnabledTrue();
            int sent = 0;

            for (NotificationPreference pref : prefs) {
                if (!pref.isCampaignAlerts()) continue;

                User user = pref.getUser();

                // Filtrar por localidad si la campaña tiene una específica
                if (campaign.getDistrictId() != null && user.getNeighborhoodId() != null) {
                    int userDistrictId = user.getNeighborhoodId().getDistrictId().getDistrictId();
                    if (userDistrictId != campaign.getDistrictId()) continue;
                }

                String msg = "Hola " + user.getNames() + ",\n\n"
                        + "📢 " + campaign.getTitle() + "\n\n"
                        + campaign.getMessage() + "\n\n"
                        + "Vigencia: " + campaign.getStartDate() + " al " + campaign.getEndDate() + "\n\n"
                        + "Equipo EcoBolivar";

                sendNotification(user, pref, "CAMPAIGN", "📢 " + campaign.getTitle(), msg);
                sent++;
            }

            // Marcar como notificada
            campaign.setNotified(true);
            campaignRepository.save(campaign);
            log.info("Campaña '{}' notificada a {} usuarios", campaign.getTitle(), sent);
        }
    }

    /**
     * Reintentos de envío: cada hora
     * Reintenta notificaciones fallidas con menos de MAX_RETRIES intentos
     */
    @Scheduled(cron = "0 0 * * * *")
    public void retryFailedNotifications() {
        List<Notification> failed = notificationRepository.findByStatus("FAILED");
        int retried = 0;

        for (Notification n : failed) {
            if (n.getRetryCount() >= MAX_RETRIES) continue;
            dispatcher.retry(n);
            retried++;
        }

        if (retried > 0) {
            log.info("Reintentos procesados: {}", retried);
        }
    }

    /**
     * Enviar notificación multicanal usando dispatcher
     */
    private void sendNotification(User user, NotificationPreference pref, String type, String title, String message) {
        dispatcher.dispatch(user, pref, type, title, message);
    }
}

