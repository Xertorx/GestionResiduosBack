package com.co.ucentral.gestionResiduos.back.auth;

import com.co.ucentral.gestionResiduos.back.auth.login.LoginRequest;
import com.co.ucentral.gestionResiduos.back.auth.register.GoogleRegisterRequest;
import com.co.ucentral.gestionResiduos.back.auth.register.RegisterRequest;
import com.co.ucentral.gestionResiduos.back.auth.register.RegisterResponse;
import com.co.ucentral.gestionResiduos.back.auth.register.UpdateProfileRequest;
import com.co.ucentral.gestionResiduos.back.role.RoleRepository;
import com.co.ucentral.gestionResiduos.back.exception.EmailAlreadyExistsException;
import com.co.ucentral.gestionResiduos.back.exception.InvalidCredentialsException;
import com.co.ucentral.gestionResiduos.back.exception.IdAlreadyExistsException;
import com.co.ucentral.gestionResiduos.back.exception.PhoneAlreadyExistsException;
import com.co.ucentral.gestionResiduos.back.exception.TokenInvalidoException;
import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.NeighborhoodRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import com.co.ucentral.gestionResiduos.back.security.JwtService;
import com.co.ucentral.gestionResiduos.back.role.Role;
import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood;
import com.co.ucentral.gestionResiduos.back.token.TokenSecurity;
import com.co.ucentral.gestionResiduos.back.token.tokenRepository;
import com.co.ucentral.gestionResiduos.back.util.EmailService;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final tokenRepository tokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       NeighborhoodRepository neighborhoodRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       tokenRepository tokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.neighborhoodRepository = neighborhoodRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        logger.info("Intentando registrar usuario con email: {}", request.getEmail());

        //Validaciones
        //Si el usuario ya se encuentra registrado pero se encuentra en estado pendiente, reenviar correo de verificación, esto para evitar que el usuario se quede bloqueado
        // por no recibir el correo o perderlo, se le da la oportunidad de recibir uno nuevo y verificar su cuenta, si el
        // usuario ya se encuentra registrado pero se encuentra en estado pendiente, se le reenvia el correo de verificación, esto
        // para evitar que el usuario se quede bloqueado por no recibir el correo o perderlo, se le da la oportunidad de recibir uno nuevo y verificar su cuenta
        // Caso 1: ID existe y está PENDIENTE → reenviar verificación y redirigir
        if (userRepository.findById(request.getDocumentNumber()).isPresent()) {
            User existing = userRepository.findById(request.getDocumentNumber()).get();

            if (existing.getStatus().equals("PENDIENTE")) {
                resendVerificationEmail(existing.getEmail());
                logger.info("ID en estado PENDIENTE, reenviando verificación: {}", request.getDocumentNumber());

                // ← devuelve 200 con un mensaje claro en lugar de lanzar excepción
                return new RegisterResponse(
                        "PENDIENTE",
                        existing.getEmail(),
                        "PENDIENTE"
                );
            }

            logger.warn("ID ya registrado: {}", request.getDocumentNumber());
            throw new IdAlreadyExistsException("El ID ya está registrado");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Email ya registrado: {}", request.getEmail());
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            logger.warn("Número de teléfono ya registrado: {}", request.getPhoneNumber());
            throw new PhoneAlreadyExistsException("El número de teléfono ya está registrado");
        }

        logger.info("Validaciones pasadas, procediendo con registro. Total usuarios en BD: {}", userRepository.count());

        Role role =  roleRepository.findByName("CIUDADANO")
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        
        Neighborhood neighborhood = neighborhoodRepository.findById(request.getNeighborhoodId())
                .orElseThrow(() -> new RuntimeException("Barrio no encontrado"));
        
        //Creacion de nuevo Usuario con la clase de JPA para ingreso a la BD, se asigna el rol de ciudadano por defecto y el estado de pendiente hasta que se verifique el correo

        User user = new User();
        user.setDocumentNumber(request.getDocumentNumber());
        user.setNames(request.getNames());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setBirthDate(request.getBirthDate());
        user.setNeighborhoodId(neighborhood);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setRole(role);
        user.setStatus("PENDIENTE");
        user.setCreatedAt(new Date(System.currentTimeMillis()));
        userRepository.save(user);
        /*
            Json:
            User {
                documentNumber: int
                names: string
                lastName: string
                documentType: string
                email: string
                birthDate: Date
                neighborhoodId: int
                address: string
                password: string
                status: string
                phoneNumber: string
                roleId: int
            }
         */

        String token = UUID.randomUUID().toString();

        TokenSecurity ts = new TokenSecurity();
        ts.setUser(user);
        ts.setToken(token);
        ts.setExpiredIn(LocalDateTime.now().plusMinutes(15).toString());
        ts.setUsage(false);
        tokenRepository.save(ts);

        logger.info("Token de confirmación creado para usuario: {}", user.getEmail());

        // Enviar correo con el link de verificación
        String link = "http://localhost:4200/register/verify?token=" + token;
        try {
            emailService.enviarConfirmacion(user.getEmail(), link);
            logger.info("Correo de confirmación enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Error al enviar correo de confirmación a: {}, pero el usuario se registró correctamente", user.getEmail(), e);
            // No lanzamos excepción aquí para no bloquear el registro si falla el email
        }

        logger.info("Usuario registrado exitosamente. Pendiente de verificación de email: {}", user.getEmail());

        // NO generar tokens en el registro, solo cuando se verifique la cuenta
        return new RegisterResponse(
                "Registro exitoso. Por favor verifica tu correo electrónico para activar tu cuenta.",
                user.getEmail(),
                "PENDIENTE"
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getNickName(),
                user.getPhoto(),
                user.getRole().getName()
        );
    }


    public AuthResponse refresh(String refreshToken) {

        String email = jwtService.extractUsername(refreshToken);

        String newAccessToken = jwtService.generateToken(email);

        return new AuthResponse(newAccessToken, refreshToken);
    }

    public AuthResponse verify(String token) {
        logger.info("Iniciando verificación de token: {}", token);

        TokenSecurity ts = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    logger.warn("Token no encontrado: {}", token);
                    return new TokenInvalidoException("Token no existe");
                });

        // Validación 1: ¿ya fue usado?
        if (ts.isUsage()) {
            logger.warn("Token ya fue utilizado: {}", token);
            throw new TokenInvalidoException("Este enlace ya fue utilizado.");
        }

        // Validación 2: ¿expiró?
        LocalDateTime expiracionTime = LocalDateTime.parse(ts.getExpiredIn());
        if (expiracionTime.isBefore(LocalDateTime.now())) {
            logger.warn("Token expirado: {}", token);
            throw new TokenInvalidoException("El enlace expiró. Solicita uno nuevo.");
        }

        // Obtener el usuario y activarlo
        User user = ts.getUser();
        if (user == null) {
            logger.error("Usuario no encontrado para token: {}", token);
            throw new RuntimeException("Usuario no encontrado");
        }

        user.setStatus("ACTIVO");
        userRepository.save(user);
        logger.info("Usuario activado: {}", user.getEmail());

        // Marcar el token como usado
        ts.setUsage(true);
        tokenRepository.save(ts);
        logger.info("Token marcado como usado: {}", token);

        // Generar JWT para que el front inicie sesión directo
        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }

    /**

     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RegisterResponse resendVerificationEmail(String email) {
        logger.info("Resolicitud de verificación para email: {}", email);

        // Verificar que el usuario existe
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado para email: {}", email);
                    return new RuntimeException("Usuario no encontrado");
                });

        // Si el usuario ya está activo, no necesita verificación
        if ("ACTIVO".equals(user.getStatus())) {
            logger.warn("Usuario ya está activo: {}", email);
            throw new RuntimeException("Tu cuenta ya ha sido verificada. Por favor inicia sesión.");
        }

        // Eliminar token anterior si existe
        tokenRepository.deleteByUser(user);

        // Crear nuevo token
        String newToken = UUID.randomUUID().toString();
        TokenSecurity ts = new TokenSecurity();
        ts.setUser(user);
        ts.setToken(newToken);
        ts.setExpiredIn(LocalDateTime.now().plusMinutes(15).toString());
        ts.setUsage(false);
        tokenRepository.save(ts);
        logger.info("Correo de verificación reenviado a: {}", ts.getToken());

        logger.info("Nuevo token de verificación creado para: {}", email);

        // Enviar correo con el nuevo link
        String link = "http://localhost:4200/register/verify?token=" + newToken;
        try {
            emailService.enviarConfirmacion(email, link);
            logger.info("Correo de verificación reenviado a: {}", email);
        } catch (Exception e) {
            logger.error("Error al reenviar correo de verificación a: {}", email, e);
            throw new RuntimeException("Error al reenviar el correo");
        }

        return new RegisterResponse(
                "Correo de verificación reenviado exitosamente. Por favor verifica tu correo.",
                email,
                "PENDIENTE"
        );
    }
    public RegisterResponse updateProfile(UpdateProfileRequest request) {
        logger.info("Actualizando perfil -apodo- -foto- para email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado para email: {}", request.getEmail());
                    return new RuntimeException("Usuario no encontrado");
                });

        user.setNickName(request.getNickName());
        user.setPhoto(request.getPhoto());
        user.setUpdatedAt(new Date(System.currentTimeMillis()));
        userRepository.save(user);

        logger.info("Perfil actualizado exitosamente para: {}", request.getEmail());

        return new RegisterResponse(
                "Perfil actualizado exitosamente.",
                user.getEmail(),
                user.getStatus()
        );
    }

    @Transactional
    public AuthResponse registerGoogle(GoogleRegisterRequest request) {

        // Si ya existe → login directo
        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getGoogleId() == null) {
                user.setGoogleId(request.getGoogleId());
                userRepository.save(user);
            }

            String accessToken = jwtService.generateToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());
            return new AuthResponse(
                    accessToken, refreshToken,
                    user.getEmail(), user.getNickName(),
                    user.getPhoto(), user.getRole().getName()
            );
        }

        // Si es nuevo → registra como ACTIVO sin verificación
        Role role = roleRepository.findByName("CIUDADANO")
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Neighborhood neighborhood = neighborhoodRepository.findById(request.getNeighborhoodId())
                .orElseThrow(() -> new RuntimeException("Barrio no encontrado"));

        User user = new User();
        user.setNames(request.getNames());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoto(request.getPhoto());
        user.setGoogleId(request.getGoogleId()); // ← agrega esto
        user.setDocumentNumber(request.getDocumentNumber());
        user.setDocumentType(request.getDocumentType());
        user.setBirthDate(request.getBirthDate());
        user.setNeighborhoodId(neighborhood);
        user.setAddress(request.getAddress());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);
        user.setStatus("ACTIVO");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setCreatedAt(new Date(System.currentTimeMillis()));
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken, refreshToken,
                user.getEmail(), user.getNickName(),
                user.getPhoto(), user.getRole().getName()
        );
    }

    public AuthResponse loginGoogle(String email, String googleId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verifica que el googleId coincida
        if (user.getGoogleId() == null || !user.getGoogleId().equals(googleId)) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken, refreshToken,
                user.getEmail(), user.getNickName(),
                user.getPhoto(), user.getRole().getName()
        );
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RegisterResponse requestPasswordReset(String email) {
        logger.info("Solicitud de recuperación de contraseña para: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Eliminar token anterior si existe
        tokenRepository.deleteByUser(user);

        // Crear nuevo token
        String newToken = UUID.randomUUID().toString();
        TokenSecurity ts = new TokenSecurity();
        ts.setUser(user);
        ts.setToken(newToken);
        ts.setExpiredIn(LocalDateTime.now().plusMinutes(15).toString());
        ts.setUsage(false);
        tokenRepository.save(ts);

        // Enviar correo con el link
        String link = "http://localhost:4200/reset-password?token=" + newToken;
        try {
            emailService.enviarRecuperacion(email, link); // ← nuevo método en EmailService
            logger.info("Correo de recuperación enviado a: {}", email);
        } catch (Exception e) {
            logger.error("Error al enviar correo de recuperación a: {}", email, e);
            throw new RuntimeException("Error al enviar el correo");
        }

        return new RegisterResponse(
                "Correo de recuperación enviado. Revisa tu bandeja de entrada.",
                email,
                "PENDIENTE"
        );
    }

    @Transactional
    public RegisterResponse resetPassword(String token, String newPassword) {
        logger.info("Restableciendo contraseña con token: {}", token);

        TokenSecurity ts = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenInvalidoException("Token no existe"));

        if (ts.isUsage()) {
            throw new TokenInvalidoException("Este enlace ya fue utilizado.");
        }

        LocalDateTime expiracionTime = LocalDateTime.parse(ts.getExpiredIn());
        if (expiracionTime.isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoException("El enlace expiró. Solicita uno nuevo.");
        }

        User user = ts.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(new Date(System.currentTimeMillis()));
        userRepository.save(user);

        ts.setUsage(true);
        tokenRepository.save(ts);

        logger.info("Contraseña restablecida para: {}", user.getEmail());

        return new RegisterResponse(
                "Contraseña restablecida exitosamente.",
                user.getEmail(),
                user.getStatus()
        );
    }

}
