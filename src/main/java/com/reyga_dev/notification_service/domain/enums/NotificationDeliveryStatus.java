package com.reyga_dev.notification_service.domain.enums;

public enum NotificationDeliveryStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED,
    RETRYING,
    DLQ
}
