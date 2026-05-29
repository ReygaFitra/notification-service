package com.reyga_dev.notification_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification-service")
public record ResilienceConfigProperties(
        Retry retry
) {
    public record Retry(
            int maxAttempts,
            long waitDurationMillis,
            boolean failAfterMaxAttempts
    ) {}
}
