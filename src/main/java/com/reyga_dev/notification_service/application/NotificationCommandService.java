package com.reyga_dev.notification_service.application;

import com.reyga_dev.notification_service.domain.dto.NotificationRequestedEvent;
import com.reyga_dev.notification_service.domain.enums.NotificationDeliveryStatus;
import com.reyga_dev.notification_service.domain.enums.NotificationRequestStatus;
import com.reyga_dev.notification_service.domain.exception.InvalidNotificationEventException;
import com.reyga_dev.notification_service.infrastucture.persistance.entity.TNotificationDelivery;
import com.reyga_dev.notification_service.infrastucture.persistance.entity.TNotificationRequest;
import com.reyga_dev.notification_service.infrastucture.persistance.repository.NotificationDeliveryRepository;
import com.reyga_dev.notification_service.infrastucture.persistance.repository.NotificationRequestRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationCommandService implements INotificationCommandService {

    private static final Logger log = LoggerFactory.getLogger(NotificationCommandService.class);

    private final NotificationRequestRepository notificationRequestRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;

    public NotificationCommandService(NotificationRequestRepository notificationRequestRepository, NotificationDeliveryRepository notificationDeliveryRepository) {
        this.notificationRequestRepository = notificationRequestRepository;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
    }

    @Override
    @Transactional
    public void processNotificationEvent(NotificationRequestedEvent event, ConsumerRecord<String, String> consumerRecord) {
        log.info("[Notification Processing Start] ---> Event ID : {}", event.eventId());
        this.validateEvent(event);

        try {
            TNotificationRequest notificationRequest = this.constructNotificationRequest(event, consumerRecord);
            notificationRequestRepository.saveAndFlush(notificationRequest);

            TNotificationDelivery notificationDelivery = this.constructNotificationDelivery(event, notificationRequest);
            notificationDeliveryRepository.save(notificationDelivery);

            log.info(
                    "[Notification Processed] ---> Notification event processed. eventId={}, eventType={}, topic={}, partition={}, offset={}",
                    event.eventId(),
                    event.eventType(),
                    consumerRecord.topic(),
                    consumerRecord.partition(),
                    consumerRecord.offset()
            );
        } catch (DataIntegrityViolationException ex) {
            log.info(
                    "[DataIntegrityViolationException] ---> Duplicate notification event ignored. eventId={}, topic={}, partition={}, offset={}",
                    event.eventId(),
                    consumerRecord.topic(),
                    consumerRecord.partition(),
                    consumerRecord.offset()
            );
            log.warn("Exception Message : {}", ex.getMessage());
        }
    }

    private void validateEvent(NotificationRequestedEvent event) {
        if (event == null) throw new InvalidNotificationEventException("Event Payload is Required");
        if (isBlank(event.eventId())) throw new InvalidNotificationEventException("Event ID is Required");
        if (isBlank(event.eventType())) throw new InvalidNotificationEventException("Event Type is Required");
        if (isBlank(event.recipientId())) throw new InvalidNotificationEventException("Recipient ID is Required");
        if (event.channel() == null) throw new InvalidNotificationEventException("Channel is Required");
        if (isBlank(event.provider())) throw new InvalidNotificationEventException("Provider is Required");
        if (isBlank(event.recipientAddress())) throw new InvalidNotificationEventException("Recipient Address is Required");
        if (event.payload() == null) throw new InvalidNotificationEventException("Payload is Required");
    }

    private TNotificationRequest constructNotificationRequest(NotificationRequestedEvent event, ConsumerRecord<String, String> consumerRecord) {
        return TNotificationRequest.builder()
                .eventId(event.eventId())
                .eventType(event.eventType())
                .recipientId(event.recipientId())
                .channel(event.channel())
                .payload(event.payload())
                .topicName(consumerRecord.topic())
                .partitionId(consumerRecord.partition())
                .offsetId(consumerRecord.offset())
                .status(NotificationRequestStatus.PROCESSING)
                .createdBy("KAFKA_CONSUMER")
                .build();
    }

    private TNotificationDelivery constructNotificationDelivery(NotificationRequestedEvent event, TNotificationRequest notificationRequest) {
        return TNotificationDelivery.builder()
                .request(notificationRequest)
                .provider(event.provider())
                .recipientAddress(event.recipientAddress())
                .status(NotificationDeliveryStatus.PENDING)
                .retryCount(0)
                .createdBy("KAFKA_CONSUMER")
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
