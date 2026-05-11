package com.co.ucentral.gestionResiduos.back.notification;

import com.co.ucentral.gestionResiduos.back.notification.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private CampaignDTO buildCampaign(Long id) {
        return CampaignDTO.builder()
                .id(id).title("Campaña test").message("Recicla más")
                .startDate(Date.valueOf("2026-01-01")).endDate(Date.valueOf("2026-12-31"))
                .status("ACTIVO").notified(false)
                .build();
    }

    // ── GET /api/notifications/preferences ───────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void getPreferences_autenticado_retorna200() throws Exception {
        NotificationPreferenceDTO pref = new NotificationPreferenceDTO();
        when(notificationService.getPreferences("user@test.com")).thenReturn(pref);

        mockMvc.perform(get("/api/notifications/preferences"))
                .andExpect(status().isOk());
    }

    @Test
    void getPreferences_sinAutenticar_retorna401() throws Exception {
        mockMvc.perform(get("/api/notifications/preferences"))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/notifications/preferences ───────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void updatePreferences_autenticado_retorna200() throws Exception {
        NotificationPreferenceDTO pref = new NotificationPreferenceDTO();
        when(notificationService.updatePreferences(eq("user@test.com"), any())).thenReturn(pref);

        mockMvc.perform(put("/api/notifications/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pref))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ── GET /api/notifications/history ───────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void getMyNotifications_retorna200() throws Exception {
        when(notificationService.getMyNotifications("user@test.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/notifications/campaigns/active ───────────────────────

    @Test
    void getActiveCampaigns_publico_retorna200() throws Exception {
        when(notificationService.getActiveCampaigns()).thenReturn(List.of(buildCampaign(1L)));

        mockMvc.perform(get("/api/notifications/campaigns/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Campaña test"));
    }

    // ── GET /api/notifications/campaigns (ADMIN) ──────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void getAllCampaigns_admin_retorna200() throws Exception {
        when(notificationService.getAllCampaigns()).thenReturn(List.of(buildCampaign(1L), buildCampaign(2L)));

        mockMvc.perform(get("/api/notifications/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "CIUDADANO")
    void getAllCampaigns_sinRolAdmin_retorna403() throws Exception {
        mockMvc.perform(get("/api/notifications/campaigns"))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/notifications/campaigns/{id} ─────────────────────────

    @Test
    @WithMockUser
    void getCampaignById_existente_retorna200() throws Exception {
        when(notificationService.getCampaignById(1L)).thenReturn(buildCampaign(1L));

        mockMvc.perform(get("/api/notifications/campaigns/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ── POST /api/notifications/campaigns (ADMIN) ─────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void createCampaign_admin_retorna201() throws Exception {
        CampaignCreateDTO dto = new CampaignCreateDTO();
        when(notificationService.createCampaign(any())).thenReturn(buildCampaign(10L));

        mockMvc.perform(post("/api/notifications/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    // ── PUT /api/notifications/campaigns/{id} (ADMIN) ─────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void updateCampaign_admin_retorna200() throws Exception {
        CampaignCreateDTO dto = new CampaignCreateDTO();
        when(notificationService.updateCampaign(eq(1L), any())).thenReturn(buildCampaign(1L));

        mockMvc.perform(put("/api/notifications/campaigns/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ── PATCH /api/notifications/campaigns/{id}/status (ADMIN) ────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void changeCampaignStatus_admin_retorna200() throws Exception {
        CampaignDTO updated = buildCampaign(1L);
        updated.setStatus("INACTIVO");
        when(notificationService.changeCampaignStatus(1L, "INACTIVO")).thenReturn(updated);

        mockMvc.perform(patch("/api/notifications/campaigns/1/status")
                        .param("status", "INACTIVO")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVO"));
    }

    // ── DELETE /api/notifications/campaigns/{id} (ADMIN) ──────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deleteCampaign_admin_retorna204() throws Exception {
        doNothing().when(notificationService).deleteCampaign(1L);

        mockMvc.perform(delete("/api/notifications/campaigns/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}

