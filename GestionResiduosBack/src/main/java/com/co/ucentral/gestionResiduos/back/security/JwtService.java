package com.co.ucentral.gestionResiduos.back.security;

import com.co.ucentral.gestionResiduos.back.exception.JwtAuthenticationException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long jwtExpiration;
    private final long refreshExpiration;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expiration}") long jwtExpiration,
                      @Value("${security.jwt.refresh-token.expiration}") long refreshExpiration) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes; current length=" + keyBytes.length);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * Extrae el email (subject) del token JWT.
     * Lanza JwtAuthenticationException con mensajes claros según el tipo de error.
     */
    public String extractUsername(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expirado: {}", e.getMessage());
            throw new JwtAuthenticationException("El token ha expirado. Por favor inicia sesión nuevamente.", e);
        } catch (MalformedJwtException e) {
            logger.warn("Token JWT malformado: {}", e.getMessage());
            throw new JwtAuthenticationException("Token malformado. Por favor inicia sesión nuevamente.", e);
        } catch (SignatureException e) {
            logger.warn("Firma de token JWT inválida: {}", e.getMessage());
            throw new JwtAuthenticationException("Token con firma inválida. Por favor inicia sesión nuevamente.", e);
        } catch (JwtException e) {
            logger.warn("Error de validación JWT: {}", e.getMessage());
            throw new JwtAuthenticationException("Token inválido. Por favor inicia sesión nuevamente.", e);
        }
    }

    /**
     * Valida si un token es válido (no expirado, firma correcta, bien formado).
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}