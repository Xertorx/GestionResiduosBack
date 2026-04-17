package com.co.ucentral.gestionResiduos.back.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserDocumentNumberOrderByCreatedAtDesc(int documentNumber);
    List<Notification> findByStatus(String status);
}

