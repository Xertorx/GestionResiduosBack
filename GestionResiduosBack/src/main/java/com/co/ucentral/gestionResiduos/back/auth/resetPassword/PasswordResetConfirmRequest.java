package com.co.ucentral.gestionResiduos.back.auth.resetPassword;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetConfirmRequest {
    private String token;
    private String newPassword;
}