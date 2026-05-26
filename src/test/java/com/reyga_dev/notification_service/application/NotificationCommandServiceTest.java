package com.reyga_dev.notification_service.application;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceTest {

    @Mock
    private NotificationRequestRepository notificationRequestRepository;

    @Mock
    private NotificationDeliveryRepository notificationDeliveryRepository;

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
    void should_SaveNotificationRequestAndDelivery_When_EventIsValid() {
        // given
        NotificationRequestedEvent event = validEvent();
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();
        ArgumentCaptor<TNotificationRequest> requestCaptor = ArgumentCaptor.forClass(TNotificationRequest.class);
        ArgumentCaptor<TNotificationDelivery> deliveryCaptor = ArgumentCaptor.forClass(TNotificationDelivery.class);

        // when
        notificationCommandService.processNotificationEvent(event, consumerRecord);

        // then
        verify(notificationRequestRepository).saveAndFlush(requestCaptor.capture());
        verify(notificationDeliveryRepository).save(deliveryCaptor.capture());
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository);

        TNotificationRequest savedRequest = requestCaptor.getValue();
        assertEquals(event.eventId(), savedRequest.getEventId());
        assertEquals(event.eventType(), savedRequest.getEventType());
        assertEquals(event.recipientId(), savedRequest.getRecipientId());
        assertEquals(event.channel(), savedRequest.getChannel());
        assertEquals(event.payload(), savedRequest.getPayload());
        assertEquals(consumerRecord.topic(), savedRequest.getTopicName());
        assertEquals(consumerRecord.partition(), savedRequest.getPartitionId());
        assertEquals(consumerRecord.offset(), savedRequest.getOffsetId());
        assertEquals(NotificationRequestStatus.PROCESSING, savedRequest.getStatus());

        TNotificationDelivery savedDelivery = deliveryCaptor.getValue();
        assertSame(savedRequest, savedDelivery.getRequest());
        assertEquals(event.provider(), savedDelivery.getProvider());
        assertEquals(event.recipientAddress(), savedDelivery.getRecipientAddress());
        assertEquals(NotificationDeliveryStatus.PENDING, savedDelivery.getStatus());
        assertEquals(0, savedDelivery.getRetryCount());
    }

    @Test
    void should_IgnoreDuplicateEvent_When_RequestRepositoryThrowsDataIntegrityViolationException() {
        // given
        NotificationRequestedEvent event = validEvent();
        ConsumerRecord<String, String> consumerRecord = validConsumerRecord();
        when(notificationRequestRepository.saveAndFlush(any(TNotificationRequest.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate event ID"));

        // when
        notificationCommandService.processNotificationEvent(event, consumerRecord);

        // then
        verify(notificationRequestRepository).saveAndFlush(any(TNotificationRequest.class));
        verify(notificationDeliveryRepository, never()).save(any(TNotificationDelivery.class));
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository);
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
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository);
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
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository);
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
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository);
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
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository);
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
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository);
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
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository);
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
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository);
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
        verifyNoMoreInteractions(notificationRequestRepository, notificationDeliveryRepository);
    }

    private NotificationRequestedEvent validEvent() {
        return new NotificationRequestedEvent(
                "event-001",
                "USER_REGISTERED",
                "user-001",
                NotificationChannel.EMAIL,
                "SMTP",
                "reyga@example.com",
                Map.of("subject", "Welcome", "name", "Reyga")
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
