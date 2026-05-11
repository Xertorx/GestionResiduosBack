package com.co.ucentral.gestionResiduos.back.ecoPoint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EcoPointRepository extends JpaRepository<EcoPoint, Long> {

    // Buscar por nombre
    Optional<EcoPoint> findByName(String name);

    // Buscar todos los activos
    List<EcoPoint> findByStatus(String status);

    // Buscar por barrio (neighborhood)
    @Query("SELECT e FROM EcoPoint e WHERE e.neighborhood.neighborhoodId = :neighborhoodId")
    List<EcoPoint> findByNeighborhoodId(@Param("neighborhoodId") Integer neighborhoodId);

    // Buscar por tipo de residuo
    @Query("SELECT e FROM EcoPoint e WHERE :residueType MEMBER OF e.residueTypes")
    List<EcoPoint> findByResidueType(@Param("residueType") String residueType);

    // Buscar por barrio y tipo de residuo
    @Query("SELECT e FROM EcoPoint e WHERE e.neighborhood.neighborhoodId = :neighborhoodId AND :residueType MEMBER OF e.residueTypes")
    List<EcoPoint> findByNeighborhoodAndResidueType(@Param("neighborhoodId") Integer neighborhoodId, @Param("residueType") String residueType);

    // Buscar activos por tipo de residuo
    @Query("SELECT e FROM EcoPoint e WHERE e.status = 'ACTIVO' AND :residueType MEMBER OF e.residueTypes")
    List<EcoPoint> findActiveByResidueType(@Param("residueType") String residueType);

    // Buscar activos por barrio
    @Query("SELECT e FROM EcoPoint e WHERE e.neighborhood.neighborhoodId = :neighborhoodId AND e.status = 'ACTIVO'")
    List<EcoPoint> findByNeighborhoodIdAndStatus(@Param("neighborhoodId") Integer neighborhoodId, @Param("status") String status);

    // Buscar activos por barrio y tipo de residuo
    @Query("SELECT e FROM EcoPoint e WHERE e.neighborhood.neighborhoodId = :neighborhoodId AND e.status = 'ACTIVO' AND :residueType MEMBER OF e.residueTypes")
    List<EcoPoint> findActiveByNeighborhoodAndResidueType(@Param("neighborhoodId") Integer neighborhoodId, @Param("residueType") String residueType);

}

