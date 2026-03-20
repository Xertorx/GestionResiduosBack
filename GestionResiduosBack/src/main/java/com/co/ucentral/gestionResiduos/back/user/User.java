package com.co.ucentral.gestionResiduos.back.user;

import java.sql.Date;

import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood;
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
    @Column(name = "documentnumber")
    private int documentNumber;

    @Column(name = "names")
    private String names;
    @Column(name = "lastname")
    private String lastName;
    @Column(name = "nickname")
    private String nickName;
    @Column(name = "documenttype")
    private String documentType;
    @Column(name = "email")
    private String email;
    @Column(name = "birthdate")
    private Date birthDate;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "neighborhoodid", nullable = false)
    private Neighborhood neighborhoodId;

    @Column(name = "address")
    private String address;
    @Column(name = "photo")
    private String photo;
    @Column(name = "password")
    private String password;
    @Column(name = "status")
    private String status;
    @Column(name = "phonenumber")
    private String phoneNumber;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleid", nullable = false)
    private Role role;
    @Column(name = "createdat")
    private Date createdAt;
    @Column(name = "updatedat")
    private Date updatedAt;
   
}

