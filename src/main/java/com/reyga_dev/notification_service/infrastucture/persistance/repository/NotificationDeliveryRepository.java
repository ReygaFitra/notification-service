package com.reyga_dev.notification_service.infrastucture.persistance.repository;

import com.reyga_dev.notification_service.infrastucture.persistance.entity.TNotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<TNotificationDelivery, UUID> {
}
