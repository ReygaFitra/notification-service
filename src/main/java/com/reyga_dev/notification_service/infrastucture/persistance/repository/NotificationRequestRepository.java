package com.reyga_dev.notification_service.infrastucture.persistance.repository;

import com.reyga_dev.notification_service.infrastucture.persistance.entity.TNotificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRequestRepository extends JpaRepository<TNotificationRequest, UUID> {

    boolean existsByEventId(String eventId);

    Optional<TNotificationRequest> findByEventId(String eventId);

}
