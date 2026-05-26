package com.reyga_dev.notification_service.domain.exception;

public class NotificationProcessingException extends RuntimeException {

    public NotificationProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
