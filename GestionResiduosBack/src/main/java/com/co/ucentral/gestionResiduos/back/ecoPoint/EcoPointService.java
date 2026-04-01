package com.co.ucentral.gestionResiduos.back.ecoPoint;

import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood;
import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.NeighborhoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EcoPointService {

    private final EcoPointRepository ecoPointRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    public EcoPointService(EcoPointRepository ecoPointRepository, NeighborhoodRepository neighborhoodRepository) {
        this.ecoPointRepository = ecoPointRepository;
        this.neighborhoodRepository = neighborhoodRepository;
    }

    /**
     * Obtener todos los eco puntos
     */
    public List<EcoPoint> getAllEcoPoints() {
        return ecoPointRepository.findAll();
    }

    /**
     * Obtener eco punto por ID
     */
    public Optional<EcoPoint> getEcoPointById(Long id) {
        return ecoPointRepository.findById(id);
    }

    /**
     * Obtener eco puntos activos
     */
    public List<EcoPoint> getActiveEcoPoints() {
        return ecoPointRepository.findByStatus("ACTIVO");
    }

    /**
     * Obtener eco puntos por barrio (neighborhood)
     */
    public List<EcoPoint> getEcoPointsByNeighborhood(Integer neighborhoodId) {
        return ecoPointRepository.findByNeighborhoodId(neighborhoodId);
    }

    /**
     * Obtener eco puntos activos por barrio
     */
    public List<EcoPoint> getActiveEcoPointsByNeighborhood(Integer neighborhoodId) {
        return ecoPointRepository.findByNeighborhoodIdAndStatus(neighborhoodId, "ACTIVO");
    }

    /**
     * Obtener eco puntos por tipo de residuo
     */
    public List<EcoPoint> getEcoPointsByResidueType(String residueType) {
        return ecoPointRepository.findByResidueType(residueType);
    }

    /**
     * Obtener eco puntos activos por tipo de residuo
     */
    public List<EcoPoint> getActiveEcoPointsByResidueType(String residueType) {
        return ecoPointRepository.findActiveByResidueType(residueType);
    }

    /**
     * Obtener eco puntos por barrio y tipo de residuo
     */
    public List<EcoPoint> getEcoPointsByNeighborhoodAndResidueType(Integer neighborhoodId, String residueType) {
        return ecoPointRepository.findByNeighborhoodAndResidueType(neighborhoodId, residueType);
    }

    /**
     * Obtener eco puntos activos por barrio y tipo de residuo
     */
    public List<EcoPoint> getActiveEcoPointsByNeighborhoodAndResidueType(Integer neighborhoodId, String residueType) {
        return ecoPointRepository.findActiveByNeighborhoodAndResidueType(neighborhoodId, residueType);
    }

    /**
     * Crear un nuevo eco punto
     */
    public EcoPoint createEcoPoint(EcoPoint ecoPoint) {
        // Validar que el neighborhood existe
        if (ecoPoint.getNeighborhood() == null || ecoPoint.getNeighborhood().neighborhoodId == 0) {
            throw new IllegalArgumentException("El barrio (neighborhood) es obligatorio");
        }
        
        // Buscar el neighborhood en la BD
        Neighborhood neighborhood = neighborhoodRepository.findById(ecoPoint.getNeighborhood().neighborhoodId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El barrio con ID " + ecoPoint.getNeighborhood().neighborhoodId + " no existe"));
        
        // Asignar el neighborhood persistido
        ecoPoint.setNeighborhood(neighborhood);
        ecoPoint.setCreatedAt(new Date(System.currentTimeMillis()));
        ecoPoint.setUpdatedAt(new Date(System.currentTimeMillis()));
        if (ecoPoint.getStatus() == null) {
            ecoPoint.setStatus("ACTIVO");
        }
        return ecoPointRepository.save(ecoPoint);
    }

    /**
     * Actualizar un eco punto existente
     */
    public Optional<EcoPoint> updateEcoPoint(Long id, EcoPoint ecoPointDetails) {
        return ecoPointRepository.findById(id).map(ecoPoint -> {
            if (ecoPointDetails.getName() != null) {
                ecoPoint.setName(ecoPointDetails.getName());
            }
            if (ecoPointDetails.getAddress() != null) {
                ecoPoint.setAddress(ecoPointDetails.getAddress());
            }
            if (ecoPointDetails.getLatitude() != null) {
                ecoPoint.setLatitude(ecoPointDetails.getLatitude());
            }
            if (ecoPointDetails.getLongitude() != null) {
                ecoPoint.setLongitude(ecoPointDetails.getLongitude());
            }
            if (ecoPointDetails.getDescription() != null) {
                ecoPoint.setDescription(ecoPointDetails.getDescription());
            }
            if (ecoPointDetails.getStatus() != null) {
                ecoPoint.setStatus(ecoPointDetails.getStatus());
            }
            if (ecoPointDetails.getResidueTypes() != null) {
                ecoPoint.setResidueTypes(ecoPointDetails.getResidueTypes());
            }
            if (ecoPointDetails.getNeighborhood() != null) {
                // Validar que el neighborhood existe
                Neighborhood neighborhood = neighborhoodRepository.findById(ecoPointDetails.getNeighborhood().neighborhoodId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "El barrio con ID " + ecoPointDetails.getNeighborhood().neighborhoodId + " no existe"));
                ecoPoint.setNeighborhood(neighborhood);
            }
            if (ecoPointDetails.getOpeningTime() != null) {
                ecoPoint.setOpeningTime(ecoPointDetails.getOpeningTime());
            }
            if (ecoPointDetails.getClosingTime() != null) {
                ecoPoint.setClosingTime(ecoPointDetails.getClosingTime());
            }
            ecoPoint.setUpdatedAt(new Date(System.currentTimeMillis()));
            return ecoPointRepository.save(ecoPoint);
        });
    }

    /**
     * Cambiar estado de un eco punto (ACTIVO/INACTIVO)
     */
    public Optional<EcoPoint> changeEcoPointStatus(Long id, String status) {
        return ecoPointRepository.findById(id).map(ecoPoint -> {
            ecoPoint.setStatus(status);
            ecoPoint.setUpdatedAt(new Date(System.currentTimeMillis()));
            return ecoPointRepository.save(ecoPoint);
        });
    }

    /**
     * Eliminar un eco punto
     */
    public boolean deleteEcoPoint(Long id) {
        if (ecoPointRepository.existsById(id)) {
            ecoPointRepository.deleteById(id);
            return true;
        }
        return false;
    }

}

