package com.co.ucentral.gestionResiduos.back.ecoPoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Date;
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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ecoPointService = new EcoPointService(ecoPointRepository);
    }

    @Test
    public void testGetAllEcoPoints() {
        // Arrange
        List<EcoPoint> ecoPoints = Arrays.asList(
            new EcoPoint(1L, "Eco Punto 1", "Dirección 1", 4.2206, -74.1479, "Descripción 1", 
                        "(601) 1111111", "eco1@test.com", "ACTIVO", 
                        Arrays.asList("RECICLABLE"), null, "08:00", "18:00", 
                        new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis())),
            new EcoPoint(2L, "Eco Punto 2", "Dirección 2", 4.2189, -74.1456, "Descripción 2", 
                        "(601) 2222222", "eco2@test.com", "ACTIVO", 
                        Arrays.asList("ESPECIAL"), null, "09:00", "17:00", 
                        new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()))
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
        EcoPoint ecoPoint = new EcoPoint(1L, "Eco Punto Test", "Dirección Test", 4.2206, -74.1479, 
                                        "Descripción Test", "(601) 1111111", "eco@test.com", "ACTIVO", 
                                        Arrays.asList("RECICLABLE"), null, "08:00", "18:00", 
                                        new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));

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
        List<EcoPoint> activeEcoPoints = Arrays.asList(
            new EcoPoint(1L, "Eco Punto 1", "Dirección 1", 4.2206, -74.1479, "Descripción 1", 
                        "(601) 1111111", "eco1@test.com", "ACTIVO", 
                        Arrays.asList("RECICLABLE"), null, "08:00", "18:00", 
                        new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()))
        );

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
        EcoPoint ecoPointToCreate = new EcoPoint(null, "Nuevo Eco Punto", "Nueva Dirección", 
                                                4.2206, -74.1479, "Nueva Descripción", 
                                                "(601) 1111111", "neweoco@test.com", null, 
                                                Arrays.asList("RECICLABLE"), null, "08:00", "18:00", 
                                                null, null);

        EcoPoint savedEcoPoint = new EcoPoint(1L, "Nuevo Eco Punto", "Nueva Dirección", 
                                             4.2206, -74.1479, "Nueva Descripción", 
                                             "(601) 1111111", "neweoco@test.com", "ACTIVO", 
                                             Arrays.asList("RECICLABLE"), null, "08:00", "18:00", 
                                             new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));

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
        EcoPoint existingEcoPoint = new EcoPoint(id, "Eco Punto Original", "Dirección Original", 
                                                4.2206, -74.1479, "Descripción Original", 
                                                "(601) 1111111", "eco@test.com", "ACTIVO", 
                                                Arrays.asList("RECICLABLE"), null, "08:00", "18:00", 
                                                new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));

        EcoPoint ecoPointDetails = new EcoPoint(null, "Eco Punto Actualizado", null, null, null, 
                                               "Descripción Actualizada", null, null, null, null, null, null, null, null, null);

        when(ecoPointRepository.findById(id)).thenReturn(Optional.of(existingEcoPoint));
        when(ecoPointRepository.save(any(EcoPoint.class))).thenReturn(existingEcoPoint);

        // Act
        Optional<EcoPoint> result = ecoPointService.updateEcoPoint(id, ecoPointDetails);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Eco Punto Actualizado", result.get().getName());
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
        EcoPoint existingEcoPoint = new EcoPoint(id, "Eco Punto", "Dirección", 
                                                4.2206, -74.1479, "Descripción", 
                                                "(601) 1111111", "eco@test.com", "ACTIVO", 
                                                Arrays.asList("RECICLABLE"), null, "08:00", "18:00", 
                                                new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));

        when(ecoPointRepository.findById(id)).thenReturn(Optional.of(existingEcoPoint));
        when(ecoPointRepository.save(any(EcoPoint.class))).thenReturn(existingEcoPoint);

        // Act
        Optional<EcoPoint> result = ecoPointService.changeEcoPointStatus(id, "INACTIVO");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("INACTIVO", result.get().getStatus());
        verify(ecoPointRepository, times(1)).findById(id);
        verify(ecoPointRepository, times(1)).save(any(EcoPoint.class));
    }

}

