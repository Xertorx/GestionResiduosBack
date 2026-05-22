package com.co.ucentral.gestionResiduos.back.education;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EducationContentController.class)
@Import(TestSecurityConfig.class)
class EducationContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EducationContentService service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Helpers ──────────────────────────────────────────────────────

    private EducationContentResponseDTO buildContentDTO(Long id) {
        return EducationContentResponseDTO.builder()
                .id(id)
                .title("Reciclaje Básico")
                .description("Guía de reciclaje")
                .category("reciclaje")
                .createdAt(LocalDateTime.now())
                .files(List.of(EducationFileDTO.builder()
                        .fileUrl("/uploads/education/test.jpg")
                        .fileType("IMAGE")
                        .build()))
                .sections(new ArrayList<>())
                .build();
    }

    private EducationSectionResponseDTO buildSectionDTO(Long id) {
        return EducationSectionResponseDTO.builder()
                .id(id)
                .title("Sección 1")
                .description("Descripción sección")
                .files(new ArrayList<>())
                .build();
    }

    // ── POST / (uploadContent) ────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void uploadContent_archivoValido_retorna201() throws Exception {
        // Arrange
        EducationContentResponseDTO saved = buildContentDTO(1L);
        when(service.saveContent(any(), any())).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile(
                "files", "foto.jpg", "image/jpeg", "data".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/education")
                        .file(file)
                        .param("title", "Reciclaje Básico")
                        .param("description", "Guía de reciclaje")
                        .param("category", "reciclaje")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Reciclaje Básico"))
                .andExpect(jsonPath("$.files[0].fileType").value("IMAGE"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void uploadContent_sinArchivos_retorna400() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/v1/education")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void uploadContent_sinRolAdmin_retorna403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "foto.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/education")
                        .file(file).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadContent_sinAutenticar_retorna401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "foto.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/education")
                        .file(file).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ── GET / (getAllContents) ─────────────────────────────────────────

    @Test
    @WithMockUser
    void getAllContents_retorna200ConLista() throws Exception {
        // Arrange
        List<EducationContentResponseDTO> list = List.of(buildContentDTO(1L), buildContentDTO(2L));
        when(service.getAllContents()).thenReturn(list);

        // Act & Assert
        mockMvc.perform(get("/api/v1/education"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser
    void getAllContents_listaVacia_retorna200ConArray() throws Exception {
        // Arrange
        when(service.getAllContents()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/v1/education"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /{id} (getContentById) ────────────────────────────────────

    @Test
    @WithMockUser
    void getContentById_existente_retorna200() throws Exception {
        // Arrange
        when(service.getContentById(1L)).thenReturn(Optional.of(buildContentDTO(1L)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/education/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Reciclaje Básico"))
                .andExpect(jsonPath("$.category").value("reciclaje"));
    }

    @Test
    @WithMockUser
    void getContentById_noExistente_retorna404() throws Exception {
        // Arrange
        when(service.getContentById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/education/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── GET /{id}/sections ────────────────────────────────────────────

    @Test
    @WithMockUser
    void getSections_retorna200ConSecciones() throws Exception {
        // Arrange
        List<EducationSectionResponseDTO> sections = List.of(buildSectionDTO(1L), buildSectionDTO(2L));
        when(service.getSectionsByContent(1L)).thenReturn(sections);

        // Act & Assert
        mockMvc.perform(get("/api/v1/education/1/sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Sección 1"));
    }

    // ── POST /{id}/sections (createSection) ───────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void createSection_valida_retorna201() throws Exception {
        // Arrange
        EducationSectionResponseDTO saved = buildSectionDTO(10L);
        when(service.addSection(anyLong(), any(), any())).thenReturn(saved);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/education/1/sections")
                        .param("title", "Sección 1")
                        .param("description", "Descripción")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Sección 1"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void createSection_contenidoNoExistente_retorna404() throws Exception {
        // Arrange
        when(service.addSection(anyLong(), any(), any()))
                .thenThrow(new RuntimeException("Contenido no encontrado con id: 99"));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/education/99/sections")
                        .param("title", "Sección").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── PUT /sections/{sectionId} (updateSection) ─────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void updateSection_existente_retorna200() throws Exception {
        // Arrange
        EducationSectionResponseDTO updated = EducationSectionResponseDTO.builder()
                .id(5L).title("Título Actualizado").description("Nueva desc").files(List.of()).build();
        when(service.updateSection(anyLong(), any())).thenReturn(updated);

        EducationSectionRequestDTO body = new EducationSectionRequestDTO();
        body.setTitle("Título Actualizado");
        body.setDescription("Nueva desc");

        // Act & Assert
        mockMvc.perform(put("/api/v1/education/sections/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Título Actualizado"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void updateSection_noExistente_retorna404() throws Exception {
        // Arrange
        when(service.updateSection(anyLong(), any()))
                .thenThrow(new RuntimeException("Sección no encontrada con id: 99"));

        EducationSectionRequestDTO body = new EducationSectionRequestDTO();
        body.setTitle("X");

        // Act & Assert
        mockMvc.perform(put("/api/v1/education/sections/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── DELETE /sections/{sectionId} ──────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deleteSection_existente_retorna200() throws Exception {
        // Arrange
        doNothing().when(service).deleteSection(5L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/education/sections/5").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sección eliminada correctamente"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deleteSection_noExistente_retorna404() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Sección no encontrada con id: 99"))
                .when(service).deleteSection(99L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/education/sections/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── PUT /{id} (updateContent) ─────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void updateContent_existente_retorna200() throws Exception {
        // Arrange
        EducationContentResponseDTO updated = buildContentDTO(1L);
        when(service.updateContent(anyLong(), any())).thenReturn(updated);

        EducationContentRequestDTO body = new EducationContentRequestDTO();
        body.setTitle("Reciclaje Básico");
        body.setDescription("Guía de reciclaje");
        body.setCategory("reciclaje");

        // Act & Assert
        mockMvc.perform(put("/api/v1/education/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Reciclaje Básico"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void updateContent_noExistente_retorna404() throws Exception {
        // Arrange
        when(service.updateContent(anyLong(), any()))
                .thenThrow(new RuntimeException("Contenido no encontrado con id: 99"));

        EducationContentRequestDTO body = new EducationContentRequestDTO();
        body.setTitle("X");

        // Act & Assert
        mockMvc.perform(put("/api/v1/education/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── DELETE /{id} (deleteContent) ──────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deleteContent_existente_retorna200() throws Exception {
        // Arrange
        doNothing().when(service).deleteContent(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/education/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contenido eliminado correctamente"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deleteContent_noExistente_retorna404() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Contenido no encontrado con id: 99"))
                .when(service).deleteContent(99L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/education/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
