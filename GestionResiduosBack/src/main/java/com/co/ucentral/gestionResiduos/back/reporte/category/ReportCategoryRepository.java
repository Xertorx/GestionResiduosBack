package com.co.ucentral.gestionResiduos.back.reporte.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportCategoryRepository extends JpaRepository<ReportCategory, Integer> {

    List<ReportCategory> findByStatus(String status);

    Optional<ReportCategory> findByName(String name);

    boolean existsByName(String name);
}

