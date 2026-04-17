package com.co.ucentral.gestionResiduos.back.user;

import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood;
import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.NeighborhoodRepository;
import com.co.ucentral.gestionResiduos.back.exception.ProfileUpdateNotAllowedException;
import com.co.ucentral.gestionResiduos.back.exception.ResourceNotFoundException;
import com.co.ucentral.gestionResiduos.back.user.dto.UserListResponse;
import com.co.ucentral.gestionResiduos.back.user.dto.UserProfileResponse;
import com.co.ucentral.gestionResiduos.back.user.dto.UserProfileUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    @Value("${app.upload.dir.photo-profile:uploads/photo_profile}")
    private String uploadDir;

    public UserService(UserRepository userRepository, NeighborhoodRepository neighborhoodRepository) {
        this.userRepository = userRepository;
        this.neighborhoodRepository = neighborhoodRepository;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ========== Métodos para administrador ==========

    /**
     * Listar usuarios con datos resumidos (foto, nombres, apellidos, estado, email)
     */
    public List<UserListResponse> getAllUsersForAdmin(String adminEmail) {
        validateAdmin(adminEmail);
        return userRepository.findAll().stream()
                .map(user -> UserListResponse.builder()
                        .documentNumber(user.getDocumentNumber())
                        .names(user.getNames())
                        .lastName(user.getLastName())
                        .photo(user.getPhoto())
                        .status(user.getStatus())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Ver detalle completo de un usuario por documentNumber (admin)
     */
    public UserProfileResponse getUserDetailForAdmin(String adminEmail, int documentNumber) {
        validateAdmin(adminEmail);
        User user = userRepository.findById(documentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con documento: " + documentNumber));
        return buildProfileResponse(user);
    }

    /**
     * Cambiar estado de un usuario (VERIFICADO / INACTIVO) - solo admin
     */
    public UserProfileResponse updateUserStatus(String adminEmail, int documentNumber, String newStatus) {
        validateAdmin(adminEmail);
        User user = userRepository.findById(documentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con documento: " + documentNumber));

        user.setStatus(newStatus);
        user.setUpdatedAt(Date.valueOf(LocalDate.now()));
        userRepository.save(user);

        log.info("Admin {} cambió estado de usuario {} a {}", adminEmail, documentNumber, newStatus);
        return buildProfileResponse(user);
    }

    /**
     * Validar que el usuario que hace la petición sea ADMINISTRADOR
     */
    private void validateAdmin(String email) {
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (admin.getRole() == null || !"ADMINISTRADOR".equals(admin.getRole().getName())) {
            throw new AccessDeniedException("Solo los administradores pueden realizar esta acción");
        }
    }

    /**
     * Obtener perfil del usuario autenticado con indicador de si puede actualizar
     */
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        return buildProfileResponse(user);
    }

    /**
     * Construir respuesta de perfil desde entidad User
     */
    private UserProfileResponse buildProfileResponse(User user) {
        boolean canUpdate = canUserUpdate(user);
        String nextUpdate = null;
        if (!canUpdate && user.getUpdatedAt() != null) {
            LocalDate next = user.getUpdatedAt().toLocalDate().plusMonths(1);
            nextUpdate = next.toString();
        }

        return UserProfileResponse.builder()
                .documentNumber(user.getDocumentNumber())
                .names(user.getNames())
                .lastName(user.getLastName())
                .nickName(user.getNickName())
                .documentType(user.getDocumentType())
                .email(user.getEmail())
                .birthDate(user.getBirthDate())
                .neighborhoodName(user.getNeighborhoodId() != null ? user.getNeighborhoodId().getName() : null)
                .neighborhoodId(user.getNeighborhoodId() != null ? user.getNeighborhoodId().getNeighborhoodId() : null)
                .address(user.getAddress())
                .photo(user.getPhoto())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .canUpdate(canUpdate)
                .nextUpdateAvailable(nextUpdate)
                .build();
    }

    /**
     * Actualizar perfil del usuario autenticado (máximo 1 vez por mes)
     */
    public UserProfileResponse updateProfile(String email, UserProfileUpdateRequest request, MultipartFile photo) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        // Validar restricción mensual
        if (!canUserUpdate(user)) {
            LocalDate next = user.getUpdatedAt().toLocalDate().plusMonths(1);
            throw new ProfileUpdateNotAllowedException(
                    "Solo puedes actualizar tu perfil una vez al mes. Próxima actualización disponible: " + next);
        }

        // Actualizar campos
        user.setNames(request.getNames());
        user.setLastName(request.getLastName());

        if (request.getNickName() != null) {
            user.setNickName(request.getNickName());
        }

        // Validar cambio de email (no duplicado)
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new IllegalArgumentException("El correo " + request.getEmail() + " ya está en uso por otro usuario.");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getNeighborhoodId() != null) {
            Neighborhood neighborhood = neighborhoodRepository.findById(request.getNeighborhoodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barrio no encontrado con ID: " + request.getNeighborhoodId()));
            user.setNeighborhoodId(neighborhood);
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Manejar foto
        if (photo != null && !photo.isEmpty()) {
            String photoPath = saveProfilePhoto(photo);
            user.setPhoto(photoPath);
        }

        user.setUpdatedAt(Date.valueOf(LocalDate.now()));
        userRepository.save(user);

        log.info("Perfil actualizado para usuario: {}", email);

        return getProfile(user.getEmail());
    }

    /**
     * Verificar si el usuario puede actualizar (1 vez por mes)
     */
    private boolean canUserUpdate(User user) {
        if (user.getUpdatedAt() == null) {
            return true;
        }
        LocalDate lastUpdate = user.getUpdatedAt().toLocalDate();
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        return !lastUpdate.isAfter(oneMonthAgo);
    }

    /**
     * Guardar foto de perfil en el servidor
     */
    private String saveProfilePhoto(MultipartFile photo) {
        try {
            if (!photo.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("El archivo debe ser una imagen");
            }

            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(photo.getInputStream(), filePath);

            log.info("Foto de perfil guardada: {}", fileName);
            return "/uploads/photo_profile/" + fileName;
        } catch (IOException e) {
            log.error("Error al guardar foto de perfil: ", e);
            throw new IllegalStateException("Error al procesar la imagen. Intenta nuevamente.", e);
        }
    }
}
