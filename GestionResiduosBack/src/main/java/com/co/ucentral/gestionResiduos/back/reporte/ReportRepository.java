package com.co.ucentral.gestionResiduos.back.reporte;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    /**
     * Obtener reportes por usuario (identificado por documentNumber)
     */
    List<Report> findByUserDocumentNumber(int documentNumber);

    /**
     * Obtener reportes por tipo
     */
    List<Report> findByType(String type);

    /**
     * Obtener reportes por estado
     */
    List<Report> findByStatus(String status);

    /**
     * Obtener reportes por tipo y estado
     */
    List<Report> findByTypeAndStatus(String type, String status);

    /**
     * Obtener reportes por categoría
     */
    List<Report> findByCategoryId(Integer categoryId);

    /**
     * Obtener reportes pendientes (HU25)
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'pendiente' ORDER BY r.createdAt DESC")
    List<Report> findPending();

    /**
     * Obtener reportes por usuario y tipo (HU34 - Consulta de estado)
     */
    List<Report> findByUserDocumentNumberAndType(int documentNumber, String type);

    /**
     * Obtener reporte por ID con usuario lazy loaded
     */
    Optional<Report> findById(Long id);
}

