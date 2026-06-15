package com.reyga_dev.notification_service.domain.dto;

import com.reyga_dev.notification_service.domain.enums.NotificationChannel;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class NotificationRequestedEventTest {

    @Test
    void should_ReturnRecordValues_When_EventIsCreatedWithValues() {
        // given
        Map<String, Object> payload = Map.of("name", "Reyga");

        // when
        NotificationRequestedEvent result = new NotificationRequestedEvent(
                "event-001",
                "USER_REGISTERED",
                "user-001",
                NotificationChannel.EMAIL,
                "SMTP",
                "reyga@example.com",
                payload
        );

        // then
        assertEquals("event-001", result.eventId());
        assertEquals("USER_REGISTERED", result.eventType());
        assertEquals("user-001", result.recipientId());
        assertEquals(NotificationChannel.EMAIL, result.channel());
        assertEquals("SMTP", result.provider());
        assertEquals("reyga@example.com", result.recipientAddress());
        assertSame(payload, result.payload());
    }

    @Test
    void should_ReturnNullValues_When_EventIsCreatedWithNullValues() {
        // given

        // when
        NotificationRequestedEvent result = new NotificationRequestedEvent(
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // then
        assertNull(result.eventId());
        assertNull(result.eventType());
        assertNull(result.recipientId());
        assertNull(result.channel());
        assertNull(result.provider());
        assertNull(result.recipientAddress());
        assertNull(result.payload());
    }
}
