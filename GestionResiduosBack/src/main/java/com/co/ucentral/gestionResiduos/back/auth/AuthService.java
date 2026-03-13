package com.co.ucentral.gestionResiduos.back.auth;

import com.co.ucentral.gestionResiduos.back.role.RoleRepository;
import com.co.ucentral.gestionResiduos.back.exception.EmailAlreadyExistsException;
import com.co.ucentral.gestionResiduos.back.exception.InvalidCredentialsException;
import com.co.ucentral.gestionResiduos.back.exception.IdAlreadyExistsException;
import com.co.ucentral.gestionResiduos.back.exception.PhoneAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import com.co.ucentral.gestionResiduos.back.security.JwtService;
import com.co.ucentral.gestionResiduos.back.role.Role;
import java.sql.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {

        logger.info("Intentando registrar usuario con email: {}", request.getEmail());

        //Validaciones
        if (userRepository.findById(request.getId()).isPresent()) {
            logger.warn("ID ya registrado: {}", request.getId());
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

        User user = new User();
        user.setId(request.getId());
        user.setName(request.getName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setRole(role);
        user.setCreatedAt(new Date(System.currentTimeMillis()));
        user.setPoints(0);

        userRepository.save(user);

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refresh(String refreshToken) {

        String email = jwtService.extractUsername(refreshToken);

        String newAccessToken = jwtService.generateToken(email);

        return new AuthResponse(newAccessToken, refreshToken);
    }
}