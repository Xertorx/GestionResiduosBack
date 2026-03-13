package com.co.ucentral.gestionResiduos.back.user;

import java.sql.Date;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.co.ucentral.gestionResiduos.back.role.Role;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    private int id;
    private String name;

    @Column(name = "last_name")
    private String lastName;
    private String email;
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role", nullable = false, insertable = false, updatable = false)
    private Role role;

    @Column(name = "phone_number")
    private String phoneNumber;
    private String address;
    private String city;
    private String state;

    @Column(name = "created_at")
    private Date createdAt;
    private long points;
    
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;


   
}