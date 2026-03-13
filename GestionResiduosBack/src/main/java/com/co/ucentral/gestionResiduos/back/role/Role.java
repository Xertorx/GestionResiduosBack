package com.co.ucentral.gestionResiduos.back.role;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private int idRole;
    
    @Column(unique = true, nullable = false)
    private String name;
    private String description;
    private String permissions; 

    public Role(int idRole) {
        this.idRole = idRole;
    }
}
