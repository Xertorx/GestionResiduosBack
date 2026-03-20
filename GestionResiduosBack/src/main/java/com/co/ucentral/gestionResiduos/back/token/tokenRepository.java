package com.co.ucentral.gestionResiduos.back.token;


import com.co.ucentral.gestionResiduos.back.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface tokenRepository extends JpaRepository<TokenSecurity, Integer> {
    Optional<TokenSecurity> findByToken(String token);
    void deleteByUser(User user);
}