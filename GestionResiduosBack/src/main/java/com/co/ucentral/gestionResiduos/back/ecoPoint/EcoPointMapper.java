package com.co.ucentral.gestionResiduos.back.ecoPoint;

import org.springframework.stereotype.Component;
import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.NeighborhoodRepository;

/**
 * Mapper para convertir entre EcoPoint Entity y EcoPointDTO
 */
@Component
public class EcoPointMapper {

    private final NeighborhoodRepository neighborhoodRepository;

    public EcoPointMapper(NeighborhoodRepository neighborhoodRepository) {
        this.neighborhoodRepository = neighborhoodRepository;
    }

    /**
     * Convierte EcoPointDTO a EcoPoint Entity
     */
    public EcoPoint toEntity(EcoPointDTO dto) {
        if (dto == null) {
            return null;
        }

        EcoPoint ecoPoint = new EcoPoint();
        ecoPoint.setId(dto.getId());
        ecoPoint.setName(dto.getName());
        ecoPoint.setAddress(dto.getAddress());
        ecoPoint.setLatitude(dto.getLatitude());
        ecoPoint.setLongitude(dto.getLongitude());
        ecoPoint.setDescription(dto.getDescription());
        ecoPoint.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVO");
        ecoPoint.setResidueTypes(dto.getResidueTypes());
        ecoPoint.setOpeningTime(dto.getOpeningTime());
        ecoPoint.setClosingTime(dto.getClosingTime());

        // Obtener el barrio por ID
        if (dto.getNeighborhoodId() != null) {
            ecoPoint.setNeighborhood(neighborhoodRepository.findById(dto.getNeighborhoodId()).orElse(null));
        }

        return ecoPoint;
    }

    /**
     * Convierte EcoPoint Entity a EcoPointDTO
     */
    public EcoPointDTO toDTO(EcoPoint ecoPoint) {
        if (ecoPoint == null) {
            return null;
        }

        EcoPointDTO dto = new EcoPointDTO();
        dto.setId(ecoPoint.getId());
        dto.setName(ecoPoint.getName());
        dto.setAddress(ecoPoint.getAddress());
        dto.setLatitude(ecoPoint.getLatitude());
        dto.setLongitude(ecoPoint.getLongitude());
        dto.setDescription(ecoPoint.getDescription());
        dto.setStatus(ecoPoint.getStatus());
        dto.setResidueTypes(ecoPoint.getResidueTypes());
        dto.setOpeningTime(ecoPoint.getOpeningTime());
        dto.setClosingTime(ecoPoint.getClosingTime());
        
        if (ecoPoint.getNeighborhood() != null) {
            dto.setNeighborhoodId(ecoPoint.getNeighborhood().neighborhoodId);
        }

        return dto;
    }

}




