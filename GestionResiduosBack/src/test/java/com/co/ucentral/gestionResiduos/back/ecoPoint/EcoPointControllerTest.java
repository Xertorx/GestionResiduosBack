package com.co.ucentral.gestionResiduos.back.ecoPoint;

import com.co.ucentral.gestionResiduos.back.exception.ResourceNotFoundException;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EcoPointController.class)
@Import(TestSecurityConfig.class)
class EcoPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EcoPointService ecoPointService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private EcoPoint buildEcoPoint(Long id) {
        com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood n =
                new com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood();
        n.neighborhoodId = 1;
        n.setName("Barrio Test");

        EcoPoint ep = new EcoPoint();
        ep.setId(id);
        ep.setName("EcoPunto Test");
        ep.setAddress("Calle 1");
        ep.setLatitude(4.6097);
        ep.setLongitude(-74.0817);
        ep.setStatus("ACTIVO");
        ep.setNeighborhood(n);
        return ep;
    }

    // ── GET /api/ecopoints ────────────────────────────────────────────

    @Test
    @WithMockUser
    void getAllEcoPoints_retorna200ConLista() throws Exception {
        when(ecoPointService.getAllEcoPoints()).thenReturn(List.of(buildEcoPoint(1L), buildEcoPoint(2L)));

        mockMvc.perform(get("/api/ecopoints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // ── GET /api/ecopoints/active ─────────────────────────────────────

    @Test
    @WithMockUser
    void getActiveEcoPoints_retorna200() throws Exception {
        when(ecoPointService.getActiveEcoPoints()).thenReturn(List.of(buildEcoPoint(1L)));

        mockMvc.perform(get("/api/ecopoints/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/ecopoints/{id} ───────────────────────────────────────

    @Test
    @WithMockUser
    void getEcoPointById_existente_retorna200() throws Exception {
        when(ecoPointService.getEcoPointById(1L)).thenReturn(Optional.of(buildEcoPoint(1L)));

        mockMvc.perform(get("/api/ecopoints/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("EcoPunto Test"));
    }

    @Test
    @WithMockUser
    void getEcoPointById_noExistente_retorna404() throws Exception {
        when(ecoPointService.getEcoPointById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ecopoints/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/ecopoints/neighborhood/{id} ─────────────────────────

    @Test
    @WithMockUser
    void getByNeighborhood_retorna200() throws Exception {
        when(ecoPointService.getEcoPointsByNeighborhood(1)).thenReturn(List.of(buildEcoPoint(1L)));

        mockMvc.perform(get("/api/ecopoints/neighborhood/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/ecopoints/residue-type/{type} ───────────────────────

    @Test
    @WithMockUser
    void getByResidueType_retorna200() throws Exception {
        when(ecoPointService.getEcoPointsByResidueType("PLASTICO")).thenReturn(List.of(buildEcoPoint(1L)));

        mockMvc.perform(get("/api/ecopoints/residue-type/PLASTICO"))
                .andExpect(status().isOk());
    }

    // ── POST /api/ecopoints (ADMIN) ───────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void createEcoPoint_adminValido_retorna201() throws Exception {
        EcoPoint ep = buildEcoPoint(null);
        EcoPoint saved = buildEcoPoint(10L);
        when(ecoPointService.createEcoPoint(any())).thenReturn(saved);

        mockMvc.perform(post("/api/ecopoints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ep))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser(roles = "CIUDADANO")
    void createEcoPoint_sinRolAdmin_retorna403() throws Exception {
        mockMvc.perform(post("/api/ecopoints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEcoPoint(null)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createEcoPoint_sinAutenticar_retorna401() throws Exception {
        mockMvc.perform(post("/api/ecopoints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEcoPoint(null)))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/ecopoints/{id} (ADMIN) ──────────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void updateEcoPoint_existente_retorna200() throws Exception {
        EcoPoint updated = buildEcoPoint(1L);
        updated.setName("Actualizado");
        when(ecoPointService.updateEcoPoint(eq(1L), any())).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/ecopoints/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Actualizado"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void updateEcoPoint_noExistente_retorna404() throws Exception {
        when(ecoPointService.updateEcoPoint(eq(99L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/ecopoints/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEcoPoint(99L)))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/ecopoints/{id}/status (ADMIN) ─────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void changeStatus_existente_retorna200() throws Exception {
        EcoPoint updated = buildEcoPoint(1L);
        updated.setStatus("INACTIVO");
        when(ecoPointService.changeEcoPointStatus(1L, "INACTIVO")).thenReturn(Optional.of(updated));

        mockMvc.perform(patch("/api/ecopoints/1/status")
                        .param("status", "INACTIVO")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVO"));
    }

    // ── DELETE /api/ecopoints/{id} (ADMIN) ───────────────────────────

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deleteEcoPoint_existente_retorna204() throws Exception {
        when(ecoPointService.deleteEcoPoint(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/ecopoints/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deleteEcoPoint_noExistente_retorna404() throws Exception {
        when(ecoPointService.deleteEcoPoint(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/ecopoints/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}

