package com.co.ucentral.gestionResiduos.back.auth.login;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public String getEmail() { return email; }

    public String getPassword() { return password; }
}
