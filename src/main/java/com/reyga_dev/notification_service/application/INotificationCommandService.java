package com.reyga_dev.notification_service.application;

import com.reyga_dev.notification_service.domain.dto.NotificationRequestedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface INotificationCommandService {

    void processNotificationEvent(NotificationRequestedEvent event,  ConsumerRecord<String, String> consumerRecord);

}
