package com.co.ucentral.gestionResiduos.back.Geography.neighborhood;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/geography")
@RequiredArgsConstructor
public class NeighborhoodController {

    private final NeighborhoodService neighborhoodService;

    @GetMapping("/neighborhoods")
    public java.util.List<Neighborhood> getNeighborhoods(@org.springframework.web.bind.annotation.RequestParam(required = false) Integer districtId) {
        return neighborhoodService.getNeighborhoodsByDistrict(districtId);
    }

    @GetMapping("/neighborhoods/{id}/location")
    public Response getNeighborhoodLocation(@org.springframework.web.bind.annotation.PathVariable Integer id) {
        Neighborhood n = neighborhoodService.getById(id);
        if (n == null) {
            throw new com.co.ucentral.gestionResiduos.back.exception.ResourceNotFoundException("Barrio no encontrado: " + id);
        }
        District d = n.getDistrictId();
        com.co.ucentral.gestionResiduos.back.Geography.City.City c = d != null ? d.getCityId() : null;
        Response r = new Response();
        r.neighborhoodId = n.getNeighborhoodId();
        r.neighborhoodName = n.getName();
        if (d != null) {
            r.districtId = d.getDistrictId();
            r.districtName = d.getName();
        }
        if (c != null) {
            r.cityId = c.getCityId();
            r.cityName = c.getName();
        }
        return r;
    }

    // Simple response DTO
    public static class Response {
        public Integer neighborhoodId;
        public String neighborhoodName;
        public Integer districtId;
        public String districtName;
        public Integer cityId;
        public String cityName;
    }

}
