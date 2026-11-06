package com.co.ucentral.gestionResiduos.back.user;

import com.co.ucentral.gestionResiduos.back.user.dto.*;
import com.co.ucentral.gestionResiduos.back.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import com.co.ucentral.gestionResiduos.back.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

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
    void getAllUsersForAdmin_admin_retorna200() throws Exception {
        when(userService.getAllUsersForAdmin(anyString()))
                .thenReturn(List.of(buildUserList(123456), buildUserList(654321)));

        mockMvc.perform(get("/api/users/admin/list")
                        .with(user("admin@test.com").roles("ADMINISTRADOR")))
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
    void getUserDetail_admin_retorna200() throws Exception {
        when(userService.getUserDetailForAdmin(anyString(), eq(123456))).thenReturn(buildProfile(123456));

        mockMvc.perform(get("/api/users/admin/123456")
                        .with(user("admin@test.com").roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value(123456));
    }

    // ── PATCH /api/users/admin/{documentNumber}/status ────────────────

    @Test
    void updateUserStatus_admin_retorna200() throws Exception {
        UserStatusUpdateRequest req = new UserStatusUpdateRequest();
        req.setStatus("INACTIVO");
        when(userService.updateUserStatus(anyString(), eq(123456), eq("INACTIVO"))).thenReturn(buildProfile(123456));

        mockMvc.perform(patch("/api/users/admin/123456/status")
                        .with(user("admin@test.com").roles("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value(123456));
    }

    // ── GET /api/users/profile ────────────────────────────────────────

    @Test
    void getProfile_autenticado_retorna200() throws Exception {
        when(userService.getProfile(anyString())).thenReturn(buildProfile(123456));

        mockMvc.perform(get("/api/users/profile")
                        .with(user("user@test.com")))
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
    void updateProfile_autenticado_retorna200() throws Exception {
        when(userService.updateProfile(anyString(), any(), any())).thenReturn(buildProfile(123456));

        MockMultipartFile photo = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/api/users/profile")
                        .file(photo)
                        .param("names", "Juan")
                        .param("lastName", "Loaiza")
                        .param("email", "user@test.com")
                        .with(user("user@test.com"))
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
