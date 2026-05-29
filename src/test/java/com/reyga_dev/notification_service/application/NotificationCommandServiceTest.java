package com.reyga_dev.notification_service.application;

import com.reyga_dev.notification_service.common.ServiceUtils;
import com.reyga_dev.notification_service.domain.dto.EmailRequest;
import com.reyga_dev.notification_service.domain.dto.NotificationRequestedEvent;
import com.reyga_dev.notification_service.domain.enums.NotificationChannel;
import com.reyga_dev.notification_service.domain.enums.NotificationDeliveryStatus;
import com.reyga_dev.notification_service.domain.enums.NotificationRequestStatus;
import com.reyga_dev.notification_service.domain.exception.InvalidNotificationEventException;
import com.reyga_dev.notification_service.infrastucture.persistance.entity.TNotificationDelivery;
import com.reyga_dev.notification_service.infrastucture.persistance.entity.TNotificationRequest;
import com.reyga_dev.notification_service.infrastucture.persistance.repository.NotificationDeliveryRepository;
import com.reyga_dev.notification_service.infrastucture.persistance.repository.NotificationRequestRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceTest {

    @Mock
    private NotificationRequestRepository notificationRequestRepository;

    @Mock
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @Mock
    private INotificationProviderService notificationProviderService;

    @Mock
    private ServiceUtils serviceUtils;

    @InjectMocks
    private NotificationCommandService notificationCommandService;

    @Test
    void should_ImplementNotificationCommandService_When_ServiceIsCreated() {
        // given

        // when

        // then
        assertInstanceOf(INotificationCommandService.class, notificationCommandService);
    }

    @Test
    void should_UpdateNotificationRequestAndDelivery_When_EventIsValidAndDataIsStored() {
        // given
        NotificationRequestedEvent event = validEvent();
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();
        TNotificationRequest notificationRequest = TNotificationRequest.builder()
                .eventId(event.eventId())
                .status(NotificationRequestStatus.STORED)
                .build();
        TNotificationDelivery notificationDelivery = TNotificationDelivery.builder()
                .request(notificationRequest)
                .status(NotificationDeliveryStatus.PENDING)
                .build();
        EmailRequest emailRequest = emailRequest();
        when(notificationRequestRepository.findByEventId(event.eventId())).thenReturn(Optional.of(notificationRequest));
        when(notificationDeliveryRepository.findAllByRequestEventId(event.eventId())).thenReturn(List.of(notificationDelivery));
        when(serviceUtils.convertValue(event.payload(), EmailRequest.class)).thenReturn(emailRequest);

        // when
        notificationCommandService.processNotificationEvent(event, consumerRecord);

        // then
        assertEquals(consumerRecord.topic(), notificationRequest.getTopicName());
        assertEquals(consumerRecord.partition(), notificationRequest.getPartitionId());
        assertEquals(consumerRecord.offset(), notificationRequest.getOffsetId());
        assertEquals(NotificationRequestStatus.PROCESSING, notificationRequest.getStatus());
        assertEquals(NotificationDeliveryStatus.SENDING, notificationDelivery.getStatus());
        verify(notificationRequestRepository).findByEventId(event.eventId());
        verify(notificationRequestRepository).save(notificationRequest);
        verify(notificationDeliveryRepository).findAllByRequestEventId(event.eventId());
        verify(notificationDeliveryRepository).saveAll(List.of(notificationDelivery));
        verify(serviceUtils).convertValue(event.payload(), EmailRequest.class);
        verify(notificationProviderService).email(event.eventId(), emailRequest, false);
        verifyNoMoreInteractions(
                notificationRequestRepository,
                notificationDeliveryRepository,
                notificationProviderService,
                serviceUtils
        );
    }

    @Test
    void should_UpdateNotificationRequestToCompleted_When_EventProcessingIsCompleted() {
        // given
        String eventId = "event-001";
        TNotificationRequest notificationRequest = TNotificationRequest.builder()
                .eventId(eventId)
                .status(NotificationRequestStatus.PROCESSING)
                .build();
        when(notificationRequestRepository.findByEventId(eventId)).thenReturn(Optional.of(notificationRequest));

        // when
        notificationCommandService.completeNotificationEvent(eventId);

        // then
        assertEquals(NotificationRequestStatus.COMPLETED, notificationRequest.getStatus());
        verify(notificationRequestRepository).findByEventId(eventId);
        verify(notificationRequestRepository).save(notificationRequest);
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_UpdateNotificationRequestAndDeliveryToDlq_When_DeadLetterEventIsProcessed() {
        // given
        NotificationRequestedEvent event = validEvent();
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();
        String errorMessage = "Message moved to DLT. topic=notification.requested, partition=1, offset=10";
        TNotificationRequest notificationRequest = TNotificationRequest.builder()
                .eventId(event.eventId())
                .status(NotificationRequestStatus.PROCESSING)
                .build();
        TNotificationDelivery notificationDelivery = TNotificationDelivery.builder()
                .request(notificationRequest)
                .status(NotificationDeliveryStatus.RETRYING)
                .build();
        when(notificationRequestRepository.findByEventId(event.eventId())).thenReturn(Optional.of(notificationRequest));
        when(notificationDeliveryRepository.findAllByRequestEventId(event.eventId())).thenReturn(List.of(notificationDelivery));

        // when
        notificationCommandService.processDeadLetterEvent(event, consumerRecord);

        // then
        assertEquals(NotificationRequestStatus.DLQ, notificationRequest.getStatus());
        assertEquals(errorMessage, notificationRequest.getErrorMessage());
        assertEquals(NotificationDeliveryStatus.DLQ, notificationDelivery.getStatus());
        assertEquals(errorMessage, notificationDelivery.getErrorMessage());
        verify(notificationRequestRepository).findByEventId(event.eventId());
        verify(notificationRequestRepository).save(notificationRequest);
        verify(notificationDeliveryRepository).findAllByRequestEventId(event.eventId());
        verify(notificationDeliveryRepository).save(notificationDelivery);
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_UpdateDeliveryToDlq_When_DeadLetterRequestIsNotFound() {
        // given
        NotificationRequestedEvent event = validEvent();
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();
        String errorMessage = "Message moved to DLT. topic=notification.requested, partition=1, offset=10";
        TNotificationDelivery notificationDelivery = TNotificationDelivery.builder()
                .status(NotificationDeliveryStatus.RETRYING)
                .build();
        when(notificationRequestRepository.findByEventId(event.eventId())).thenReturn(Optional.empty());
        when(notificationDeliveryRepository.findAllByRequestEventId(event.eventId())).thenReturn(List.of(notificationDelivery));

        // when
        notificationCommandService.processDeadLetterEvent(event, consumerRecord);

        // then
        assertEquals(NotificationDeliveryStatus.DLQ, notificationDelivery.getStatus());
        assertEquals(errorMessage, notificationDelivery.getErrorMessage());
        verify(notificationRequestRepository).findByEventId(event.eventId());
        verify(notificationDeliveryRepository).findAllByRequestEventId(event.eventId());
        verify(notificationDeliveryRepository).save(notificationDelivery);
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_ThrowInvalidNotificationEventException_When_NotificationRequestIsNotStored() {
        // given
        NotificationRequestedEvent event = validEvent();
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();
        when(notificationRequestRepository.findByEventId(event.eventId())).thenReturn(Optional.empty());

        // when
        InvalidNotificationEventException result = assertThrows(
                InvalidNotificationEventException.class,
                () -> notificationCommandService.processNotificationEvent(event, consumerRecord)
        );

        // then
        assertEquals("Notification request is not stored", result.getMessage());
        verify(notificationRequestRepository).findByEventId(event.eventId());
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_ThrowInvalidNotificationEventException_When_EventIsNull() {
        // given
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();

        // when
        InvalidNotificationEventException result = assertThrows(
                InvalidNotificationEventException.class,
                () -> notificationCommandService.processNotificationEvent(null, consumerRecord)
        );

        // then
        assertEquals("Event Payload is Required", result.getMessage());
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_ThrowInvalidNotificationEventException_When_EventIdIsBlank() {
        // given
        NotificationRequestedEvent event = eventWithEventId(" ");
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();

        // when
        InvalidNotificationEventException result = assertThrows(
                InvalidNotificationEventException.class,
                () -> notificationCommandService.processNotificationEvent(event, consumerRecord)
        );

        // then
        assertEquals("Event ID is Required", result.getMessage());
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_ThrowInvalidNotificationEventException_When_EventTypeIsBlank() {
        // given
        NotificationRequestedEvent event = eventWithEventType(" ");
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();

        // when
        InvalidNotificationEventException result = assertThrows(
                InvalidNotificationEventException.class,
                () -> notificationCommandService.processNotificationEvent(event, consumerRecord)
        );

        // then
        assertEquals("Event Type is Required", result.getMessage());
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_ThrowInvalidNotificationEventException_When_RecipientIdIsBlank() {
        // given
        NotificationRequestedEvent event = eventWithRecipientId(" ");
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();

        // when
        InvalidNotificationEventException result = assertThrows(
                InvalidNotificationEventException.class,
                () -> notificationCommandService.processNotificationEvent(event, consumerRecord)
        );

        // then
        assertEquals("Recipient ID is Required", result.getMessage());
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_ThrowInvalidNotificationEventException_When_ChannelIsNull() {
        // given
        NotificationRequestedEvent event = eventWithChannel(null);
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();

        // when
        InvalidNotificationEventException result = assertThrows(
                InvalidNotificationEventException.class,
                () -> notificationCommandService.processNotificationEvent(event, consumerRecord)
        );

        // then
        assertEquals("Channel is Required", result.getMessage());
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_ThrowInvalidNotificationEventException_When_ProviderIsBlank() {
        // given
        NotificationRequestedEvent event = eventWithProvider(" ");
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();

        // when
        InvalidNotificationEventException result = assertThrows(
                InvalidNotificationEventException.class,
                () -> notificationCommandService.processNotificationEvent(event, consumerRecord)
        );

        // then
        assertEquals("Provider is Required", result.getMessage());
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_ThrowInvalidNotificationEventException_When_RecipientAddressIsBlank() {
        // given
        NotificationRequestedEvent event = eventWithRecipientAddress(" ");
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();

        // when
        InvalidNotificationEventException result = assertThrows(
                InvalidNotificationEventException.class,
                () -> notificationCommandService.processNotificationEvent(event, consumerRecord)
        );

        // then
        assertEquals("Recipient Address is Required", result.getMessage());
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    @Test
    void should_ThrowInvalidNotificationEventException_When_PayloadIsNull() {
        // given
        NotificationRequestedEvent event = eventWithPayload(null);
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();

        // when
        InvalidNotificationEventException result = assertThrows(
                InvalidNotificationEventException.class,
                () -> notificationCommandService.processNotificationEvent(event, consumerRecord)
        );

        // then
        assertEquals("Payload is Required", result.getMessage());
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository, notificationProviderService, serviceUtils);
    }

    private NotificationRequestedEvent validEvent() {
        return new NotificationRequestedEvent(
                "event-001",
                "USER_REGISTERED",
                "user-001",
                NotificationChannel.EMAIL,
                "SMTP",
                "reyga@example.com",
                Map.of(
                        "to", "reyga@example.com",
                        "subject", "Welcome",
                        "text", "Hello Reyga",
                        "html", false
                )
        );
    }

    private EmailRequest emailRequest() {
        return new EmailRequest(
                "reyga@example.com",
                "Welcome",
                "Hello Reyga",
                null,
                null,
                false,
                null
        );
    }

    private ConsumerRecord<String, String> validConsumerRecord() {
        return new ConsumerRecord<>(
                "notification.requested",
                1,
                10L,
                "event-001",
                "{\"eventId\":\"event-001\"}"
        );
    }

    private NotificationRequestedEvent eventWithEventId(String eventId) {
        NotificationRequestedEvent event = validEvent();
        return new NotificationRequestedEvent(
                eventId,
                event.eventType(),
                event.recipientId(),
                event.channel(),
                event.provider(),
                event.recipientAddress(),
                event.payload()
        );
    }

    private NotificationRequestedEvent eventWithEventType(String eventType) {
        NotificationRequestedEvent event = validEvent();
        return new NotificationRequestedEvent(
                event.eventId(),
                eventType,
                event.recipientId(),
                event.channel(),
                event.provider(),
                event.recipientAddress(),
                event.payload()
        );
    }

    private NotificationRequestedEvent eventWithRecipientId(String recipientId) {
        NotificationRequestedEvent event = validEvent();
        return new NotificationRequestedEvent(
                event.eventId(),
                event.eventType(),
                recipientId,
                event.channel(),
                event.provider(),
                event.recipientAddress(),
                event.payload()
        );
    }

    private NotificationRequestedEvent eventWithChannel(NotificationChannel channel) {
        NotificationRequestedEvent event = validEvent();
        return new NotificationRequestedEvent(
                event.eventId(),
                event.eventType(),
                event.recipientId(),
                channel,
                event.provider(),
                event.recipientAddress(),
                event.payload()
        );
    }

    private NotificationRequestedEvent eventWithProvider(String provider) {
        NotificationRequestedEvent event = validEvent();
        return new NotificationRequestedEvent(
                event.eventId(),
                event.eventType(),
                event.recipientId(),
                event.channel(),
                provider,
                event.recipientAddress(),
                event.payload()
        );
    }

    private NotificationRequestedEvent eventWithRecipientAddress(String recipientAddress) {
        NotificationRequestedEvent event = validEvent();
        return new NotificationRequestedEvent(
                event.eventId(),
                event.eventType(),
                event.recipientId(),
                event.channel(),
                event.provider(),
                recipientAddress,
                event.payload()
        );
    }

    private NotificationRequestedEvent eventWithPayload(Map<String, Object> payload) {
        NotificationRequestedEvent event = validEvent();
        return new NotificationRequestedEvent(
                event.eventId(),
                event.eventType(),
                event.recipientId(),
                event.channel(),
                event.provider(),
                event.recipientAddress(),
                payload
        );
    }
}
