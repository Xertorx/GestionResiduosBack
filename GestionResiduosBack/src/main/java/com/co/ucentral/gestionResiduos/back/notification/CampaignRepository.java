package com.co.ucentral.gestionResiduos.back.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatus(String status);
    List<Campaign> findByStatusAndNotifiedFalse(String status);
    List<Campaign> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String status, Date today1, Date today2);
}

