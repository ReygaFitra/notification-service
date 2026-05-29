package com.reyga_dev.notification_service.application;

import com.reyga_dev.notification_service.common.ServiceUtils;
import com.reyga_dev.notification_service.domain.dto.EmailRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationCommandService implements INotificationCommandService {

    private static final Logger log = LoggerFactory.getLogger(NotificationCommandService.class);

    private final NotificationRequestRepository notificationRequestRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final INotificationProviderService notificationProviderService;
    private final ServiceUtils serviceUtils;

    public NotificationCommandService(NotificationRequestRepository notificationRequestRepository,
                                      NotificationDeliveryRepository notificationDeliveryRepository,
                                      INotificationProviderService notificationProviderService,
                                      ServiceUtils serviceUtils) {
        this.notificationRequestRepository = notificationRequestRepository;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.notificationProviderService = notificationProviderService;
        this.serviceUtils = serviceUtils;
    }

    @Override
    @Transactional
    public void processNotificationEvent(NotificationRequestedEvent event, ConsumerRecord<String, String> consumerRecord) {
        this.validateEvent(event);
        log.info("[Notification Processing Start] ---> Event ID : {}", event.eventId());

        TNotificationRequest notificationRequest = notificationRequestRepository.findByEventId(event.eventId())
                .orElseThrow(() -> new InvalidNotificationEventException("Notification request is not stored"));

        notificationRequest.setTopicName(consumerRecord.topic());
        notificationRequest.setPartitionId(consumerRecord.partition());
        notificationRequest.setOffsetId(consumerRecord.offset());
        notificationRequest.setStatus(NotificationRequestStatus.PROCESSING);
        notificationRequestRepository.save(notificationRequest);

        List<TNotificationDelivery> notificationDeliveries = notificationDeliveryRepository.findAllByRequestEventId(event.eventId());
        notificationDeliveries.forEach(notificationDelivery ->
                notificationDelivery.setStatus(NotificationDeliveryStatus.SENDING)
        );
        notificationDeliveryRepository.saveAll(notificationDeliveries);

        switch (event.channel()) {
            case EMAIL -> {
                EmailRequest emailRequest = serviceUtils.convertValue(event.payload(), EmailRequest.class);
                boolean requireAttachment = emailRequest.attachments() != null && !emailRequest.attachments().isEmpty();
                notificationProviderService.email(event.eventId(), emailRequest, requireAttachment);
            }
            case SMS -> log.info("[SMS Notification Service] ---> On Development");
            case WHATSAPP -> log.info("[WhatsApp Notification Service] ---> On Development");
            case PUSH -> log.info("[Push Notification Service] ---> On Development");
        }

        log.info(
                "[Notification Processed] ---> Notification event marked as processing. eventId={}, eventType={}, topic={}, partition={}, offset={}",
                event.eventId(), event.eventType(), consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset()
        );
    }

    @Override
    @Transactional
    public void processDeadLetterEvent(NotificationRequestedEvent event, ConsumerRecord<String, String> consumerRecord) {
        String errorMessage = "Message moved to DLT. topic=%s, partition=%s, offset=%s"
                .formatted(consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());

        notificationRequestRepository.findByEventId(event.eventId()).ifPresentOrElse(notificationRequest -> {
            notificationRequest.setStatus(NotificationRequestStatus.DLQ);
            notificationRequest.setErrorMessage(errorMessage);
            notificationRequestRepository.save(notificationRequest);
        }, () -> log.warn("[NOTIFICATION DLT] ---> Notification request not found. eventId={}", event.eventId()));

        notificationDeliveryRepository.findAllByRequestEventId(event.eventId()).forEach(notificationDelivery -> {
            notificationDelivery.setStatus(NotificationDeliveryStatus.DLQ);
            notificationDelivery.setErrorMessage(errorMessage);
            notificationDeliveryRepository.save(notificationDelivery);
        });
    }

    @Transactional
    public void completeNotificationEvent(String eventId) {
        TNotificationRequest notificationRequest = notificationRequestRepository.findByEventId(eventId)
                .orElseThrow(() -> new InvalidNotificationEventException("Notification request is not stored"));

        notificationRequest.setStatus(NotificationRequestStatus.COMPLETED);
        notificationRequestRepository.save(notificationRequest);

        log.info("[Notification Completed] ---> Notification request completed. eventId={}", eventId);
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
