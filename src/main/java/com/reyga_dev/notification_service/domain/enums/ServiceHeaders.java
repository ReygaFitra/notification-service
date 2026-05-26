package com.reyga_dev.notification_service.domain.enums;

public enum ServiceHeaders {
    REQUEST_ID("X-Request-Id");

    private final String label;

    ServiceHeaders(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
