package com.co.ucentral.gestionResiduos.back.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByUserDocumentNumber(int documentNumber);
    java.util.List<NotificationPreference> findByEnabledTrue();
}

