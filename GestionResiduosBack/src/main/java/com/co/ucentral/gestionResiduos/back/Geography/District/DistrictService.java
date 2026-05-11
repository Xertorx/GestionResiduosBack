package com.co.ucentral.gestionResiduos.back.Geography.District;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistrictService {

    private final DistrictRepository districtRepository;

    public DistrictService(DistrictRepository districtRepository) {
        this.districtRepository = districtRepository;
    }

    public java.util.List<District> getDistrictsByCity(Integer cityId) {
        if (cityId == null) {
            return districtRepository.findAll();
        }
        return districtRepository.findAll().stream()
                .filter(d -> d.getCityId() != null && d.getCityId().getCityId() == cityId)
                .collect(java.util.stream.Collectors.toList());
    }


}
