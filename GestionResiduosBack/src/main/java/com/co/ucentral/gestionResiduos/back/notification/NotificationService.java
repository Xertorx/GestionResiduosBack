package com.co.ucentral.gestionResiduos.back.notification;

import com.co.ucentral.gestionResiduos.back.Geography.District.District;
import com.co.ucentral.gestionResiduos.back.Geography.District.DistrictRepository;
import com.co.ucentral.gestionResiduos.back.exception.ResourceNotFoundException;
import com.co.ucentral.gestionResiduos.back.notification.dto.*;
import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
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

