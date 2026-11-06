package com.co.ucentral.gestionResiduos.back.auth;

import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.NeighborhoodRepository;
import com.co.ucentral.gestionResiduos.back.auth.login.LoginRequest;
import com.co.ucentral.gestionResiduos.back.auth.register.GoogleRegisterRequest;
import com.co.ucentral.gestionResiduos.back.exception.UserInactiveException;
import com.co.ucentral.gestionResiduos.back.role.Role;
import com.co.ucentral.gestionResiduos.back.role.RoleRepository;
import com.co.ucentral.gestionResiduos.back.security.JwtService;
import com.co.ucentral.gestionResiduos.back.token.tokenRepository;
import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import com.co.ucentral.gestionResiduos.back.util.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private NeighborhoodRepository neighborhoodRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private tokenRepository tokenRepository;
    @Mock private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, roleRepository, neighborhoodRepository,
                passwordEncoder, jwtService, tokenRepository, emailService
        );
    }

    @Test
    void login_usuarioInactivo_noGeneraTokens() {
        User user = inactiveUser();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("user@test.com");
        when(request.getPassword()).thenReturn("password");

        assertThrows(UserInactiveException.class, () -> authService.login(request));
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginGoogle_usuarioInactivo_noGeneraTokens() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(inactiveUser()));

        assertThrows(UserInactiveException.class, () -> authService.loginGoogle("user@test.com", "google-id"));
        verifyNoInteractions(jwtService);
    }

    @Test
    void registerGoogle_usuarioExistenteInactivo_noGeneraTokens() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(inactiveUser()));

        assertThrows(UserInactiveException.class, () -> authService.registerGoogle(googleRequest()));
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any(User.class));
    }

    private User inactiveUser() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword("encoded-password");
        user.setGoogleId("google-id");
        user.setStatus("INACTIVO");
        user.setRole(new Role());
        return user;
    }

    private GoogleRegisterRequest googleRequest() {
        GoogleRegisterRequest request = new GoogleRegisterRequest();
        request.setEmail("user@test.com");
        request.setGoogleId("google-id");
        return request;
    }
}
