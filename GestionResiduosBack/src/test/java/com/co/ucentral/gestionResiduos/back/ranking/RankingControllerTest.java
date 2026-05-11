package com.co.ucentral.gestionResiduos.back.ranking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RankingController.class)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankingService rankingService;

    // ── GET /api/ranking ──────────────────────────────────────────────

    @Test
    void getRanking_publico_retorna200() throws Exception {
        List<RankingDTO> list = List.of(
                RankingDTO.builder().position(1).names("Ana").lastName("Ruiz").points(500).build(),
                RankingDTO.builder().position(2).names("Carlos").lastName("López").points(450).build()
        );
        when(rankingService.getRanking(50)).thenReturn(list);

        mockMvc.perform(get("/api/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].position").value(1))
                .andExpect(jsonPath("$[0].names").value("Ana"))
                .andExpect(jsonPath("$[0].points").value(500));
    }

    @Test
    void getRanking_conLimite10_retorna200() throws Exception {
        when(rankingService.getRanking(10)).thenReturn(List.of(
                RankingDTO.builder().position(1).names("Ana").points(500).build()
        ));

        mockMvc.perform(get("/api/ranking").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRanking_listaVacia_retorna200ConArrayVacio() throws Exception {
        when(rankingService.getRanking(50)).thenReturn(List.of());

        mockMvc.perform(get("/api/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

