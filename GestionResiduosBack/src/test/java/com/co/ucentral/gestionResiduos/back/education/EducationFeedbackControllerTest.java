package com.co.ucentral.gestionResiduos.back.education;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EducationFeedbackController.class)
class EducationFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EducationFeedbackService feedbackService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── POST /api/v1/education/{id}/feedback ──────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void giveFeedback_autenticadoPrimerVez_retorna200ConMensaje() throws Exception {
        when(feedbackService.saveFeedback(1L, true, "user@test.com")).thenReturn(false);

        FeedbackRequestDTO dto = new FeedbackRequestDTO();
        dto.setUseful(true);

        mockMvc.perform(post("/api/v1/education/1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Feedback registrado"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void giveFeedback_usuarioYaVoto_retorna200ConMensajeYaRegistrado() throws Exception {
        when(feedbackService.saveFeedback(1L, true, "user@test.com")).thenReturn(true);

        FeedbackRequestDTO dto = new FeedbackRequestDTO();
        dto.setUseful(true);

        mockMvc.perform(post("/api/v1/education/1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ya se registró el feedback"));
    }

    @Test
    void giveFeedback_sinAutenticar_permiteFeedbackAnonimo() throws Exception {
        when(feedbackService.saveFeedback(eq(1L), eq(false), isNull())).thenReturn(false);

        FeedbackRequestDTO dto = new FeedbackRequestDTO();
        dto.setUseful(false);

        mockMvc.perform(post("/api/v1/education/1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Feedback registrado"));
    }

    // ── GET /api/v1/education/{id}/feedback/stats ─────────────────────

    @Test
    @WithMockUser
    void getStats_retorna200ConEstadisticas() throws Exception {
        Map<String, Object> stats = Map.of("useful", 10, "notUseful", 2, "total", 12);
        when(feedbackService.getFeedbackStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/education/1/feedback/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.useful").value(10))
                .andExpect(jsonPath("$.total").value(12));
    }

    @Test
    void getStats_publico_retorna200() throws Exception {
        when(feedbackService.getFeedbackStats(1L)).thenReturn(Map.of("total", 0));

        mockMvc.perform(get("/api/v1/education/1/feedback/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }
}

