package com.reyga_dev.notification_service.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class NotificationDltConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationDltConsumer.class);

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
        acknowledgment.acknowledge();
    }

}
