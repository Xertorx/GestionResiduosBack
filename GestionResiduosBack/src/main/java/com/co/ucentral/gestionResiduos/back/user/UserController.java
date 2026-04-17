package com.co.ucentral.gestionResiduos.back.user;

import com.co.ucentral.gestionResiduos.back.user.dto.UserListResponse;
import com.co.ucentral.gestionResiduos.back.user.dto.UserProfileResponse;
import com.co.ucentral.gestionResiduos.back.user.dto.UserProfileUpdateRequest;
import com.co.ucentral.gestionResiduos.back.user.dto.UserStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ========== Endpoints de administrador ==========

    /**
     * GET /api/users/admin/list - Listar usuarios resumido (solo admin)
     */
    @GetMapping("/admin/list")
    public ResponseEntity<List<UserListResponse>> getAllUsersForAdmin(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getAllUsersForAdmin(email));
    }

    /**
     * GET /api/users/admin/{documentNumber} - Detalle de un usuario (solo admin)
     */
    @GetMapping("/admin/{documentNumber}")
    public ResponseEntity<UserProfileResponse> getUserDetail(
            Authentication authentication,
            @PathVariable int documentNumber) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getUserDetailForAdmin(email, documentNumber));
    }

    /**
     * PATCH /api/users/admin/{documentNumber}/status - Cambiar estado de usuario (solo admin)
     */
    @PatchMapping("/admin/{documentNumber}/status")
    public ResponseEntity<UserProfileResponse> updateUserStatus(
            Authentication authentication,
            @PathVariable int documentNumber,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateUserStatus(email, documentNumber, request.getStatus()));
    }

    // ========== Endpoints de usuario autenticado ==========

    /**
     * GET /api/users/profile - Obtener perfil del usuario autenticado
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getProfile(email));
    }

    /**
     * PUT /api/users/profile - Actualizar perfil del usuario autenticado
     */
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @ModelAttribute UserProfileUpdateRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateProfile(email, request, photo));
    }
}
