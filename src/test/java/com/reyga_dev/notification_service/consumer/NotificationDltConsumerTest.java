package com.reyga_dev.notification_service.consumer;

import com.reyga_dev.notification_service.application.INotificationCommandService;
import com.reyga_dev.notification_service.common.ServiceUtils;
import com.reyga_dev.notification_service.domain.dto.NotificationRequestedEvent;
import com.reyga_dev.notification_service.domain.enums.NotificationChannel;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDltConsumerTest {

    @Mock
    private Acknowledgment acknowledgment;

    @Mock
    private ServiceUtils serviceUtils;

    @Mock
    private INotificationCommandService notificationCommandService;

    @Test
    void should_ProcessDeadLetterEventAndAcknowledgeMessage_When_DltMessageIsReceived() {
        // given
        ConsumerRecord<String, String> consumerRecord = consumerRecord();
        NotificationRequestedEvent event = event();
        NotificationDltConsumer notificationDltConsumer = notificationDltConsumer();
        when(serviceUtils.readValue(consumerRecord.value(), NotificationRequestedEvent.class)).thenReturn(event);

        // when
        notificationDltConsumer.consumeDlt(consumerRecord, acknowledgment);

        // then
        verify(serviceUtils).readValue(consumerRecord.value(), NotificationRequestedEvent.class);
        verify(notificationCommandService).processDeadLetterEvent(event, consumerRecord);
        verify(acknowledgment).acknowledge();
        verifyNoMoreInteractions(serviceUtils, notificationCommandService, acknowledgment);
    }

    @Test
    void should_ThrowExceptionAndNotAcknowledgeMessage_When_MessageCannotBeParsed() {
        // given
        ConsumerRecord<String, String> consumerRecord = consumerRecord();
        NotificationDltConsumer notificationDltConsumer = notificationDltConsumer();
        IllegalArgumentException exception = new IllegalArgumentException("Invalid JSON message");
        when(serviceUtils.readValue(consumerRecord.value(), NotificationRequestedEvent.class)).thenThrow(exception);

        // when
        IllegalArgumentException result = assertThrows(
                IllegalArgumentException.class,
                () -> notificationDltConsumer.consumeDlt(consumerRecord, acknowledgment)
        );

        // then
        assertSame(exception, result);
        verify(serviceUtils).readValue(consumerRecord.value(), NotificationRequestedEvent.class);
        verifyNoMoreInteractions(serviceUtils);
        verifyNoMoreInteractions(notificationCommandService, acknowledgment);
    }

    @Test
    void should_ThrowExceptionAndNotAcknowledgeMessage_When_ProcessDeadLetterEventFails() {
        // given
        ConsumerRecord<String, String> consumerRecord = consumerRecord();
        NotificationRequestedEvent event = event();
        NotificationDltConsumer notificationDltConsumer = notificationDltConsumer();
        RuntimeException exception = new RuntimeException("DLT processing failed");
        when(serviceUtils.readValue(consumerRecord.value(), NotificationRequestedEvent.class)).thenReturn(event);
        doThrow(exception).when(notificationCommandService).processDeadLetterEvent(event, consumerRecord);

        // when
        RuntimeException result = assertThrows(
                RuntimeException.class,
                () -> notificationDltConsumer.consumeDlt(consumerRecord, acknowledgment)
        );

        // then
        assertSame(exception, result);
        verify(serviceUtils).readValue(consumerRecord.value(), NotificationRequestedEvent.class);
        verify(notificationCommandService).processDeadLetterEvent(event, consumerRecord);
        verifyNoMoreInteractions(serviceUtils, notificationCommandService);
        verifyNoMoreInteractions(acknowledgment);
    }

    @Test
    void should_ThrowExceptionAfterProcessingDeadLetterEvent_When_AcknowledgeFails() {
        // given
        ConsumerRecord<String, String> consumerRecord = consumerRecord();
        NotificationRequestedEvent event = event();
        NotificationDltConsumer notificationDltConsumer = notificationDltConsumer();
        RuntimeException exception = new RuntimeException("Ack failed");
        when(serviceUtils.readValue(consumerRecord.value(), NotificationRequestedEvent.class)).thenReturn(event);
        doThrow(exception).when(acknowledgment).acknowledge();

        // when
        RuntimeException result = assertThrows(
                RuntimeException.class,
                () -> notificationDltConsumer.consumeDlt(consumerRecord, acknowledgment)
        );

        // then
        assertSame(exception, result);
        verify(serviceUtils).readValue(consumerRecord.value(), NotificationRequestedEvent.class);
        verify(notificationCommandService).processDeadLetterEvent(event, consumerRecord);
        verify(acknowledgment).acknowledge();
        verifyNoMoreInteractions(serviceUtils, notificationCommandService, acknowledgment);
    }

    private ConsumerRecord<String, String> consumerRecord() {
        return new ConsumerRecord<>(
                "notification.requested.dlt",
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

    private NotificationDltConsumer notificationDltConsumer() {
        return new NotificationDltConsumer(
                serviceUtils,
                notificationCommandService
        );
    }
}
