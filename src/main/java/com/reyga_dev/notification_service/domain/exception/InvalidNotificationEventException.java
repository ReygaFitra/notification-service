package com.reyga_dev.notification_service.domain.exception;

public class InvalidNotificationEventException extends RuntimeException {

    public InvalidNotificationEventException(String message) {
        super(message);
    }

}
