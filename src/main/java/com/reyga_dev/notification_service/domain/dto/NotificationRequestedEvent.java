package com.reyga_dev.notification_service.domain.dto;

import com.reyga_dev.notification_service.domain.enums.NotificationChannel;

import java.util.Map;

public record NotificationRequestedEvent(
        String eventId,
        String eventType,
        String recipientId,
        NotificationChannel channel,
        String provider,
        String recipientAddress,
        Map<String, Object> payload
) {}
