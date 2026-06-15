package com.reyga_dev.notification_service.config;

import com.reyga_dev.notification_service.common.ResilienceServices;
import com.reyga_dev.notification_service.config.properties.ResilienceConfigProperties;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

    @Bean
    public RetryConfig retryConfig(ResilienceConfigProperties properties) {
        long baseWaitDurationMillis = properties.retry().waitDurationMillis();
        IntervalFunction linearIntervalFunction = attempt -> baseWaitDurationMillis * attempt;

        return RetryConfig.custom()
                .maxAttempts(properties.retry().maxAttempts())
                // Linear backoff: retry#1=base, retry#2=2*base, retry#3=3*base, ...
                .intervalFunction(linearIntervalFunction)
                .failAfterMaxAttempts(properties.retry().failAfterMaxAttempts())
                .build();
    }

    @Bean
    public RetryRegistry retryRegistry(RetryConfig retryConfig) {
        RetryRegistry registry = RetryRegistry.of(retryConfig);
        registry.getEventPublisher()
                .onEntryAdded(event -> {
                    Retry addedRetry = event.getAddedEntry();
                    addedRetry.getEventPublisher().onRetry(retryEvent ->
                            log.info(
                                    "[Resilience Retry] ---> Retry triggered [{}] attempt#{} because: {}",
                                    retryEvent.getName(),
                                    retryEvent.getNumberOfRetryAttempts(),
                                    retryEvent.getLastThrowable() != null
                                            ? retryEvent.getLastThrowable().getMessage()
                                            : "unknown error"
                            )

                    );
                });
        return registry;
    }

    @Bean
    public ResilienceServices resilienceServices() {
        return new ResilienceServices() {
        };
    }

}
