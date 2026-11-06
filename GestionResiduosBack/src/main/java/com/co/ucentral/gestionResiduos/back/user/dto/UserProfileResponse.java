package com.co.ucentral.gestionResiduos.back.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private int documentNumber;
    private String names;
    private String lastName;
    private String nickName;
    private String documentType;
    private String email;
    private Date birthDate;
    private String neighborhoodName;
    private Integer neighborhoodId;
    private String address;
    private String photo;
    private String phoneNumber;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private String roleName;
    private boolean canUpdate;
    private String nextUpdateAvailable;

    /** ── HU22: puntos acumulados por respuestas correctas ── */
    private Integer points;
}