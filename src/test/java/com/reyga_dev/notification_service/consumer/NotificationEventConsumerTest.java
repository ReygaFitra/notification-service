package com.reyga_dev.notification_service.consumer;

import com.reyga_dev.notification_service.application.NotificationCommandService;
import com.reyga_dev.notification_service.common.ServiceUtils;
import com.reyga_dev.notification_service.domain.dto.NotificationRequestedEvent;
import com.reyga_dev.notification_service.domain.enums.NotificationChannel;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationCommandService notificationCommandService;

    @Mock
    private ServiceUtils serviceUtils;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private NotificationEventConsumer notificationEventConsumer;

    @Test
    void should_ProcessEventAndAcknowledgeMessage_When_MessageIsValid() {
        // given
        ConsumerRecord<String, String> consumerRecord = consumerRecord();
        NotificationRequestedEvent event = event();
        when(serviceUtils.readValue(consumerRecord.value(), NotificationRequestedEvent.class)).thenReturn(event);

        // when
        notificationEventConsumer.consume(consumerRecord, acknowledgment);

        // then
        verify(serviceUtils).readValue(consumerRecord.value(), NotificationRequestedEvent.class);
        verify(notificationCommandService).processNotificationEvent(event, consumerRecord);
        verify(notificationCommandService).completeNotificationEvent(event.eventId());
        verify(acknowledgment).acknowledge();
        verifyNoMoreInteractions(serviceUtils, notificationCommandService, acknowledgment);
    }

    @Test
    void should_ThrowExceptionAndNotAcknowledgeMessage_When_MessageCannotBeParsed() {
        // given
        ConsumerRecord<String, String> consumerRecord = consumerRecord();
        IllegalArgumentException exception = new IllegalArgumentException("Invalid JSON message");
        when(serviceUtils.readValue(consumerRecord.value(), NotificationRequestedEvent.class)).thenThrow(exception);

        // when
        IllegalArgumentException result = assertThrows(
                IllegalArgumentException.class,
                () -> notificationEventConsumer.consume(consumerRecord, acknowledgment)
        );

        // then
        assertSame(exception, result);
        verify(serviceUtils).readValue(consumerRecord.value(), NotificationRequestedEvent.class);
        verifyNoMoreInteractions(serviceUtils);
        verifyNoMoreInteractions(notificationCommandService, acknowledgment);
    }

    @Test
    void should_ThrowExceptionAndNotAcknowledgeMessage_When_ProcessNotificationEventFails() {
        // given
        ConsumerRecord<String, String> consumerRecord = consumerRecord();
        NotificationRequestedEvent event = event();
        RuntimeException exception = new RuntimeException("Processing failed");
        when(serviceUtils.readValue(consumerRecord.value(), NotificationRequestedEvent.class)).thenReturn(event);
        org.mockito.Mockito.doThrow(exception)
                .when(notificationCommandService)
                .processNotificationEvent(event, consumerRecord);

        // when
        RuntimeException result = assertThrows(
                RuntimeException.class,
                () -> notificationEventConsumer.consume(consumerRecord, acknowledgment)
        );

        // then
        assertSame(exception, result);
        verify(serviceUtils).readValue(consumerRecord.value(), NotificationRequestedEvent.class);
        verify(notificationCommandService).processNotificationEvent(event, consumerRecord);
        verifyNoMoreInteractions(serviceUtils, notificationCommandService);
        verifyNoMoreInteractions(acknowledgment);
    }

    @Test
    void should_ThrowExceptionAfterProcessingEvent_When_AcknowledgeFails() {
        // given
        ConsumerRecord<String, String> consumerRecord = consumerRecord();
        NotificationRequestedEvent event = event();
        RuntimeException exception = new RuntimeException("Ack failed");
        when(serviceUtils.readValue(consumerRecord.value(), NotificationRequestedEvent.class)).thenReturn(event);
        org.mockito.Mockito.doThrow(exception).when(acknowledgment).acknowledge();

        // when
        RuntimeException result = assertThrows(
                RuntimeException.class,
                () -> notificationEventConsumer.consume(consumerRecord, acknowledgment)
        );

        // then
        assertSame(exception, result);
        verify(serviceUtils).readValue(consumerRecord.value(), NotificationRequestedEvent.class);
        verify(notificationCommandService).processNotificationEvent(event, consumerRecord);
        verify(notificationCommandService).completeNotificationEvent(event.eventId());
        verify(acknowledgment).acknowledge();
        verifyNoMoreInteractions(serviceUtils, notificationCommandService, acknowledgment);
    }

    private ConsumerRecord<String, String> consumerRecord() {
        return new ConsumerRecord<>(
                "notification.requested",
                1,
                10L,
                "event-001",
                "{\"eventId\":\"event-001\"}"
        );
    }

    private NotificationRequestedEvent event() {
        return new NotificationRequestedEvent(
                "event-001",
                "USER_REGISTERED",
                "user-001",
                NotificationChannel.EMAIL,
                "SMTP",
                "reyga@example.com",
                Map.of("name", "Reyga")
        );
    }
}
