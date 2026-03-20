package com.co.ucentral.gestionResiduos.back.Geography.District;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/district")
@RequiredArgsConstructor

public class DistrictController {

    private final DistrictService districtService;



}
