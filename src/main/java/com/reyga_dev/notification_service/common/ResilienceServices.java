package com.reyga_dev.notification_service.common;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

public interface ResilienceServices {

    default void useRetry(RetryConfig config, RetryRegistry registry, String name, Runnable runnable) {
        Retry retry = registry.retry(name, config);
        Retry.decorateRunnable(retry, runnable).run();
    }

}
