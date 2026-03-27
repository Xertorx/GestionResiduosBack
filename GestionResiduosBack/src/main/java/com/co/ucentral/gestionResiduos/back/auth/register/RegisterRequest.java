package com.co.ucentral.gestionResiduos.back.auth.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/*

Table Usesr {
  documentNumber int [pk]
  names varchar
  lastName varchar
  documentType varchar
  email varchar [unique]
  birthDate date
  neighborhoodId varchar [ref: > Neighborhood.neighborhoodId]
  address varchar
  password varchar
  status varchar // ACTIVE, INACTIVE
  roleId int [ref: > Role.roleId]
  updatedAt datetime
}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

   @NotNull(message = "El numero de documento es obligatorio")
    private int documentNumber;

    @NotBlank(message = "El nombre es obligatorio")
    private String names;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private Date birthDate;

    @NotNull(message = "El id del barrio es obligatorio")
    private int neighborhoodId;

    @NotNull(message = "La direccion es obligatoria")
    private String address;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El número de teléfono es obligatorio")
    private String phoneNumber;

    private String status;

    private Date createdAt;
}
