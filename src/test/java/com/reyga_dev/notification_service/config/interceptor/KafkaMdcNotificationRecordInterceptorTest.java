package com.reyga_dev.notification_service.config.interceptor;

import com.reyga_dev.notification_service.domain.enums.ServiceHeaders;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class KafkaMdcNotificationRecordInterceptorTest {

    private final KafkaMdcNotificationRecordInterceptor interceptor = new KafkaMdcNotificationRecordInterceptor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_PutRequestIdAndKafkaMetadataToMdc_When_RequestIdHeaderExists() {
        // given
        ConsumerRecord<String, String> eventRecord = consumerRecord();
        eventRecord.headers().add(
                ServiceHeaders.REQUEST_ID.getLabel(),
                "request-001".getBytes(StandardCharsets.UTF_8)
        );
        Consumer<String, String> consumerData = mockConsumer();

        // when
        ConsumerRecord<String, String> result = interceptor.intercept(eventRecord, consumerData);

        // then
        assertSame(eventRecord, result);
        assertEquals("request-001", MDC.get("requestId"));
        assertEquals(eventRecord.topic(), MDC.get("kafkaTopic"));
        assertEquals(String.valueOf(eventRecord.partition()), MDC.get("kafkaPartition"));
        assertEquals(String.valueOf(eventRecord.offset()), MDC.get("kafkaOffset"));
        verifyNoInteractions(consumerData);
    }

    @Test
    void should_GenerateRequestIdAndPutKafkaMetadataToMdc_When_RequestIdHeaderIsMissing() {
        // given
        ConsumerRecord<String, String> eventRecord = consumerRecord();
        Consumer<String, String> consumerData = mockConsumer();

        // when
        ConsumerRecord<String, String> result = interceptor.intercept(eventRecord, consumerData);

        // then
        assertSame(eventRecord, result);
        assertDoesNotThrow(() -> UUID.fromString(MDC.get("requestId")));
        assertEquals(eventRecord.topic(), MDC.get("kafkaTopic"));
        assertEquals(String.valueOf(eventRecord.partition()), MDC.get("kafkaPartition"));
        assertEquals(String.valueOf(eventRecord.offset()), MDC.get("kafkaOffset"));
        verifyNoInteractions(consumerData);
    }

    @Test
    void should_GenerateRequestIdAndPutKafkaMetadataToMdc_When_RequestIdHeaderValueIsNull() {
        // given
        ConsumerRecord<String, String> eventRecord = new ConsumerRecord<>(
                "notification.requested",
                1,
                10L,
                "event-001",
                "{\"eventId\":\"event-001\"}"
        );
        eventRecord.headers().add(new RecordHeaders().add(ServiceHeaders.REQUEST_ID.getLabel(), null).lastHeader(ServiceHeaders.REQUEST_ID.getLabel()));
        Consumer<String, String> consumerData = mockConsumer();

        // when
        ConsumerRecord<String, String> result = interceptor.intercept(eventRecord, consumerData);

        // then
        assertSame(eventRecord, result);
        assertDoesNotThrow(() -> UUID.fromString(MDC.get("requestId")));
        assertEquals(eventRecord.topic(), MDC.get("kafkaTopic"));
        assertEquals(String.valueOf(eventRecord.partition()), MDC.get("kafkaPartition"));
        assertEquals(String.valueOf(eventRecord.offset()), MDC.get("kafkaOffset"));
        verifyNoInteractions(consumerData);
    }

    @Test
    void should_ClearMdc_When_RecordProcessingIsCompleted() {
        // given
        ConsumerRecord<String, String> eventRecord = consumerRecord();
        Consumer<String, String> consumerData = mockConsumer();
        MDC.put("requestId", "request-001");
        MDC.put("kafkaTopic", eventRecord.topic());
        MDC.put("kafkaPartition", String.valueOf(eventRecord.partition()));
        MDC.put("kafkaOffset", String.valueOf(eventRecord.offset()));

        // when
        interceptor.afterRecord(eventRecord, consumerData);

        // then
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("kafkaTopic"));
        assertNull(MDC.get("kafkaPartition"));
        assertNull(MDC.get("kafkaOffset"));
        verifyNoInteractions(consumerData);
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

    @SuppressWarnings("unchecked")
    private Consumer<String, String> mockConsumer() {
        return mock(Consumer.class);
    }
}
