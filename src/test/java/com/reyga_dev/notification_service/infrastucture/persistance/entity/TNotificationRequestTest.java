package com.reyga_dev.notification_service.infrastucture.persistance.entity;

import com.reyga_dev.notification_service.domain.enums.NotificationChannel;
import com.reyga_dev.notification_service.domain.enums.NotificationRequestStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TNotificationRequestTest {

    @Test
    void should_ReturnEntityWithAllFields_When_BuilderIsUsed() {
        // given
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();
        Map<String, Object> payload = Map.of("name", "Reyga");

        // when
        TNotificationRequest result = TNotificationRequest.builder()
                .id(id)
                .eventId("event-001")
                .eventType("USER_REGISTERED")
                .recipientId("user-001")
                .channel(NotificationChannel.EMAIL)
                .payload(payload)
                .topicName("notification.requested")
                .partitionId(1)
                .offsetId(10L)
                .status(NotificationRequestStatus.PROCESSING)
                .errorMessage("error")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .createdBy("tester")
                .modifiedBy("modifier")
                .version(2)
                .build();

        // then
        assertEquals(id, result.getId());
        assertEquals("event-001", result.getEventId());
        assertEquals("USER_REGISTERED", result.getEventType());
        assertEquals("user-001", result.getRecipientId());
        assertEquals(NotificationChannel.EMAIL, result.getChannel());
        assertSame(payload, result.getPayload());
        assertEquals("notification.requested", result.getTopicName());
        assertEquals(1, result.getPartitionId());
        assertEquals(10L, result.getOffsetId());
        assertEquals(NotificationRequestStatus.PROCESSING, result.getStatus());
        assertEquals("error", result.getErrorMessage());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
        assertEquals("tester", result.getCreatedBy());
        assertEquals("modifier", result.getModifiedBy());
        assertEquals(2, result.getVersion());
    }

    @Test
    void should_ReturnEntityWithDefaultValues_When_PrePersistIsCalledWithNullDefaults() {
        // given
        TNotificationRequest request = TNotificationRequest.builder()
                .eventId("event-001")
                .eventType("USER_REGISTERED")
                .recipientId("user-001")
                .channel(NotificationChannel.EMAIL)
                .payload(Map.of("name", "Reyga"))
                .build();

        // when
        request.prePersist();

        // then
        assertNotNull(request.getCreatedAt());
        assertEquals("SYSTEM", request.getCreatedBy());
        assertEquals(NotificationRequestStatus.STORED, request.getStatus());
        assertEquals(1, request.getVersion());
        assertNull(request.getUpdatedAt());
        assertNull(request.getModifiedBy());
    }

    @Test
    void should_PreserveExistingValues_When_PrePersistIsCalledWithExistingDefaults() {
        // given
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        TNotificationRequest request = TNotificationRequest.builder()
                .createdAt(createdAt)
                .createdBy("KAFKA_CONSUMER")
                .version(7)
                .status(NotificationRequestStatus.DLQ)
                .build();

        // when
        request.prePersist();

        // then
        assertEquals(createdAt, request.getCreatedAt());
        assertEquals("KAFKA_CONSUMER", request.getCreatedBy());
        assertEquals(7, request.getVersion());
        assertEquals(NotificationRequestStatus.DLQ, request.getStatus());
    }

    @Test
    void should_SetUpdatedAtAndDefaultModifiedBy_When_PreUpdateIsCalled() {
        // given
        TNotificationRequest request = new TNotificationRequest();

        // when
        request.preUpdate();

        // then
        assertNotNull(request.getUpdatedAt());
        assertEquals("SYSTEM", request.getModifiedBy());
    }

    @Test
    void should_PreserveModifiedBy_When_PreUpdateIsCalledWithExistingModifiedBy() {
        // given
        TNotificationRequest request = TNotificationRequest.builder()
                .modifiedBy("tester")
                .build();

        // when
        request.preUpdate();

        // then
        assertNotNull(request.getUpdatedAt());
        assertEquals("tester", request.getModifiedBy());
    }

    @Test
    void should_UpdateFields_When_SettersAreCalled() {
        // given
        TNotificationRequest request = new TNotificationRequest();
        UUID id = UUID.randomUUID();
        Map<String, Object> payload = Map.of("name", "Reyga");

        // when
        request.setId(id);
        request.setEventId("event-001");
        request.setEventType("USER_REGISTERED");
        request.setRecipientId("user-001");
        request.setChannel(NotificationChannel.EMAIL);
        request.setPayload(payload);
        request.setTopicName("notification.requested");
        request.setPartitionId(1);
        request.setOffsetId(10L);
        request.setStatus(NotificationRequestStatus.PROCESSING);
        request.setErrorMessage("error");

        // then
        assertEquals(id, request.getId());
        assertEquals("event-001", request.getEventId());
        assertEquals("USER_REGISTERED", request.getEventType());
        assertEquals("user-001", request.getRecipientId());
        assertEquals(NotificationChannel.EMAIL, request.getChannel());
        assertSame(payload, request.getPayload());
        assertEquals("notification.requested", request.getTopicName());
        assertEquals(1, request.getPartitionId());
        assertEquals(10L, request.getOffsetId());
        assertEquals(NotificationRequestStatus.PROCESSING, request.getStatus());
        assertEquals("error", request.getErrorMessage());
    }
}
