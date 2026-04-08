package com.co.ucentral.gestionResiduos.back.auth.register;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleRegisterRequest {
    private String names;
    private String lastName;
    private String email;
    private String photo;
    private String googleId;
    private String documentType;
    private int documentNumber;
    private Date birthDate;
    private int neighborhoodId;
    private String address;
    private String phoneNumber;
}