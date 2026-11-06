package com.co.ucentral.gestionResiduos.back.Geography.neighborhood;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NeighborhoodService {

    private final NeighborhoodRepository neighborhoodRepository;

    public NeighborhoodService(NeighborhoodRepository neighborhoodRepository) {
        this.neighborhoodRepository = neighborhoodRepository;
    }

    public List<Neighborhood> getNeighborhoods(Integer districtId, Integer cityId) {
        return neighborhoodRepository.findByLocation(districtId, cityId);
    }

    public Neighborhood getById(Integer id) {
        return neighborhoodRepository.findById(id).orElse(null);
    }


}
