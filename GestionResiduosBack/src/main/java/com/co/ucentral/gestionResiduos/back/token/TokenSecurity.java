package com.co.ucentral.gestionResiduos.back.token;

import com.co.ucentral.gestionResiduos.back.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "securitytokens")
public class TokenSecurity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtoken")
    private int idToken;
    @Column(name = "token")
    private String token;
    @Column(name = "expiredin")
    private String expiredIn;
    @Column(name = "usage")
    private boolean usage;
    @ManyToOne
    @JoinColumn(name = "documentnumber", nullable = false)
    private User user;

}
