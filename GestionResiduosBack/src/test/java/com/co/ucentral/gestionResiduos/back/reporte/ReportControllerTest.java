package com.co.ucentral.gestionResiduos.back.reporte;

import com.co.ucentral.gestionResiduos.back.reporte.ReportController;
import com.co.ucentral.gestionResiduos.back.reporte.ReportDTO;
import com.co.ucentral.gestionResiduos.back.reporte.ReportService;
import com.co.ucentral.gestionResiduos.back.reporte.ReportStatisticsDTO;
import com.co.ucentral.gestionResiduos.back.reporte.ReportStatsDTO;
import com.co.ucentral.gestionResiduos.back.security.JwtService;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import com.co.ucentral.gestionResiduos.back.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(TestSecurityConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ReportDTO buildReport(Long id) {
        ReportDTO dto = new ReportDTO();
        dto.setId(id);
        dto.setType("punto_critico");
        dto.setDescription("Basura en la vía");
        dto.setStatus("pendiente");
        dto.setUserId(1L);
        dto.setUserName("Juan");
        dto.setCreatedAt(Date.valueOf("2026-01-01"));
        return dto;
    }

    // ── GET /api/reports ──────────────────────────────────────────────

    @Test
    @WithMockUser
    void getAllReports_retorna200ConLista() throws Exception {
        when(reportService.getAllReports()).thenReturn(List.of(buildReport(1L), buildReport(2L)));

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // ── GET /api/reports/my-reports ───────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void getMyReports_retorna200() throws Exception {
        when(reportService.getMyReports("user@test.com")).thenReturn(List.of(buildReport(1L)));

        mockMvc.perform(get("/api/reports/my-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void getMyReports_sinReportes_retornaListaVacia() throws Exception {
        when(reportService.getMyReports("user@test.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/my-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/reports/pending ──────────────────────────────────────

    @Test
    @WithMockUser
    void getPendingReports_retorna200() throws Exception {
        when(reportService.getPendingReports()).thenReturn(List.of(buildReport(1L)));

        mockMvc.perform(get("/api/reports/pending"))
                .andExpect(status().isOk());
    }

    // ── GET /api/reports/{id} ─────────────────────────────────────────

    @Test
    @WithMockUser
    void getReportById_existente_retorna200() throws Exception {
        when(reportService.getReportById(1L)).thenReturn(buildReport(1L));

        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("punto_critico"));
    }

    // ── GET /api/reports/type/{type} ──────────────────────────────────

    @Test
    @WithMockUser
    void getByType_retorna200() throws Exception {
        when(reportService.getReportsByType("punto_critico")).thenReturn(List.of(buildReport(1L)));

        mockMvc.perform(get("/api/reports/type/punto_critico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/reports/status/{status} ─────────────────────────────

    @Test
    @WithMockUser
    void getByStatus_retorna200() throws Exception {
        when(reportService.getReportsByStatus("pendiente")).thenReturn(List.of(buildReport(1L)));

        mockMvc.perform(get("/api/reports/status/pendiente"))
                .andExpect(status().isOk());
    }

    // ── PATCH /api/reports/{id}/status ────────────────────────────────

    @Test
    @WithMockUser
    void changeStatus_valido_retorna200() throws Exception {
        ReportDTO updated = buildReport(1L);
        updated.setStatus("resuelto");
        when(reportService.changeStatus(1L, "resuelto")).thenReturn(updated);

        mockMvc.perform(patch("/api/reports/1/status")
                        .param("newStatus", "resuelto")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("resuelto"));
    }

    // ── GET /api/reports/search ───────────────────────────────────────

    @Test
    @WithMockUser
    void searchReports_sinFiltros_retorna200ConPagina() throws Exception {
        PageImpl<ReportDTO> page = new PageImpl<>(List.of(buildReport(1L)), PageRequest.of(0, 10), 1);
        when(reportService.searchReports(isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/reports/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser
    void searchReports_conFiltroStatus_retorna200() throws Exception {
        PageImpl<ReportDTO> page = new PageImpl<>(List.of(buildReport(1L)), PageRequest.of(0, 10), 1);
        when(reportService.searchReports(eq("pendiente"), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/reports/search").param("status", "pendiente"))
                .andExpect(status().isOk());
    }

    // ── GET /api/reports/statistics ───────────────────────────────────

    @Test
    @WithMockUser
    void getStatistics_retorna200() throws Exception {
        ReportStatisticsDTO stats = new ReportStatisticsDTO();
        when(reportService.getStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/reports/statistics"))
                .andExpect(status().isOk());
    }

    // ── GET /api/reports/stats (ADMIN) ────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void getFilteredStats_adminValido_retorna200() throws Exception {
        ReportStatsDTO stats = new ReportStatsDTO();
        when(reportService.getStats(any(), any(), isNull())).thenReturn(stats);

        mockMvc.perform(get("/api/reports/stats")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CIUDADANO")
    void getFilteredStats_sinRolAdmin_retorna403() throws Exception {
        mockMvc.perform(get("/api/reports/stats")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().isForbidden());
    }
}

