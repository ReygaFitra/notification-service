package com.reyga_dev.notification_service.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationDltConsumerTest {

    @Mock
    private Acknowledgment acknowledgment;

    private final NotificationDltConsumer notificationDltConsumer = new NotificationDltConsumer();

    @Test
    void should_AcknowledgeMessage_When_DltMessageIsReceived() {
        // given
        ConsumerRecord<String, String> consumerRecord = consumerRecord();

        // when
        notificationDltConsumer.consumeDlt(consumerRecord, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
        verifyNoMoreInteractions(acknowledgment);
    }

    @Test
    void should_ThrowException_When_AcknowledgeFails() {
        // given
        ConsumerRecord<String, String> consumerRecord = consumerRecord();
        RuntimeException exception = new RuntimeException("Ack failed");
        doThrow(exception).when(acknowledgment).acknowledge();

        // when
        RuntimeException result = assertThrows(
                RuntimeException.class,
                () -> notificationDltConsumer.consumeDlt(consumerRecord, acknowledgment)
        );

        // then
        assertSame(exception, result);
        verify(acknowledgment).acknowledge();
        verifyNoMoreInteractions(acknowledgment);
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
}
