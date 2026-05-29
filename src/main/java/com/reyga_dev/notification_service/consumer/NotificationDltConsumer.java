package com.reyga_dev.notification_service.consumer;

import com.reyga_dev.notification_service.application.INotificationCommandService;
import com.reyga_dev.notification_service.common.ServiceUtils;
import com.reyga_dev.notification_service.domain.dto.NotificationRequestedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class NotificationDltConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationDltConsumer.class);

    private final ServiceUtils serviceUtils;
    private final INotificationCommandService notificationCommandService;

    public NotificationDltConsumer(ServiceUtils serviceUtils, INotificationCommandService notificationCommandService) {
        this.serviceUtils = serviceUtils;
        this.notificationCommandService = notificationCommandService;
    }

    @KafkaListener(
            topics = "${notification-service.kafka.topic.notification-dlt}",
            groupId = "${spring.kafka.consumer.group-id}-dlt",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consumeDlt(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        log.error(
                "[NOTIFICATION DLT Message Received] ===> Received message from DLT. topic={}, partition={}, offset={}, key={}, value={}",
                consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), consumerRecord.key(), consumerRecord.value()
        );

        NotificationRequestedEvent event = serviceUtils.readValue(consumerRecord.value(), NotificationRequestedEvent.class);
        notificationCommandService.processDeadLetterEvent(event, consumerRecord);

        acknowledgment.acknowledge();

        log.info(
                "[DLQ Event Acknowledged] ===> Kafka message acknowledged. topic={}, partition={}, offset={}",
                consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset()
        );
    }

}
