package com.reyga_dev.notification_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification-service.email")
public record EmailConfigProperties(
        String from
) {
}
