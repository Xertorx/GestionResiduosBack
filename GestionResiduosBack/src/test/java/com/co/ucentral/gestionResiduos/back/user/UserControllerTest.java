package com.co.ucentral.gestionResiduos.back.user;

import com.co.ucentral.gestionResiduos.back.user.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserListResponse buildUserList(int doc) {
        return UserListResponse.builder()
                .documentNumber(doc).names("Juan").lastName("Pérez")
                .photo(null).status("VERIFICADO").email("juan@test.com")
                .build();
    }

    private UserProfileResponse buildProfile(int doc) {
        return UserProfileResponse.builder()
                .documentNumber(doc).names("Juan").lastName("Pérez")
                .email("juan@test.com").status("VERIFICADO")
                .createdAt(Date.valueOf("2026-01-01"))
                .build();
    }

    // ── GET /api/users/admin/list ─────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMINISTRADOR")
    void getAllUsersForAdmin_admin_retorna200() throws Exception {
        when(userService.getAllUsersForAdmin("admin@test.com"))
                .thenReturn(List.of(buildUserList(123456), buildUserList(654321)));

        mockMvc.perform(get("/api/users/admin/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].names").value("Juan"));
    }

    @Test
    void getAllUsersForAdmin_sinAutenticar_retorna401() throws Exception {
        mockMvc.perform(get("/api/users/admin/list"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/users/admin/{documentNumber} ────────────────────────

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMINISTRADOR")
    void getUserDetail_admin_retorna200() throws Exception {
        when(userService.getUserDetailForAdmin("admin@test.com", 123456)).thenReturn(buildProfile(123456));

        mockMvc.perform(get("/api/users/admin/123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value(123456));
    }

    // ── PATCH /api/users/admin/{documentNumber}/status ────────────────

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMINISTRADOR")
    void updateUserStatus_admin_retorna200() throws Exception {
        UserStatusUpdateRequest req = new UserStatusUpdateRequest();
        req.setStatus("INACTIVO");
        when(userService.updateUserStatus("admin@test.com", 123456, "INACTIVO")).thenReturn(buildProfile(123456));

        mockMvc.perform(patch("/api/users/admin/123456/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value(123456));
    }

    // ── GET /api/users/profile ────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void getProfile_autenticado_retorna200() throws Exception {
        when(userService.getProfile("user@test.com")).thenReturn(buildProfile(123456));

        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    void getProfile_sinAutenticar_retorna401() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/users/profile ────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    void updateProfile_autenticado_retorna200() throws Exception {
        when(userService.updateProfile(eq("user@test.com"), any(), any())).thenReturn(buildProfile(123456));

        MockMultipartFile photo = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "data".getBytes());
        MockMultipartFile names = new MockMultipartFile("names", "", "text/plain", "Juan".getBytes());

        mockMvc.perform(multipart("/api/users/profile")
                        .file(photo).file(names)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}

