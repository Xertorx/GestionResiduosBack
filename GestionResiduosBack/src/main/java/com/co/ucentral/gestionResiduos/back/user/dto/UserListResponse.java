package com.co.ucentral.gestionResiduos.back.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListResponse {
    private int documentNumber;
    private String names;
    private String lastName;
    private String photo;
    private String status;
    private String email;
}

