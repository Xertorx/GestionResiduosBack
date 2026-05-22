package com.co.ucentral.gestionResiduos.back.auth;

import com.co.ucentral.gestionResiduos.back.auth.register.RegisterResponse;
import com.co.ucentral.gestionResiduos.back.auth.resetPassword.PasswordResetConfirmRequest;
import com.co.ucentral.gestionResiduos.back.auth.resetPassword.PasswordResetRequest;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterResponse buildRegisterResponse(String msg) {
        return new RegisterResponse(msg, null, null);
    }

    // ── POST /auth/login ──────────────────────────────────────────────

    @Test
    void login_credencialesValidas_retorna200ConToken() throws Exception {
        AuthResponse resp = new AuthResponse("access-token", "refresh-token", "user@test.com", "nick", null, "CIUDADANO");
        when(authService.login(any())).thenReturn(resp);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\",\"password\":\"pass123\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void login_bodyVacio_retorna400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ── POST /auth/register/user ──────────────────────────────────────

    @Test
    void register_datosValidos_retorna200() throws Exception {
        when(authService.register(any())).thenReturn(buildRegisterResponse("Registro exitoso. Verifica tu correo."));

        String body = "{\"documentNumber\":123456,\"names\":\"Juan\",\"lastName\":\"Perez\"," +
                "\"email\":\"juan@test.com\",\"birthDate\":\"2000-01-01\",\"neighborhoodId\":1," +
                "\"address\":\"Calle 1\",\"password\":\"pass123\",\"phoneNumber\":\"3001234567\"}";

        mockMvc.perform(post("/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registro exitoso. Verifica tu correo."));
    }

    // ── GET /auth/verify ─────────────────────────────────────────────

    @Test
    void verify_tokenValido_retorna200() throws Exception {
        AuthResponse resp = new AuthResponse("access-token", "refresh-token");
        when(authService.verify("token-abc")).thenReturn(resp);

        mockMvc.perform(get("/auth/verify").param("token", "token-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    // ── POST /auth/refresh ────────────────────────────────────────────

    @Test
    @WithMockUser
    void refresh_tokenValido_retornaNuevoAccessToken() throws Exception {
        AuthResponse resp = new AuthResponse("new-access", "refresh-token");
        when(authService.refresh(any())).thenReturn(resp);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"refresh-token\"")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    // ── POST /auth/forgot-password ────────────────────────────────────

    @Test
    void forgotPassword_emailExistente_retorna200() throws Exception {
        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmail("user@test.com");
        when(authService.requestPasswordReset("user@test.com")).thenReturn(buildRegisterResponse("Correo enviado"));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ── POST /auth/reset-password ─────────────────────────────────────

    @Test
    void resetPassword_tokenValido_retorna200() throws Exception {
        PasswordResetConfirmRequest req = new PasswordResetConfirmRequest();
        req.setToken("reset-token");
        req.setNewPassword("newPass123");
        when(authService.resetPassword(anyString(), anyString()))
                .thenReturn(buildRegisterResponse("Contrasena actualizada."));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ── POST /auth/login/google ───────────────────────────────────────

    @Test
    void loginGoogle_datosValidos_retorna200() throws Exception {
        AuthResponse resp = new AuthResponse("google-access", "google-refresh", "g@gmail.com", "gnick", null, "CIUDADANO");
        when(authService.loginGoogle(anyString(), anyString())).thenReturn(resp);

        Map<String, String> body = Map.of("email", "g@gmail.com", "googleId", "google-123");
        mockMvc.perform(post("/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("google-access"));
    }

    // ── POST /auth/resend-verification ───────────────────────────────

    @Test
    void resendVerification_retorna200() throws Exception {
        when(authService.resendVerificationEmail("user@test.com"))
                .thenReturn(buildRegisterResponse("Verificacion reenviada."));

        mockMvc.perform(post("/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\"}")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
