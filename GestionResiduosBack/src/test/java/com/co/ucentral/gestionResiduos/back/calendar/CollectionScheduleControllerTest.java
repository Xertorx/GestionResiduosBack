package com.co.ucentral.gestionResiduos.back.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.co.ucentral.gestionResiduos.back.security.JwtService;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import com.co.ucentral.gestionResiduos.back.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollectionScheduleController.class)
@Import(TestSecurityConfig.class)
class CollectionScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CollectionScheduleService scheduleService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CollectionScheduleDTO buildSchedule(Long id) {
        return CollectionScheduleDTO.builder()
                .id(id).districtId(1).districtName("Chapinero")
                .residueType("ORGANICO").dayOfWeek("LUNES")
                .startTime("08:00").endTime("12:00")
                .description("Recolección orgánicos").status("ACTIVO")
                .build();
    }

    private CollectionScheduleCreateDTO buildCreateDTO() {
        CollectionScheduleCreateDTO dto = new CollectionScheduleCreateDTO();
        dto.setDistrictId(1);
        dto.setResidueType("ORGANICO");
        dto.setDayOfWeek("LUNES");
        dto.setStartTime("08:00");
        dto.setEndTime("12:00");
        dto.setDescription("Recolección orgánicos");
        return dto;
    }

    // ── GET /api/schedules/district/{id} ──────────────────────────────

    @Test
    void getByDistrict_publico_retorna200() throws Exception {
        when(scheduleService.getSchedulesByDistrict(1)).thenReturn(List.of(buildSchedule(1L)));

        mockMvc.perform(get("/api/schedules/district/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].districtId").value(1));
    }

    // ── GET /api/schedules/district/{id}/day/{day} ────────────────────

    @Test
    void getByDistrictAndDay_retorna200() throws Exception {
        when(scheduleService.getSchedulesByDistrictAndDay(1, "LUNES")).thenReturn(List.of(buildSchedule(1L)));

        mockMvc.perform(get("/api/schedules/district/1/day/LUNES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dayOfWeek").value("LUNES"));
    }

    // ── GET /api/schedules/district/{id}/residue-type/{type} ─────────

    @Test
    void getByDistrictAndResidueType_retorna200() throws Exception {
        when(scheduleService.getSchedulesByDistrictAndResidueType(1, "ORGANICO"))
                .thenReturn(List.of(buildSchedule(1L)));

        mockMvc.perform(get("/api/schedules/district/1/residue-type/ORGANICO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].residueType").value("ORGANICO"));
    }

    // ── GET /api/schedules/{id} ───────────────────────────────────────

    @Test
    void getById_existente_retorna200() throws Exception {
        when(scheduleService.getScheduleById(1L)).thenReturn(buildSchedule(1L));

        mockMvc.perform(get("/api/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ── GET /api/schedules (ADMIN) ────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void getAll_admin_retorna200() throws Exception {
        when(scheduleService.getAllSchedules()).thenReturn(List.of(buildSchedule(1L), buildSchedule(2L)));

        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "CIUDADANO")
    void getAll_sinRolAdmin_retorna403() throws Exception {
        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isForbidden());
    }

    // ── POST /api/schedules (ADMIN) ───────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void create_admin_retorna201() throws Exception {
        when(scheduleService.createSchedule(any())).thenReturn(buildSchedule(10L));

        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDTO()))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser(roles = "CIUDADANO")
    void create_sinRolAdmin_retorna403() throws Exception {
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDTO()))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/schedules/{id} (ADMIN) ──────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void update_admin_retorna200() throws Exception {
        CollectionScheduleDTO updated = buildSchedule(1L);
        updated.setDescription("Actualizado");
        when(scheduleService.updateSchedule(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDTO()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Actualizado"));
    }

    // ── PATCH /api/schedules/{id}/status (ADMIN) ─────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void changeStatus_admin_retorna200() throws Exception {
        CollectionScheduleDTO updated = buildSchedule(1L);
        updated.setStatus("INACTIVO");
        when(scheduleService.changeStatus(1L, "INACTIVO")).thenReturn(updated);

        mockMvc.perform(patch("/api/schedules/1/status")
                        .param("status", "INACTIVO")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVO"));
    }

    // ── DELETE /api/schedules/{id} (ADMIN) ───────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void delete_admin_retorna204() throws Exception {
        doNothing().when(scheduleService).deleteSchedule(1L);

        mockMvc.perform(delete("/api/schedules/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}

