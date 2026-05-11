package com.co.ucentral.gestionResiduos.back.ecoPoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO para crear o actualizar Eco Puntos
 * Simplifica la comunicación entre Frontend y Backend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcoPointDTO {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String description;
    private String status;
    private List<String> residueTypes;
    private Integer neighborhoodId;
    private String openingTime;
    private String closingTime;

}


