package com.co.ucentral.gestionResiduos.back.notification;

import com.co.ucentral.gestionResiduos.back.notification.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ========== Preferencias del usuario autenticado ==========

    /**
     * GET /api/notifications/preferences - Obtener mis preferencias
     */
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceDTO> getPreferences(Authentication auth) {
        return ResponseEntity.ok(notificationService.getPreferences(auth.getName()));
    }

    /**
     * PUT /api/notifications/preferences - Actualizar mis preferencias
     */
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceDTO> updatePreferences(
            Authentication auth,
            @Valid @RequestBody NotificationPreferenceDTO dto) {
        return ResponseEntity.ok(notificationService.updatePreferences(auth.getName(), dto));
    }

    // ========== Historial del usuario autenticado ==========

    /**
     * GET /api/notifications/history - Mi historial de notificaciones
     */
    @GetMapping("/history")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(Authentication auth) {
        return ResponseEntity.ok(notificationService.getMyNotifications(auth.getName()));
    }

    // ========== Campañas (público GET, admin CRUD) ==========

    /**
     * GET /api/notifications/campaigns/active - Campañas activas (público)
     */
    @GetMapping("/campaigns/active")
    public ResponseEntity<List<CampaignDTO>> getActiveCampaigns() {
        return ResponseEntity.ok(notificationService.getActiveCampaigns());
    }

    /**
     * GET /api/notifications/campaigns - Todas las campañas (admin)
     */
    @GetMapping("/campaigns")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<CampaignDTO>> getAllCampaigns() {
        return ResponseEntity.ok(notificationService.getAllCampaigns());
    }

    /**
     * GET /api/notifications/campaigns/{id} - Detalle campaña
     */
    @GetMapping("/campaigns/{id}")
    public ResponseEntity<CampaignDTO> getCampaignById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getCampaignById(id));
    }

    /**
     * POST /api/notifications/campaigns - Crear campaña (admin)
     */
    @PostMapping("/campaigns")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CampaignDTO> createCampaign(@Valid @RequestBody CampaignCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createCampaign(dto));
    }

    /**
     * PUT /api/notifications/campaigns/{id} - Actualizar campaña (admin)
     */
    @PutMapping("/campaigns/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CampaignDTO> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignCreateDTO dto) {
        return ResponseEntity.ok(notificationService.updateCampaign(id, dto));
    }

    /**
     * PATCH /api/notifications/campaigns/{id}/status - Cambiar estado (admin)
     */
    @PatchMapping("/campaigns/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CampaignDTO> changeCampaignStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(notificationService.changeCampaignStatus(id, status));
    }

    /**
     * DELETE /api/notifications/campaigns/{id} - Eliminar campaña (admin)
     */
    @DeleteMapping("/campaigns/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        notificationService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }
}

