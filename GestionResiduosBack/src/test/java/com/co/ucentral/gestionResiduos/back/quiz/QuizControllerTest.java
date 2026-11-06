package com.co.ucentral.gestionResiduos.back.quiz;

import com.co.ucentral.gestionResiduos.back.quiz.dto.*;
import com.co.ucentral.gestionResiduos.back.security.JwtService;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuizController.class)
@Import(TestSecurityConfig.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private QuizAdminResponseDTO buildAdminQuiz(Long id) {
        return QuizAdminResponseDTO.builder()
                .id(id).title("Quiz reciclaje").description("Preguntas sobre reciclaje")
                .contentId(1L).pointsPerQuestion(10)
                .createdAt(LocalDateTime.now()).questions(List.of())
                .build();
    }

    private QuizResultDTO buildResult() {
        return QuizResultDTO.builder()
                .correctAnswers(3).totalQuestions(5)
                .pointsEarned(30).userTotalPoints(130)
                .firstAttempt(true).perQuestion(List.of()).newAchievements(List.of())
                .build();
    }

    // ── POST /api/v1/quizzes/content/{contentId} (ADMIN) ─────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void createQuiz_admin_retorna201() throws Exception {
        QuizRequestDTO req = new QuizRequestDTO();
        req.setTitle("Quiz reciclaje");
        req.setPointsPerQuestion(10);
        when(quizService.createForContent(eq(1L), any())).thenReturn(buildAdminQuiz(10L));

        mockMvc.perform(post("/api/v1/quizzes/content/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Quiz reciclaje"));
    }

    @Test
    @WithMockUser(roles = "CIUDADANO")
    void createQuiz_sinRolAdmin_retorna403() throws Exception {
        QuizRequestDTO req = new QuizRequestDTO();

        mockMvc.perform(post("/api/v1/quizzes/content/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/v1/quizzes/content/{contentId}/admin (ADMIN) ─────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void getAdminByContent_admin_retorna200() throws Exception {
        when(quizService.getAdminByContent(1L)).thenReturn(buildAdminQuiz(1L));

        mockMvc.perform(get("/api/v1/quizzes/content/1/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void getAdminByContent_noExistente_retorna404() throws Exception {
        when(quizService.getAdminByContent(99L)).thenThrow(new RuntimeException("Quiz no encontrado"));

        mockMvc.perform(get("/api/v1/quizzes/content/99/admin"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/v1/quizzes/content/{contentId} (usuario) ────────────

    @Test
    @WithMockUser
    void getPlayByContent_usuario_retorna200() throws Exception {
        QuizPlayResponseDTO play = QuizPlayResponseDTO.builder()
                .id(1L).title("Quiz").questions(List.of()).build();
        when(quizService.getPlayByContent(1L)).thenReturn(play);

        mockMvc.perform(get("/api/v1/quizzes/content/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ── GET /api/v1/quizzes/content/{contentId}/exists ────────────────

    @Test
    @WithMockUser
    void exists_conQuiz_retornaTrue() throws Exception {
        when(quizService.existsForContent(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/quizzes/content/1/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    @WithMockUser
    void exists_sinQuiz_retornaFalse() throws Exception {
        when(quizService.existsForContent(99L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/quizzes/content/99/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));
    }

    // ── PUT /api/v1/quizzes/{quizId} (ADMIN) ─────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void updateQuiz_admin_retorna200() throws Exception {
        QuizRequestDTO req = new QuizRequestDTO();
        req.setTitle("Quiz actualizado");
        when(quizService.update(eq(1L), any())).thenReturn(buildAdminQuiz(1L));

        mockMvc.perform(put("/api/v1/quizzes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/v1/quizzes/{quizId} (ADMIN) ──────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deleteQuiz_admin_retorna200() throws Exception {
        doNothing().when(quizService).delete(1L);

        mockMvc.perform(delete("/api/v1/quizzes/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Quiz eliminado correctamente"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deleteQuiz_noExistente_retorna404() throws Exception {
        doThrow(new RuntimeException("Quiz no encontrado")).when(quizService).delete(99L);

        mockMvc.perform(delete("/api/v1/quizzes/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/v1/quizzes/{quizId}/attempt ────────────────────────

    @Test
    void submitAttempt_autenticado_retorna200() throws Exception {
        SubmitAttemptDTO dto = new SubmitAttemptDTO();
        when(quizService.submitAttempt(eq(1L), anyString(), any())).thenReturn(buildResult());

        mockMvc.perform(post("/api/v1/quizzes/1/attempt")
                        .with(user("user@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctAnswers").value(3))
                .andExpect(jsonPath("$.pointsEarned").value(30));
    }

    @Test
    void submitAttempt_sinAutenticar_retorna401() throws Exception {
        mockMvc.perform(post("/api/v1/quizzes/1/attempt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/v1/quizzes/me/stats ─────────────────────────────────

    @Test
    void getMyStats_autenticado_retorna200() throws Exception {
        when(quizService.getUserStats(anyString())).thenReturn(Map.of("totalPoints", 130));

        mockMvc.perform(get("/api/v1/quizzes/me/stats")
                        .with(user("user@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(130));
    }
}

