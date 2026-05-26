package com.reyga_dev.notification_service.consumer;

import com.reyga_dev.notification_service.application.NotificationCommandService;
import com.reyga_dev.notification_service.common.ServiceUtils;
import com.reyga_dev.notification_service.domain.dto.NotificationRequestedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationCommandService notificationCommandService;
    private final ServiceUtils serviceUtils;

    public NotificationEventConsumer(NotificationCommandService notificationCommandService, ServiceUtils serviceUtils) {
        this.notificationCommandService = notificationCommandService;
        this.serviceUtils = serviceUtils;
    }

    @KafkaListener(
            topics = "${notification-service.kafka.topic.notification-requested}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info(
                "[Received Event] ===> Received notification event. topic={}, partition={}, offset={}, key={}",
                consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), consumerRecord.key()
        );

        NotificationRequestedEvent event = serviceUtils.readValue(consumerRecord.value(), NotificationRequestedEvent.class);
        notificationCommandService.processNotificationEvent(event, consumerRecord);

        acknowledgment.acknowledge();

        log.info(
                "[Event Acknowledged] ===> Kafka message acknowledged. topic={}, partition={}, offset={}",
                consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset()
        );
    }

}
