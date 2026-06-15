package com.reyga_dev.notification_service.infrastucture.persistance.repository;

import com.reyga_dev.notification_service.domain.enums.NotificationOutboxStatus;
import com.reyga_dev.notification_service.infrastucture.persistance.entity.TNotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<TNotificationOutbox, UUID> {

    Optional<List<TNotificationOutbox>> findAllByStatusOrderByCreatedAtAsc(NotificationOutboxStatus status);

    Optional<TNotificationOutbox> findByEventId(String eventId);

}
