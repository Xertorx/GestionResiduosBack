package com.co.ucentral.gestionResiduos.back.Geography.neighborhood;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Integer> {

    @Query("SELECT n FROM Neighborhood n " +
            "WHERE (:districtId IS NULL OR n.districtId.districtId = :districtId) " +
            "AND (:cityId IS NULL OR n.districtId.cityId.cityId = :cityId)")
    List<Neighborhood> findByLocation(
            @Param("districtId") Integer districtId,
            @Param("cityId") Integer cityId
    );

}
