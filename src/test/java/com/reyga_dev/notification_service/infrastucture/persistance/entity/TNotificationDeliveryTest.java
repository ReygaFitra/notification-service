package com.reyga_dev.notification_service.infrastucture.persistance.entity;

import com.reyga_dev.notification_service.domain.enums.NotificationDeliveryStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TNotificationDeliveryTest {

    @Test
    void should_ReturnEntityWithAllFields_When_BuilderIsUsed() {
        // given
        UUID id = UUID.randomUUID();
        TNotificationRequest request = new TNotificationRequest();
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();
        OffsetDateTime sentAt = OffsetDateTime.now().minusMinutes(1);

        // when
        TNotificationDelivery result = TNotificationDelivery.builder()
                .id(id)
                .request(request)
                .provider("SMTP")
                .recipientAddress("reyga@example.com")
                .status(NotificationDeliveryStatus.SENT)
                .retryCount(2)
                .errorMessage("error")
                .sentAt(sentAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .createdBy("tester")
                .modifiedBy("modifier")
                .version(3)
                .build();

        // then
        assertEquals(id, result.getId());
        assertSame(request, result.getRequest());
        assertEquals("SMTP", result.getProvider());
        assertEquals("reyga@example.com", result.getRecipientAddress());
        assertEquals(NotificationDeliveryStatus.SENT, result.getStatus());
        assertEquals(2, result.getRetryCount());
        assertEquals("error", result.getErrorMessage());
        assertEquals(sentAt, result.getSentAt());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
        assertEquals("tester", result.getCreatedBy());
        assertEquals("modifier", result.getModifiedBy());
        assertEquals(3, result.getVersion());
    }

    @Test
    void should_ReturnEntityWithDefaultValues_When_PrePersistIsCalledWithNullDefaults() {
        // given
        TNotificationDelivery delivery = TNotificationDelivery.builder()
                .request(new TNotificationRequest())
                .provider("SMTP")
                .recipientAddress("reyga@example.com")
                .build();

        // when
        delivery.prePersist();

        // then
        assertNotNull(delivery.getCreatedAt());
        assertEquals("SYSTEM", delivery.getCreatedBy());
        assertEquals(NotificationDeliveryStatus.PENDING, delivery.getStatus());
        assertEquals(0, delivery.getRetryCount());
        assertEquals(1, delivery.getVersion());
        assertNull(delivery.getUpdatedAt());
        assertNull(delivery.getModifiedBy());
    }

    @Test
    void should_PreserveExistingValues_When_PrePersistIsCalledWithExistingDefaults() {
        // given
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        TNotificationDelivery delivery = TNotificationDelivery.builder()
                .createdAt(createdAt)
                .createdBy("KAFKA_CONSUMER")
                .version(7)
                .status(NotificationDeliveryStatus.DLQ)
                .retryCount(4)
                .build();

        // when
        delivery.prePersist();

        // then
        assertEquals(createdAt, delivery.getCreatedAt());
        assertEquals("KAFKA_CONSUMER", delivery.getCreatedBy());
        assertEquals(7, delivery.getVersion());
        assertEquals(NotificationDeliveryStatus.DLQ, delivery.getStatus());
        assertEquals(4, delivery.getRetryCount());
    }

    @Test
    void should_SetUpdatedAtAndDefaultModifiedBy_When_PreUpdateIsCalled() {
        // given
        TNotificationDelivery delivery = new TNotificationDelivery();

        // when
        delivery.preUpdate();

        // then
        assertNotNull(delivery.getUpdatedAt());
        assertEquals("SYSTEM", delivery.getModifiedBy());
    }

    @Test
    void should_PreserveModifiedBy_When_PreUpdateIsCalledWithExistingModifiedBy() {
        // given
        TNotificationDelivery delivery = TNotificationDelivery.builder()
                .modifiedBy("tester")
                .build();

        // when
        delivery.preUpdate();

        // then
        assertNotNull(delivery.getUpdatedAt());
        assertEquals("tester", delivery.getModifiedBy());
    }

    @Test
    void should_UpdateFields_When_SettersAreCalled() {
        // given
        TNotificationDelivery delivery = new TNotificationDelivery();
        UUID id = UUID.randomUUID();
        TNotificationRequest request = new TNotificationRequest();
        OffsetDateTime sentAt = OffsetDateTime.now();

        // when
        delivery.setId(id);
        delivery.setRequest(request);
        delivery.setProvider("SMTP");
        delivery.setRecipientAddress("reyga@example.com");
        delivery.setStatus(NotificationDeliveryStatus.SENDING);
        delivery.setRetryCount(2);
        delivery.setErrorMessage("error");
        delivery.setSentAt(sentAt);

        // then
        assertEquals(id, delivery.getId());
        assertSame(request, delivery.getRequest());
        assertEquals("SMTP", delivery.getProvider());
        assertEquals("reyga@example.com", delivery.getRecipientAddress());
        assertEquals(NotificationDeliveryStatus.SENDING, delivery.getStatus());
        assertEquals(2, delivery.getRetryCount());
        assertEquals("error", delivery.getErrorMessage());
        assertEquals(sentAt, delivery.getSentAt());
    }
}
