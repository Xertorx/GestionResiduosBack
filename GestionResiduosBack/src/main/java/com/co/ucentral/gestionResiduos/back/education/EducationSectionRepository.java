package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationSectionRepository extends JpaRepository<EducationSection, Long> {
    List<EducationSection> findByContentId(Long contentId);
}

