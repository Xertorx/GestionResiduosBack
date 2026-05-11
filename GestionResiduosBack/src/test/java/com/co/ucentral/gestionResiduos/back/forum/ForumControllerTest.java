package com.co.ucentral.gestionResiduos.back.forum;

import com.co.ucentral.gestionResiduos.back.forum.dto.*;
import com.co.ucentral.gestionResiduos.back.forum.entity.TopicStatus;
import com.co.ucentral.gestionResiduos.back.forum.service.ForumService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(ForumController.class)
class ForumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ForumService forumService;

    @Autowired
    private ObjectMapper objectMapper;

    private TopicListDTO buildTopicList(Long id) {
        return TopicListDTO.builder()
                .id(id).titulo("Tema de prueba").descripcion("Descripción")
                .autorNombre("Juan").fechaCreacion("2026-01-01T00:00:00").cantidadComentarios(0)
                .build();
    }

    private TopicDetailDTO buildTopicDetail(Long id) {
        return TopicDetailDTO.builder()
                .id(id).titulo("Tema de prueba").descripcion("Descripción")
                .autorNombre("Juan").fechaCreacion("2026-01-01T00:00:00").comentarios(List.of())
                .build();
    }

    private CommentDTO buildComment(Long id) {
        return CommentDTO.builder()
                .id(id).texto("Comentario de prueba").usuarioNombre("Juan")
                .fechaCreacion("2026-01-01T00:00:00").respuestas(List.of())
                .build();
    }

    private ReplyDTO buildReply(Long id) {
        return ReplyDTO.builder()
                .id(id).texto("Respuesta de prueba").usuarioNombre("Ana")
                .fechaCreacion("2026-01-01T00:00:00")
                .build();
    }

    // ── GET /api/forum/topics ─────────────────────────────────────────

    @Test
    void getActiveTopics_publico_retorna200() throws Exception {
        when(forumService.getActiveTopics()).thenReturn(List.of(buildTopicList(1L), buildTopicList(2L)));

        mockMvc.perform(get("/api/forum/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // ── GET /api/forum/topics/all (ADMIN) ─────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void getAllTopics_admin_retorna200() throws Exception {
        when(forumService.getAllTopics()).thenReturn(List.of(buildTopicList(1L)));

        mockMvc.perform(get("/api/forum/topics/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/forum/topics/{id} ────────────────────────────────────

    @Test
    @WithMockUser
    void getTopicById_existente_retorna200() throws Exception {
        when(forumService.getTopicById(1L)).thenReturn(buildTopicDetail(1L));

        mockMvc.perform(get("/api/forum/topics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Tema de prueba"));
    }

    // ── POST /api/forum/topics ────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void createTopic_autenticado_retorna201() throws Exception {
        CreateTopicRequest req = new CreateTopicRequest();
        req.setTitulo("Nuevo Tema");
        req.setDescripcion("Descripción del tema");
        when(forumService.createTopic(eq("user@test.com"), any())).thenReturn(buildTopicDetail(10L));

        mockMvc.perform(post("/api/forum/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void createTopic_sinAutenticar_retorna401() throws Exception {
        CreateTopicRequest req = new CreateTopicRequest();
        req.setTitulo("Tema");
        req.setDescripcion("Desc");

        mockMvc.perform(post("/api/forum/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ── DELETE /api/forum/topics/{id} ────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void deleteTopic_autorOAdmin_retorna204() throws Exception {
        doNothing().when(forumService).deleteTopic(1L, "user@test.com");

        mockMvc.perform(delete("/api/forum/topics/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ── PATCH /api/forum/topics/{id}/status ──────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void changeTopicStatus_admin_retorna200() throws Exception {
        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setEstado(TopicStatus.INACTIVO);
        when(forumService.changeTopicStatus(1L, TopicStatus.INACTIVO)).thenReturn(buildTopicList(1L));

        mockMvc.perform(patch("/api/forum/topics/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ── GET /api/forum/topics/{topicId}/comments ──────────────────────

    @Test
    @WithMockUser
    void getCommentsByTopic_retorna200() throws Exception {
        when(forumService.getCommentsByTopic(1L)).thenReturn(List.of(buildComment(1L)));

        mockMvc.perform(get("/api/forum/topics/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].texto").value("Comentario de prueba"));
    }

    // ── POST /api/forum/topics/{topicId}/comments ─────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void addComment_autenticado_retorna201() throws Exception {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setTexto("Mi comentario");
        when(forumService.addComment(eq(1L), eq("user@test.com"), any())).thenReturn(buildComment(5L));

        mockMvc.perform(post("/api/forum/topics/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    // ── POST /api/forum/comments/{commentId}/replies ──────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void addReply_autenticado_retorna201() throws Exception {
        CreateReplyRequest req = new CreateReplyRequest();
        req.setTexto("Mi respuesta");
        when(forumService.addReply(eq(1L), eq("user@test.com"), any())).thenReturn(buildReply(8L));

        mockMvc.perform(post("/api/forum/comments/1/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8));
    }
}

