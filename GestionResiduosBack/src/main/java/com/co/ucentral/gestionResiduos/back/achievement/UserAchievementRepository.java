package com.co.ucentral.gestionResiduos.back.achievement;

import com.co.ucentral.gestionResiduos.back.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserOrderByFechaObtenidoDesc(User user);
    boolean existsByUserAndAchievement(User user, Achievement achievement);
    long countByUser(User user);
}