package com.co.ucentral.gestionResiduos.back.Geography.neighborhood;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NeighborhoodService {

    private final NeighborhoodRepository neighborhoodRepository;

    public NeighborhoodService(NeighborhoodRepository neighborhoodRepository) {
        this.neighborhoodRepository = neighborhoodRepository;
    }

    public java.util.List<Neighborhood> getNeighborhoodsByDistrict(Integer districtId) {
        if (districtId == null) {
            return neighborhoodRepository.findAll();
        }
        return neighborhoodRepository.findAll().stream()
                .filter(n -> n.getDistrictId() != null && n.getDistrictId().getDistrictId() == districtId)
                .collect(java.util.stream.Collectors.toList());
    }

    public Neighborhood getById(Integer id) {
        return neighborhoodRepository.findById(id).orElse(null);
    }


}
