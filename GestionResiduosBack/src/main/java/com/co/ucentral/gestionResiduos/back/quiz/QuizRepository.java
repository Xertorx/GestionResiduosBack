package com.co.ucentral.gestionResiduos.back.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByContentId(Long contentId);
    boolean existsByContentId(Long contentId);
}