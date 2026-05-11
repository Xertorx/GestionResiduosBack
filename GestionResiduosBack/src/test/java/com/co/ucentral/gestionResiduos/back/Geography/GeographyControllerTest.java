package com.co.ucentral.gestionResiduos.back.Geography;

import com.co.ucentral.gestionResiduos.back.Geography.City.City;
import com.co.ucentral.gestionResiduos.back.Geography.City.CityController;
import com.co.ucentral.gestionResiduos.back.Geography.City.CityService;
import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood;
import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.NeighborhoodController;
import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.NeighborhoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({NeighborhoodController.class, CityController.class})
class GeographyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NeighborhoodService neighborhoodService;

    @MockitoBean
    private CityService cityService;

    // ── GET /api/geography/cities ─────────────────────────────────────

    @Test
    void getCities_publico_retorna200() throws Exception {
        City city = new City();
        city.setCityId(1);
        city.setName("Bogotá");
        when(cityService.getAllCities()).thenReturn(List.of(city));

        mockMvc.perform(get("/api/geography/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bogotá"));
    }

    @Test
    void getCities_listaVacia_retorna200() throws Exception {
        when(cityService.getAllCities()).thenReturn(List.of());

        mockMvc.perform(get("/api/geography/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/geography/neighborhoods ─────────────────────────────

    @Test
    void getNeighborhoods_sinFiltro_retorna200() throws Exception {
        Neighborhood n = new Neighborhood();
        n.setNeighborhoodId(1);
        n.setName("Chapinero");
        when(neighborhoodService.getNeighborhoodsByDistrict(null)).thenReturn(List.of(n));

        mockMvc.perform(get("/api/geography/neighborhoods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Chapinero"));
    }

    @Test
    void getNeighborhoods_conDistrictId_retorna200() throws Exception {
        Neighborhood n = new Neighborhood();
        n.setNeighborhoodId(2);
        n.setName("Teusaquillo");
        when(neighborhoodService.getNeighborhoodsByDistrict(3)).thenReturn(List.of(n));

        mockMvc.perform(get("/api/geography/neighborhoods").param("districtId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Teusaquillo"));
    }

    // ── GET /api/geography/neighborhoods/{id}/location ────────────────

    @Test
    void getNeighborhoodLocation_existente_retorna200() throws Exception {
        Neighborhood n = new Neighborhood();
        n.setNeighborhoodId(1);
        n.setName("Chapinero");
        when(neighborhoodService.getById(1)).thenReturn(n);

        mockMvc.perform(get("/api/geography/neighborhoods/1/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighborhoodId").value(1))
                .andExpect(jsonPath("$.neighborhoodName").value("Chapinero"));
    }

    @Test
    void getNeighborhoodLocation_noExistente_retorna404() throws Exception {
        when(neighborhoodService.getById(99)).thenReturn(null);

        mockMvc.perform(get("/api/geography/neighborhoods/99/location"))
                .andExpect(status().isNotFound());
    }
}

