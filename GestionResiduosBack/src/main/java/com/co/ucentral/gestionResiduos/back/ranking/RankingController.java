package com.co.ucentral.gestionResiduos.back.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * GET /api/ranking?limit=50
     * Retorna el ranking de usuarios ordenado por puntaje acumulado.
     * Endpoint PÚBLICO (no requiere JWT).
     */
    @GetMapping
    public ResponseEntity<List<RankingDTO>> getRanking(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(rankingService.getRanking(limit));
    }
}