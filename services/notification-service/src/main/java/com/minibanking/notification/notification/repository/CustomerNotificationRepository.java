package com.minibanking.notification.notification.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minibanking.notification.notification.domain.CustomerNotification;

public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, UUID> {

    boolean existsByEventId(UUID eventId);

    Optional<CustomerNotification> findByEventId(UUID eventId);
}
