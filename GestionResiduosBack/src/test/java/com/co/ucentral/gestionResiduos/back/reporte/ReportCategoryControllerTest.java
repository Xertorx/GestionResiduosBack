package com.co.ucentral.gestionResiduos.back.reporte.category;

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

@WebMvcTest(ReportCategoryController.class)
class ReportCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportCategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReportCategoryDTO buildCategory(Integer id) {
        return new ReportCategoryDTO(id, "Basura", "Residuos en vía pública", "ACTIVO");
    }

    // ── GET /api/report-categories ────────────────────────────────────

    @Test
    @WithMockUser
    void getAllCategories_retorna200ConLista() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(buildCategory(1), buildCategory(2)));

        mockMvc.perform(get("/api/report-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Basura"));
    }

    @Test
    @WithMockUser
    void getAllCategories_listaVacia_retorna200() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/report-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/report-categories/active ────────────────────────────

    @Test
    void getActiveCategories_sinToken_retorna200() throws Exception {
        when(categoryService.getActiveCategories()).thenReturn(List.of(buildCategory(1)));

        mockMvc.perform(get("/api/report-categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/report-categories/{id} ──────────────────────────────

    @Test
    @WithMockUser
    void getCategoryById_existente_retorna200() throws Exception {
        when(categoryService.getCategoryById(1)).thenReturn(buildCategory(1));

        mockMvc.perform(get("/api/report-categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Basura"));
    }

    // ── POST /api/report-categories ───────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void createCategory_datosValidos_retorna201() throws Exception {
        ReportCategoryDTO dto = new ReportCategoryDTO(null, "Nueva Categoría", "Desc", "ACTIVO");
        when(categoryService.createCategory(any())).thenReturn(buildCategory(10));

        mockMvc.perform(post("/api/report-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void createCategory_nombreVacio_retorna400() throws Exception {
        ReportCategoryDTO dto = new ReportCategoryDTO(null, "", "Desc", "ACTIVO");

        mockMvc.perform(post("/api/report-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/report-categories/{id} ──────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void updateCategory_existente_retorna200() throws Exception {
        ReportCategoryDTO updated = new ReportCategoryDTO(1, "Actualizada", "Nueva desc", "ACTIVO");
        when(categoryService.updateCategory(eq(1), any())).thenReturn(updated);

        mockMvc.perform(put("/api/report-categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Actualizada"));
    }

    // ── PATCH /api/report-categories/{id}/status ─────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void changeStatus_aInactivo_retorna200() throws Exception {
        ReportCategoryDTO inactivo = new ReportCategoryDTO(1, "Basura", "Desc", "INACTIVO");
        when(categoryService.changeStatus(1, "INACTIVO")).thenReturn(inactivo);

        mockMvc.perform(patch("/api/report-categories/1/status")
                        .param("status", "INACTIVO")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVO"));
    }

    // ── DELETE /api/report-categories/{id} ───────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deleteCategory_existente_retorna204() throws Exception {
        doNothing().when(categoryService).deleteCategory(1);

        mockMvc.perform(delete("/api/report-categories/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}

