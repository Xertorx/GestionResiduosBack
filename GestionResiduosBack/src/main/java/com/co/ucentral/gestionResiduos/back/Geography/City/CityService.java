package com.co.ucentral.gestionResiduos.back.Geography.City;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public java.util.List<City> getAllCities() {
        return cityRepository.findAll();
    }



}
