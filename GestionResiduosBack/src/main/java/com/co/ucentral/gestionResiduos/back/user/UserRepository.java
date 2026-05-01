package com.co.ucentral.gestionResiduos.back.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * HU31: Obtener usuarios para el ranking.
     * Solo VERIFICADOS con rol CIUDADANO, ordenados por puntos DESC.
     * Los que tienen puntos NULL se tratan como 0.
     */
    @Query("SELECT u FROM User u " +
            "WHERE u.status = 'ACTIVO' " +
            "AND u.role.name = 'CIUDADANO' " +
            "ORDER BY COALESCE(u.points, 0) DESC")
    List<User> findRankingUsers();
}