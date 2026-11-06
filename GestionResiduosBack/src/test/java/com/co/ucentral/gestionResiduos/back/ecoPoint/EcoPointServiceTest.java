package com.co.ucentral.gestionResiduos.back.ecoPoint;

import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.NeighborhoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class EcoPointServiceTest {

    private EcoPointService ecoPointService;

    @Mock
    private EcoPointRepository ecoPointRepository;

    @Mock
    private NeighborhoodRepository neighborhoodRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ecoPointService = new EcoPointService(ecoPointRepository, neighborhoodRepository);
    }

    private com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood buildNeighborhood(int id) {
        com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood n =
                new com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood();
        n.neighborhoodId = id;
        n.setName("Barrio Test");
        return n;
    }

    private EcoPoint buildEcoPoint(Long id, String name, String status) {
        EcoPoint ep = new EcoPoint();
        ep.setId(id);
        ep.setName(name);
        ep.setAddress("Calle 1");
        ep.setLatitude(4.2206);
        ep.setLongitude(-74.1479);
        ep.setStatus(status);
        ep.setNeighborhood(buildNeighborhood(1));
        return ep;
    }

    @Test
    public void testGetAllEcoPoints() {
        // Arrange
        List<EcoPoint> ecoPoints = Arrays.asList(
                buildEcoPoint(1L, "Eco Punto 1", "ACTIVO"),
                buildEcoPoint(2L, "Eco Punto 2", "ACTIVO")
        );
        when(ecoPointRepository.findAll()).thenReturn(ecoPoints);

        // Act
        List<EcoPoint> result = ecoPointService.getAllEcoPoints();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Eco Punto 1", result.get(0).getName());
        verify(ecoPointRepository, times(1)).findAll();
    }

    @Test
    public void testGetEcoPointById() {
        // Arrange
        EcoPoint ecoPoint = buildEcoPoint(1L, "Eco Punto Test", "ACTIVO");
        when(ecoPointRepository.findById(1L)).thenReturn(Optional.of(ecoPoint));

        // Act
        Optional<EcoPoint> result = ecoPointService.getEcoPointById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Eco Punto Test", result.get().getName());
        verify(ecoPointRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetActiveEcoPoints() {
        // Arrange
        List<EcoPoint> activeEcoPoints = List.of(buildEcoPoint(1L, "Eco Punto 1", "ACTIVO"));
        when(ecoPointRepository.findByStatus("ACTIVO")).thenReturn(activeEcoPoints);

        // Act
        List<EcoPoint> result = ecoPointService.getActiveEcoPoints();

        // Assert
        assertEquals(1, result.size());
        assertEquals("ACTIVO", result.get(0).getStatus());
        verify(ecoPointRepository, times(1)).findByStatus("ACTIVO");
    }

    @Test
    public void testCreateEcoPoint() {
        // Arrange
        EcoPoint ecoPointToCreate = buildEcoPoint(null, "Nuevo Eco Punto", null);
        EcoPoint savedEcoPoint = buildEcoPoint(1L, "Nuevo Eco Punto", "ACTIVO");
        when(neighborhoodRepository.findById(1)).thenReturn(Optional.of(buildNeighborhood(1)));
        when(ecoPointRepository.save(any(EcoPoint.class))).thenReturn(savedEcoPoint);

        // Act
        EcoPoint result = ecoPointService.createEcoPoint(ecoPointToCreate);

        // Assert
        assertNotNull(result.getId());
        assertEquals("Nuevo Eco Punto", result.getName());
        assertEquals("ACTIVO", result.getStatus());
        verify(ecoPointRepository, times(1)).save(any(EcoPoint.class));
    }

    @Test
    public void testUpdateEcoPoint() {
        // Arrange
        Long id = 1L;
        EcoPoint existingEcoPoint = buildEcoPoint(id, "Eco Punto Original", "ACTIVO");
        EcoPoint ecoPointDetails = buildEcoPoint(null, "Eco Punto Actualizado", null);

        when(neighborhoodRepository.findById(1)).thenReturn(Optional.of(buildNeighborhood(1)));
        when(ecoPointRepository.findById(id)).thenReturn(Optional.of(existingEcoPoint));
        when(ecoPointRepository.save(any(EcoPoint.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Optional<EcoPoint> result = ecoPointService.updateEcoPoint(id, ecoPointDetails);

        // Assert
        assertTrue(result.isPresent());
        verify(ecoPointRepository, times(1)).findById(id);
        verify(ecoPointRepository, times(1)).save(any(EcoPoint.class));
    }

    @Test
    public void testDeleteEcoPoint() {
        // Arrange
        Long id = 1L;
        when(ecoPointRepository.existsById(id)).thenReturn(true);

        // Act
        boolean result = ecoPointService.deleteEcoPoint(id);

        // Assert
        assertTrue(result);
        verify(ecoPointRepository, times(1)).existsById(id);
        verify(ecoPointRepository, times(1)).deleteById(id);
    }

    @Test
    public void testDeleteEcoPointNotFound() {
        // Arrange
        Long id = 999L;
        when(ecoPointRepository.existsById(id)).thenReturn(false);

        // Act
        boolean result = ecoPointService.deleteEcoPoint(id);

        // Assert
        assertFalse(result);
        verify(ecoPointRepository, times(1)).existsById(id);
        verify(ecoPointRepository, times(0)).deleteById(id);
    }

    @Test
    public void testChangeEcoPointStatus() {
        // Arrange
        Long id = 1L;
        EcoPoint existingEcoPoint = buildEcoPoint(id, "Eco Punto", "ACTIVO");
        when(ecoPointRepository.findById(id)).thenReturn(Optional.of(existingEcoPoint));
        when(ecoPointRepository.save(any(EcoPoint.class))).thenAnswer(i -> {
            EcoPoint ep = i.getArgument(0);
            ep.setStatus("INACTIVO");
            return ep;
        });

        // Act
        Optional<EcoPoint> result = ecoPointService.changeEcoPointStatus(id, "INACTIVO");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("INACTIVO", result.get().getStatus());
        verify(ecoPointRepository, times(1)).findById(id);
        verify(ecoPointRepository, times(1)).save(any(EcoPoint.class));
    }
}
