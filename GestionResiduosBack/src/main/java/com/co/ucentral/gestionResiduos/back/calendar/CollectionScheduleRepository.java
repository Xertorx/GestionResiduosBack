package com.co.ucentral.gestionResiduos.back.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionScheduleRepository extends JpaRepository<CollectionSchedule, Long> {

    List<CollectionSchedule> findByDistrictDistrictId(int districtId);

    List<CollectionSchedule> findByDistrictDistrictIdAndStatus(int districtId, String status);

    List<CollectionSchedule> findByDistrictDistrictIdAndResidueType(int districtId, String residueType);

    List<CollectionSchedule> findByDistrictDistrictIdAndDayOfWeek(int districtId, String dayOfWeek);

    List<CollectionSchedule> findByStatus(String status);
}

