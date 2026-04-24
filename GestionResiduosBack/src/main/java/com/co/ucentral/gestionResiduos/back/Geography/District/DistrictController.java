package com.co.ucentral.gestionResiduos.back.Geography.District;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/geography")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @GetMapping("/districts")
    public java.util.List<District> getDistricts(@org.springframework.web.bind.annotation.RequestParam(required = false) Integer cityId) {
        return districtService.getDistrictsByCity(cityId);
    }

}
