package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationContentRepository extends JpaRepository<EducationContent, Long> {
    List<EducationContent> findByCategory(String category);
}