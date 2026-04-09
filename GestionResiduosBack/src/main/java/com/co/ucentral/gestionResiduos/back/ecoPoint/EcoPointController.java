package com.co.ucentral.gestionResiduos.back.ecoPoint;

import com.co.ucentral.gestionResiduos.back.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ecopoints")
@RequiredArgsConstructor
public class EcoPointController {

    private final EcoPointService ecoPointService;

    /**
     * Obtener todos los eco puntos
     */
    @GetMapping
    public ResponseEntity<List<EcoPoint>> getAllEcoPoints() {
        List<EcoPoint> ecoPoints = ecoPointService.getAllEcoPoints();
        return ResponseEntity.ok(ecoPoints);
    }

    /**
     * Obtener eco puntos activos
     */
    @GetMapping("/active")
    public ResponseEntity<List<EcoPoint>> getActiveEcoPoints() {
        List<EcoPoint> ecoPoints = ecoPointService.getActiveEcoPoints();
        return ResponseEntity.ok(ecoPoints);
    }

    /**
     * Obtener eco punto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EcoPoint> getEcoPointById(@PathVariable Long id) {
        EcoPoint ecoPoint = ecoPointService.getEcoPointById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Eco punto no encontrado con ID: " + id));
        return ResponseEntity.ok(ecoPoint);
    }

    /**
     * Obtener eco puntos por barrio (neighborhood)
     */
    @GetMapping("/neighborhood/{neighborhoodId}")
    public ResponseEntity<List<EcoPoint>> getEcoPointsByNeighborhood(@PathVariable Integer neighborhoodId) {
        List<EcoPoint> ecoPoints = ecoPointService.getEcoPointsByNeighborhood(neighborhoodId);
        return ResponseEntity.ok(ecoPoints);
    }

    /**
     * Obtener eco puntos activos por barrio
     */
    @GetMapping("/neighborhood/{neighborhoodId}/active")
    public ResponseEntity<List<EcoPoint>> getActiveEcoPointsByNeighborhood(@PathVariable Integer neighborhoodId) {
        List<EcoPoint> ecoPoints = ecoPointService.getActiveEcoPointsByNeighborhood(neighborhoodId);
        return ResponseEntity.ok(ecoPoints);
    }

    /**
     * Obtener eco puntos por tipo de residuo
     */
    @GetMapping("/residue-type/{residueType}")
    public ResponseEntity<List<EcoPoint>> getEcoPointsByResidueType(@PathVariable String residueType) {
        List<EcoPoint> ecoPoints = ecoPointService.getEcoPointsByResidueType(residueType);
        return ResponseEntity.ok(ecoPoints);
    }

    /**
     * Obtener eco puntos activos por tipo de residuo
     */
    @GetMapping("/residue-type/{residueType}/active")
    public ResponseEntity<List<EcoPoint>> getActiveEcoPointsByResidueType(@PathVariable String residueType) {
        List<EcoPoint> ecoPoints = ecoPointService.getActiveEcoPointsByResidueType(residueType);
        return ResponseEntity.ok(ecoPoints);
    }

    /**
     * Obtener eco puntos por barrio y tipo de residuo
     */
    @GetMapping("/neighborhood/{neighborhoodId}/residue-type/{residueType}")
    public ResponseEntity<List<EcoPoint>> getEcoPointsByNeighborhoodAndResidueType(
            @PathVariable Integer neighborhoodId,
            @PathVariable String residueType) {
        List<EcoPoint> ecoPoints = ecoPointService.getEcoPointsByNeighborhoodAndResidueType(neighborhoodId, residueType);
        return ResponseEntity.ok(ecoPoints);
    }

    /**
     * Obtener eco puntos activos por barrio y tipo de residuo
     */
    @GetMapping("/neighborhood/{neighborhoodId}/residue-type/{residueType}/active")
    public ResponseEntity<List<EcoPoint>> getActiveEcoPointsByNeighborhoodAndResidueType(
            @PathVariable Integer neighborhoodId,
            @PathVariable String residueType) {
        List<EcoPoint> ecoPoints = ecoPointService.getActiveEcoPointsByNeighborhoodAndResidueType(neighborhoodId, residueType);
        return ResponseEntity.ok(ecoPoints);
    }

    /**
     * Crear un nuevo eco punto
     */
    @PostMapping
    public ResponseEntity<EcoPoint> createEcoPoint(@Valid @RequestBody EcoPoint ecoPoint) {
        EcoPoint createdEcoPoint = ecoPointService.createEcoPoint(ecoPoint);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEcoPoint);
    }

    /**
     * Actualizar un eco punto existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<EcoPoint> updateEcoPoint(
            @PathVariable Long id,
            @Valid @RequestBody EcoPoint ecoPointDetails) {
        EcoPoint updatedEcoPoint = ecoPointService.updateEcoPoint(id, ecoPointDetails)
                .orElseThrow(() -> new ResourceNotFoundException("Eco punto no encontrado con ID: " + id));
        return ResponseEntity.ok(updatedEcoPoint);
    }

    /**
     * Cambiar estado de un eco punto
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<EcoPoint> changeEcoPointStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        EcoPoint updatedEcoPoint = ecoPointService.changeEcoPointStatus(id, status)
                .orElseThrow(() -> new ResourceNotFoundException("Eco punto no encontrado con ID: " + id));
        return ResponseEntity.ok(updatedEcoPoint);
    }

    /**
     * Eliminar un eco punto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEcoPoint(@PathVariable Long id) {
        boolean deleted = ecoPointService.deleteEcoPoint(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Eco punto no encontrado con ID: " + id);
        }
        return ResponseEntity.noContent().build();
    }

}

