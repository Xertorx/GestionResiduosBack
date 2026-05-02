package com.co.ucentral.gestionResiduos.back.achievement;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * GET /api/achievements/me
     * Todos los logros con estado unlocked/locked para el usuario autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<List<AchievementDTO>> getMyAchievements(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(achievementService.getAllForUser(email));
    }

    /**
     * GET /api/achievements/me/unlocked
     * Solo los logros desbloqueados del usuario.
     */
    @GetMapping("/me/unlocked")
    public ResponseEntity<List<AchievementDTO>> getMyUnlocked(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(achievementService.getUnlockedForUser(email));
    }
}